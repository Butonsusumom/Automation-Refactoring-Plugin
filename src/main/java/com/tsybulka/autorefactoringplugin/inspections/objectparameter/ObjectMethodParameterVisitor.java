package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectMethodParameterVisitor extends CodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.object.parameter.display.name");

	private List<ImplementationSmell> smellsList;

	public ObjectMethodParameterVisitor(List<ImplementationSmell> smellsList) {
		this.smellsList = smellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isObjectMethodParameterCheck();
	}

	@Override
	public void visitMethod(PsiMethod method) {
		if (isInspectionEnabled()) {
			super.visitMethod(method);

			// Get method parameters
			PsiParameterList parameterList = method.getParameterList();
			for (PsiParameter parameter : parameterList.getParameters()) {
				if (!(parameter.getType() instanceof PsiPrimitiveType)) {
					// Analyze method body to determine accessed properties from parameters
					Set<PsiField> accessedProperties = new HashSet<>();
					Set<String> methodCalls = new HashSet<>();
					method.accept(new JavaRecursiveElementVisitor() {
						@Override
						public void visitReferenceExpression(PsiReferenceExpression expression) {
							super.visitReferenceExpression(expression);
							PsiElement resolved = expression.resolve();
							if (resolved instanceof PsiField && isParameterOfObject(parameter, resolved)) {
								accessedProperties.add((PsiField) resolved);
							} else if (resolved instanceof PsiMethod) {
								PsiMethod resolvedMethod = (PsiMethod) resolved;
								if (isGetterMethod(resolvedMethod)) {
									PsiField objectField = resolveGetterField(resolvedMethod);
									if (isParameterOfObject(parameter, objectField)) {
										accessedProperties.add(objectField);
									}
								} else {
									methodCalls.add(resolvedMethod.getName());
								}
							}
						}
					});

					// Check if method can be refactored
					if (accessedProperties.size() == 1 && methodCalls.isEmpty()) {
						PsiField property = accessedProperties.iterator().next();
						registerSmell(method, property, parameter);
					}
				}
			}
		}
	}

	private PsiField resolveGetterField(PsiMethod getterMethod) {
		// Check if the method is a getter method
		if (isGetterMethod(getterMethod)) {
			// Extract property name from getter method name
			String propertyName = getPropertyNameFromGetter(getterMethod);
			// Find the corresponding field in the class
			PsiClass containingClass = getterMethod.getContainingClass();
			if (containingClass != null) {
				PsiField[] fields = containingClass.getAllFields();
				for (PsiField field : fields) {
					if (field.getName().equals(propertyName)) {
						return field;
					}
				}
			}
		}
		return null;
	}

	private static String getPropertyNameFromGetter(PsiMethod method) {
		// Extract property name from getter method name
		String name = method.getName();
		if (name.startsWith("get")) {
			return StringUtil.decapitalize(name.substring(3));
		} else if (name.startsWith("is")) {
			return StringUtil.decapitalize(name.substring(2));
		}
		return null;
	}

	private boolean isParameterOfObject(PsiParameter parameter, PsiElement resolvedElement) {
		// Check if the resolved element is a field of the parameter's type
		PsiType parameterType = parameter.getType();
		if (parameterType instanceof PsiClassType) {
			PsiClass parameterClass = ((PsiClassType) parameterType).resolve();
			if (parameterClass != null) {
				for (PsiField field : parameterClass.getAllFields()) {
					if (field.equals(resolvedElement)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isGetterMethod(PsiMethod method) {
		// Check if the method is a getter method
		String name = method.getName();
		return method.getParameterList().isEmpty() && (name.startsWith("get") || name.startsWith("is")) && method.getReturnType() != null;
	}

	void registerSmell(PsiMethod method, PsiField property, PsiParameter parameter) {
		// Find the containing class of the expression
		PsiClass containingClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
		String className = "";
		String packageName = "";
		String methodName = method.getName();

		if (containingClass != null) {
			className = containingClass.getName();
			// Find the package name of the class
			PsiFile containingFile = containingClass.getContainingFile();
			if (containingFile instanceof PsiJavaFile) {
				packageName = ((PsiJavaFile) containingFile).getPackageName();
			}
		}

		smellsList.add(new ImplementationSmell(NAME, packageName, InspectionsBundle.message("inspection.object.parameter.problem.descriptor", property.getName()), ImplementationSmellType.OBJECT_METHOD_PARAMETER, parameter, className, methodName));

	}
}

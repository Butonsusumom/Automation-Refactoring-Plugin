package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ObjectMethodParameterFix implements LocalQuickFix {

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return InspectionsBundle.message("inspection.object.parameter.use.quickfix");
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
		PsiParameter parameter = (PsiParameter) problemDescriptor.getPsiElement();
		PsiField field = replaceFieldUsageWithParameter(parameter, factory);
		if (field != null) {
			replaceParameterWithField(factory, parameter, field);
		}
	}

	private void replaceParameterWithField(PsiElementFactory factory, PsiParameter parameter, PsiField field) {
		try {
			PsiParameter newParameter = factory.createParameter(field.getName(), field.getType());
			parameter.replace(newParameter);
		} catch (IncorrectOperationException e) {
			e.printStackTrace();
		}
	}

	private PsiField replaceFieldUsageWithParameter(PsiParameter parameter, PsiElementFactory factory) {
		// Navigate up the PSI tree to find the containing method
		final PsiField[] objectField = new PsiField[1];
		PsiMethod method = findContainingMethod(parameter);
		method.accept(new JavaRecursiveElementVisitor() {
			@Override
			public void visitReferenceExpression(PsiReferenceExpression expression) {
				super.visitReferenceExpression(expression);
				PsiElement resolved = expression.resolve();
				if (resolved instanceof PsiField && isParameterOfObject(parameter, resolved)) {
					objectField[0] = (PsiField) resolved;
					expression.replace(factory.createExpressionFromText(objectField[0].getName(), null));
				} else if (resolved instanceof PsiMethod && isGetterMethod((PsiMethod) resolved)) {
					objectField[0] = resolveGetterField((PsiMethod) resolved);
					PsiMethodCallExpression methodCall = getMethodCallFromReference(expression);
					if (isParameterOfObject(parameter, objectField[0])) {
						if (methodCall != null) {
							methodCall.replace(factory.createExpressionFromText(objectField[0].getName(), null));

						}
					}
				}
			}
		});
		return objectField[0];
	}

	private PsiMethodCallExpression getMethodCallFromReference(PsiReferenceExpression referenceExpression) {
		PsiElement parent = referenceExpression.getParent();
		if (parent instanceof PsiMethodCallExpression) {
			return (PsiMethodCallExpression) parent;
		}
		return null;
	}

	private PsiMethod findContainingMethod(PsiElement element) {
		// Navigate up the PSI tree until a method is found
		while (element != null && !(element instanceof PsiMethod)) {
			element = element.getParent();
		}
		return (PsiMethod) element; // This may return null if no method is found
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
}
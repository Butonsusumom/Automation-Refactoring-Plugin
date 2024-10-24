package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.projectanalyses.MetricsCalculationService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ObjectMethodParameterFix implements LocalQuickFix {

	@SafeFieldForPreview
	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();

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
		PsiMethod containingMethod = findContainingMethod(parameter);
		List<PsiMethodCallExpression> methodCalls = findMethodCallsUsingParameter(project, containingMethod);
		int parameterIndex = getParameterIndex(parameter, containingMethod);
		PsiField field = replaceFieldUsageWithParameter(parameter, factory, containingMethod);
		if (field != null) {
			replaceParameterWithField(factory, parameter, field);
			adjustMethodCalls(methodCalls, parameterIndex, field, factory);
		}
	}

	private void adjustMethodCalls(List<PsiMethodCallExpression> methodCalls, int parameterIndex, PsiField field, PsiElementFactory factory) {
		for (PsiMethodCallExpression callExpression : methodCalls) {
			try {
				PsiExpressionList argumentList = callExpression.getArgumentList();
				PsiExpression[] arguments = argumentList.getExpressions();

				if (parameterIndex != -1 && parameterIndex < arguments.length) {
					String fieldName = field.getName();
					StringBuilder stringBuilder = new StringBuilder(arguments[parameterIndex].getText()).append(".");
					if (isFieldPublic(field)){
						stringBuilder.append(fieldName);
					} else {
						stringBuilder.append("get").append(StringUtils.capitalize(fieldName)).append("()");
					}
					PsiExpression fieldAccessExpression = factory.createExpressionFromText(stringBuilder.toString(), null);
					arguments[parameterIndex].replace(fieldAccessExpression);
				}
			} catch (IncorrectOperationException e) {
				Logger.getInstance(ObjectMethodParameterFix.class).error("Failed to adjust method call", e);
			}
		}
	}

	public boolean isFieldPublic(PsiField field) {
		if (field != null) {
			PsiModifierList modifierList = field.getModifierList();
			return modifierList != null && modifierList.hasModifierProperty(PsiModifier.PUBLIC);
		}
		return false;
	}

	public List<PsiMethodCallExpression> findMethodCallsUsingParameter(Project project, PsiMethod containingMethod) {
		if (containingMethod == null) {
			return new ArrayList<>(); // Return an empty list if the method is null
		}

		Set<PsiClass> classes = metricsCalculationService.collectPsiClassesFromSrc(project);

		List<PsiMethodCallExpression> methodCalls = new ArrayList<>();

		for (PsiClass psiClass : classes) {
			psiClass.accept(new JavaRecursiveElementVisitor() {
				@Override
				public void visitMethodCallExpression(PsiMethodCallExpression expression) {
					super.visitMethodCallExpression(expression);
					if (isCallOfMethod(expression, containingMethod)) {
						methodCalls.add(expression); // Add each found method call expression to the list
					}
				}
			});
		}

		return methodCalls;
	}

	private boolean isCallOfMethod(PsiMethodCallExpression callExpression, PsiMethod targetMethod) {
		if (callExpression == null || targetMethod == null) {
			return false; // Return false if either parameter is null
		}

		PsiMethod resolvedMethod = callExpression.resolveMethod();
		return resolvedMethod != null && resolvedMethod.equals(targetMethod);
	}

	private void replaceParameterWithField(PsiElementFactory factory, PsiParameter parameter, PsiField field) {
		try {
			PsiParameter newParameter = factory.createParameter(field.getName(), field.getType());
			parameter.replace(newParameter);
		} catch (IncorrectOperationException e) {
			e.printStackTrace();
		}
	}

	private PsiField replaceFieldUsageWithParameter(PsiParameter parameter, PsiElementFactory factory, PsiMethod method) {
		// Navigate up the PSI tree to find the containing method
		final PsiField[] objectField = new PsiField[1];
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

	public static int getParameterIndex(PsiParameter parameter, PsiMethod containingMethod) {
		if (containingMethod != null) {
			PsiParameter[] parameters = containingMethod.getParameterList().getParameters();
			for (int i = 0; i < parameters.length; i++) {
				if (parameter.equals(parameters[i])) {
					return i; // Return the index if found
				}
			}
		}
		return -1; // Return -1 if the parameter is not found or if there is no containing method
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
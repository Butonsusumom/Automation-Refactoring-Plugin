package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionQuickFix;
import com.tsybulka.autorefactoringplugin.util.PsiElementsUtils;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The class provides a quick fix for the object method parameter smell
 * identified during code inspections. This fix replaces method parameters of object types with the corresponding
 * field of that object type when only one field is accessed within a method.
 */
public class ObjectMethodParameterFix extends BaseCodeInspectionQuickFix {

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return InspectionsBundle.message("inspection.object.parameter.use.quickfix");
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiParameter parameter = (PsiParameter) problemDescriptor.getPsiElement();
		PsiMethod containingMethod = PsiTreeUtil.getParentOfType(parameter, PsiMethod.class);
		if (containingMethod == null) {
			return;
		}
		PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
		PsiField field = replaceFieldUsageWithParameter(parameter, factory, containingMethod);
		if (field != null) {
			replaceParameterWithField(factory, parameter, field);
			List<PsiMethodCallExpression> methodCalls = findMethodCallsUsingParameter(project, containingMethod);
			int parameterIndex = getParameterIndex(parameter, containingMethod);
			adjustMethodCalls(methodCalls, parameterIndex, field, factory);
		}
	}

	/**
	 * Adjusts method calls that are using the specified parameter by replacing
	 * the parameter with the field access expression.
	 *
	 * @param methodCalls    the list of method calls to adjust
	 * @param parameterIndex the index of the parameter in the method call
	 * @param field          the field to replace the parameter with
	 * @param factory        the factory used to create new expressions
	 */
	private void adjustMethodCalls(List<PsiMethodCallExpression> methodCalls, int parameterIndex, PsiField field, PsiElementFactory factory) {
		for (PsiMethodCallExpression callExpression : methodCalls) {
			try {
				PsiExpressionList argumentList = callExpression.getArgumentList();
				PsiExpression[] arguments = argumentList.getExpressions();

				if (parameterIndex >= 0 && parameterIndex < arguments.length) {
					PsiExpression fieldAccessExpression = createFieldAccessExpression(field, factory, arguments[parameterIndex]);
					arguments[parameterIndex].replace(fieldAccessExpression);
				}
			} catch (IncorrectOperationException e) {
				Logger.getInstance(ObjectMethodParameterFix.class).error("Failed to adjust method call", e);
			}
		}
	}

	/**
	 * Creates an expression for accessing the field, using the appropriate
	 * method call if the field is not public.
	 *
	 * @param field    the field to access
	 * @param factory  the factory used to create expressions
	 * @param argument the original argument expression
	 * @return the created field access expression
	 */
	private PsiExpression createFieldAccessExpression(PsiField field, PsiElementFactory factory, PsiExpression argument) {
		String fieldName = field.getName();
		String fieldAccess;

		if (isFieldPublic(field)) {
			fieldAccess = fieldName;
		} else {
			fieldAccess = "get" + StringUtils.capitalize(fieldName) + "()";
		}

		return factory.createExpressionFromText(argument.getText() + "." + fieldAccess, null);
	}

	/**
	 * Checks if the specified field is public.
	 *
	 * @param field the field to check
	 * @return true if the field is public, false otherwise
	 */
	public boolean isFieldPublic(PsiField field) {
		return field != null && field.hasModifierProperty(PsiModifier.PUBLIC);
	}

	/**
	 * Finds all method calls within the project that use the specified parameter.
	 *
	 * @param project          the project context
	 * @param containingMethod the method containing the parameter
	 * @return a list of method calls using the parameter
	 */
	public List<PsiMethodCallExpression> findMethodCallsUsingParameter(Project project, PsiMethod containingMethod) {
		if (containingMethod == null) {
			return new ArrayList<>();
		}
		Set<PsiClass> classes = PsiElementsUtils.collectPsiClassesFromSrc(project);
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

	/**
	 * Checks if the specified method call expression is calling the target method.
	 *
	 * @param callExpression the method call expression
	 * @param targetMethod   the method to check against
	 * @return true if it is a call to the target method, false otherwise
	 */
	private boolean isCallOfMethod(PsiMethodCallExpression callExpression, PsiMethod targetMethod) {
		if (callExpression == null || targetMethod == null) {
			return false;
		}
		PsiMethod resolvedMethod = callExpression.resolveMethod();
		return resolvedMethod != null && resolvedMethod.equals(targetMethod);
	}

	/**
	 * Replaces the parameter with a new field in the method's body.
	 *
	 * @param factory   the factory to create new parameters
	 * @param parameter the parameter to replace
	 * @param field     the field to replace with
	 */
	private void replaceParameterWithField(PsiElementFactory factory, PsiParameter parameter, PsiField field) {
		try {
			PsiParameter newParameter = factory.createParameter(field.getName(), field.getType());
			parameter.replace(newParameter);
		} catch (IncorrectOperationException e) {
			Logger.getInstance(ObjectMethodParameterFix.class).error("Failed to replace parameter with field", e);
		}
	}

	/**
	 * Replaces the field usage in the method with the parameter.
	 *
	 * @param parameter the parameter to analyze
	 * @param factory   the factory to create new expressions
	 * @param method    the method containing the parameter
	 * @return the replaced field or null if none was found
	 */
	private PsiField replaceFieldUsageWithParameter(PsiParameter parameter, PsiElementFactory factory, PsiMethod method) {
		final PsiField[] objectField = new PsiField[1];

		method.accept(new JavaRecursiveElementVisitor() {
			@Override
			public void visitReferenceExpression(PsiReferenceExpression expression) {
				super.visitReferenceExpression(expression);
				handleReferenceExpression(expression, parameter, objectField, factory);
			}
		});

		return objectField[0];
	}

	/**
	 * Handles the reference expression for possible field and getter method calls.
	 *
	 * @param expression  the reference expression
	 * @param parameter   the parameter being analyzed
	 * @param objectField an array holding the found field
	 * @param factory     the factory to create new expressions
	 */
	private void handleReferenceExpression(PsiReferenceExpression expression, PsiParameter parameter, PsiField[] objectField, PsiElementFactory factory) {
		PsiElement resolved = expression.resolve();
		if (resolved instanceof PsiField && PsiElementsUtils.isParameterOfObject(parameter, resolved)) {
			objectField[0] = (PsiField) resolved;
			expression.replace(factory.createExpressionFromText(objectField[0].getName(), null));
		} else if (resolved instanceof PsiMethod && PsiElementsUtils.isGetterMethod((PsiMethod) resolved)) {
			objectField[0] = resolveGetterField((PsiMethod) resolved);
			PsiMethodCallExpression methodCall = getMethodCallFromReference(expression);
			if (PsiElementsUtils.isParameterOfObject(parameter, objectField[0]) && methodCall != null) {
				methodCall.replace(factory.createExpressionFromText(objectField[0].getName(), null));
			}
		}
	}

	/**
	 * Gets the index of the specified parameter in the containing method.
	 *
	 * @param parameter        the parameter to find
	 * @param containingMethod the method containing the parameter
	 * @return the index of the parameter, or -1 if not found
	 */
	public static int getParameterIndex(PsiParameter parameter, PsiMethod containingMethod) {
		if (containingMethod != null) {
			PsiParameter[] parameters = containingMethod.getParameterList().getParameters();
			for (int i = 0; i < parameters.length; i++) {
				if (parameter.equals(parameters[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Gets the method call expression from the reference expression's parent.
	 *
	 * @param referenceExpression the reference expression
	 * @return the method call expression, or null if not found
	 */
	private PsiMethodCallExpression getMethodCallFromReference(PsiReferenceExpression referenceExpression) {
		PsiElement parent = referenceExpression.getParent();
		return parent instanceof PsiMethodCallExpression ? (PsiMethodCallExpression) parent : null;
	}

	/**
	 * Resolves the field corresponding to the given getter method.
	 *
	 * @param getterMethod the getter method to analyze
	 * @return the corresponding field, or null if not found
	 */
	private PsiField resolveGetterField(PsiMethod getterMethod) {
		if (PsiElementsUtils.isGetterMethod(getterMethod)) {
			String propertyName = PsiElementsUtils.getPropertyNameFromGetter(getterMethod);
			PsiClass containingClass = getterMethod.getContainingClass();
			if (containingClass != null) {
				for (PsiField field : containingClass.getAllFields()) {
					if (field.getName().equals(propertyName)) {
						return field;
					}
				}
			}
		}
		return null;
	}

}
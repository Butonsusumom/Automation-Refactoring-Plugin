package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.util.PsiElementsUtils;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Visitor for inspecting method calls that involve enum comparisons.
 * This class identifies when enums are incorrectly compared using
 * reference comparison methods like equals() or Objects.equals().
 *
 * @see BaseCodeInspectionVisitor
 */
@Getter
public class EnumComparisonVisitor extends BaseCodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.comparing.enums.references.display.name");
	private static final String DESCRIPTION = InspectionsBundle.message("inspection.comparing.enums.references.problem.descriptor");

	private List<ImplementationSmell> smellsList = new ArrayList<>();

	@Override
	public boolean isInspectionEnabled() {
		return settings.isEnumComparisonCheck();
	}

	@Override
	public void visitMethodCallExpression(PsiMethodCallExpression expression) {
		if (isInspectionEnabled()) {
			super.visitMethodCallExpression(expression);
			evaluateMethodCall(expression);
		}
	}

	/**
	 * Evaluates the method call to determine if it involves enum comparisons.
	 *
	 * @param expression The method call expression to evaluate.
	 */
	private void evaluateMethodCall(PsiMethodCallExpression expression) {
		PsiReferenceExpression methodExpression = expression.getMethodExpression();
		String methodName = methodExpression.getReferenceName();
		PsiMethod method = expression.resolveMethod();

		if (method != null && method.getContainingClass() != null) {
			boolean isObjectsMethod = Objects.equals(method.getContainingClass().getQualifiedName(), "java.util.Objects");

			if ("equals".equals(methodName)) {
				if (isObjectsMethod) {
					checkObjectsEqualsMethod(expression);
				} else {
					checkEqualsMethod(expression);
				}
			}
		}
	}

	/**
	 * Checks if any argument in the equals() method call is of enum type.
	 *
	 * @param expression The method call expression to check.
	 */
	private void checkEqualsMethod(PsiMethodCallExpression expression) {
		for (PsiExpression argument : expression.getArgumentList().getExpressions()) {
			if (PsiElementsUtils.isEnumType(argument.getType())) {
				registerSmell(expression);
				return;
			}
		}
	}

	/**
	 * Checks if all arguments in the Objects.equals() method call are of enum type.
	 *
	 * @param expression The method call expression to check.
	 */
	private void checkObjectsEqualsMethod(PsiMethodCallExpression expression) {
		for (PsiExpression argument : expression.getArgumentList().getExpressions()) {
			if (!PsiElementsUtils.isEnumType(argument.getType())) {
				return;
			}
		}
		registerSmell(expression);
	}

	/**
	 * Registers an implementation smell for the given method call expression.
	 *
	 * @param expression The method call expression that contains the smell.
	 */
	private void registerSmell(PsiMethodCallExpression expression) {
		PsiClass containingClass = PsiTreeUtil.getParentOfType(expression, PsiClass.class);
		String className = containingClass != null ? containingClass.getName() : "";
		String packageName = PsiElementsUtils.getPackageName(containingClass);
		String methodName = PsiElementsUtils.getContainingMethodName(expression);

		smellsList.add(new ImplementationSmell(
				NAME,
				packageName,
				DESCRIPTION,
				ImplementationSmellType.CONTENT_COMPARISON_INSTEAD_OF_REFERENCE,
				expression,
				className,
				methodName
		));
	}
}

package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionQuickFix;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * QuickFix implementation for addressing improper enum comparisons.
 * This fix replaces comparisons of enum references with appropriate equality checks.
 */
public class EnumComparisonFix extends BaseCodeInspectionQuickFix {

	private static final String FIX_MESSAGE = InspectionsBundle.message("inspection.comparing.enums.references.use.quickfix");

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return FIX_MESSAGE;
	}

	/**
	 * Applies the fix by replacing improper enum comparisons with the correct usage.
	 *
	 * @param project           The current project context.
	 * @param problemDescriptor The descriptor for the detected problem.
	 */
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) problemDescriptor.getPsiElement();
		PsiMethod method = methodCallExpression.resolveMethod();
		assert method != null;

		boolean isObjectsMethod = isObjectsMethod(method);
		PsiExpression lOperand = getLeftOperand(methodCallExpression, isObjectsMethod);
		PsiExpression rOperand = getRightOperand(methodCallExpression, isObjectsMethod);

		PsiBinaryExpression equalsExpression = createEqualsExpression(project, lOperand, rOperand);
		replaceExpression(methodCallExpression, equalsExpression);
	}

	/**
	 * Determines if the method belongs to the java.util.Objects class.
	 *
	 * @param method The method to check.
	 */
	private boolean isObjectsMethod(PsiMethod method) {
		PsiClass containingClass = method.getContainingClass();
		return containingClass != null && "java.util.Objects".equals(containingClass.getQualifiedName());
	}

	/**
	 * Retrieves the left operand based on the method call type.
	 *
	 * @param methodCallExpression The method call expression.
	 * @param isObjectsMethod      True if the method belongs to java.util.Objects, false otherwise.
	 */
	private PsiExpression getLeftOperand(PsiMethodCallExpression methodCallExpression, boolean isObjectsMethod) {
		return isObjectsMethod
				? methodCallExpression.getArgumentList().getExpressions()[0]
				: methodCallExpression.getMethodExpression().getQualifierExpression();
	}

	/**
	 * Retrieves the right operand based on the method call type.
	 *
	 * @param methodCallExpression The method call expression.
	 * @param isObjectsMethod      True if the method belongs to java.util.Objects, false otherwise.
	 */
	private PsiExpression getRightOperand(PsiMethodCallExpression methodCallExpression, boolean isObjectsMethod) {
		return isObjectsMethod
				? methodCallExpression.getArgumentList().getExpressions()[1]
				: methodCallExpression.getArgumentList().getExpressions()[0];
	}

	/**
	 * Creates a binary expression for the equality check.
	 *
	 * @param project  The current project context.
	 * @param lOperand The left operand expression.
	 * @param rOperand The right operand expression.
	 */
	private PsiBinaryExpression createEqualsExpression(@NotNull Project project, PsiExpression lOperand, PsiExpression rOperand) {
		PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
		String comparisonExpression = (isNegationRequired(lOperand))
				? "a != b"
				: "a == b";

		PsiBinaryExpression equalsExpression = (PsiBinaryExpression) factory.createExpressionFromText(comparisonExpression, null);
		Objects.requireNonNull(equalsExpression.getLOperand()).replace(lOperand);
		Objects.requireNonNull(equalsExpression.getROperand()).replace(rOperand);

		return equalsExpression;
	}

	/**
	 * Checks if the negation of the equality check is required.
	 *
	 * @param lOperand The left operand.
	 * @return True if negation is required, false otherwise.
	 */
	private boolean isNegationRequired(PsiExpression lOperand) {
		PsiElement parent = lOperand.getParent();

		if (parent instanceof PsiPrefixExpression) {
			PsiJavaToken operationToken = ((PsiPrefixExpression) parent).getOperationSign();
			return operationToken.getTokenType() == JavaTokenType.EXCL;
		}
		return false;
	}

	/**
	 * Replaces the original method call expression with the new equality expression.
	 *
	 * @param methodCallExpression The original method call expression.
	 * @param equalsExpression     The new equals expression.
	 */
	private void replaceExpression(PsiMethodCallExpression methodCallExpression, PsiBinaryExpression equalsExpression) {
		PsiElement parent = methodCallExpression.getParent();
		if (parent instanceof PsiPrefixExpression) {
			parent.replace(equalsExpression);
		} else {
			methodCallExpression.replace(equalsExpression);
		}
	}
}

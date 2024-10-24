package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionQuickFix;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * QuickFix implementation that transforms object reference comparisons
 * (using == or !=) into calls to Objects.equals() to improve code quality
 * and avoid reference comparison issues.
 */
public class ObjectComparisonFix extends BaseCodeInspectionQuickFix {

	private static final String FIX_MESSAGE =  InspectionsBundle.message("inspection.comparing.objects.references.use.quickfix");

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return FIX_MESSAGE;
	}

	/**
	 * Applies the quick fix by replacing object reference comparisons
	 * with calls to Objects.equals().
	 *
	 * @param project          The current project.
	 * @param problemDescriptor The problem descriptor containing the
	 *                          binary expression that needs to be fixed.
	 */
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiBinaryExpression binaryExpression = (PsiBinaryExpression) problemDescriptor.getPsiElement();
		if (binaryExpression == null) {
			return;
		}

		PsiExpression leftOperand = binaryExpression.getLOperand();
		PsiExpression rightOperand = binaryExpression.getROperand();

		if (rightOperand == null) {
			return;
		}

		replaceWithObjectsEquals(project, binaryExpression, leftOperand, rightOperand);
	}

	/**
	 * Replaces the binary expression with a call to Objects.equals().
	 *
	 * @param project          The current project.
	 * @param binaryExpression The binary expression to replace.
	 * @param leftOperand      The left operand of the binary expression.
	 * @param rightOperand     The right operand of the binary expression.
	 */
	private void replaceWithObjectsEquals(@NotNull Project project,
										  @NotNull PsiBinaryExpression binaryExpression,
										  @NotNull PsiExpression leftOperand,
										  @NotNull PsiExpression rightOperand) {
		PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
		PsiMethodCallExpression equalsCall = (PsiMethodCallExpression) factory.createExpressionFromText("Objects.equals(a, b)", null);

		equalsCall.getArgumentList().getExpressions()[0].replace(leftOperand);
		equalsCall.getArgumentList().getExpressions()[1].replace(rightOperand);

		PsiExpression fixedExpression = (PsiExpression) binaryExpression.replace(equalsCall);

		if (binaryExpression.getOperationTokenType() == JavaTokenType.NE) {
			negateExpression(factory, fixedExpression);
		}
	}

	/**
	 * Negates the expression if the original comparison was a "not equal" (NE).
	 *
	 * @param factory         The PsiElementFactory for creating new expressions.
	 * @param fixedExpression The fixed expression that represents the Objects.equals() call.
	 */
	private void negateExpression(@NotNull PsiElementFactory factory, @NotNull PsiExpression fixedExpression) {
		PsiPrefixExpression negation = (PsiPrefixExpression) factory.createExpressionFromText("!a", null);
		Objects.requireNonNull(negation.getOperand()).replace(fixedExpression);
		fixedExpression.replace(negation);
	}
}

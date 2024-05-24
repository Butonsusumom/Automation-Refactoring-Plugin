package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Fixes a==b -> a.equals(b) and Objects.equals(a,b), in case a nd b Objects
 */
public class ObjectComparisonFix implements LocalQuickFix {

	@NotNull
	@Override
	public String getName() {
		return InspectionsBundle.message("inspection.comparing.objects.references.use.quickfix");
	}

	@Override
	public @NotNull String getFamilyName() {
		return getName();
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiBinaryExpression binaryExpression = (PsiBinaryExpression) problemDescriptor.getPsiElement();
		IElementType operator = binaryExpression.getOperationTokenType();
		PsiExpression lOperand = binaryExpression.getLOperand();
		PsiExpression rOperand = binaryExpression.getROperand();
		if (rOperand == null) {
			return;
		}
		PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
		PsiMethodCallExpression equalsCall = (PsiMethodCallExpression) factory.createExpressionFromText("Objects.equals(a, b)", null);
		equalsCall.getArgumentList().getExpressions()[0].replace(lOperand);
		equalsCall.getArgumentList().getExpressions()[1].replace(rOperand);
		PsiExpression fixedExpression = (PsiExpression) binaryExpression.replace(equalsCall);

		if (operator == JavaTokenType.NE) {
			PsiPrefixExpression negation = (PsiPrefixExpression) factory.createExpressionFromText("!a", null);
			Objects.requireNonNull(negation.getOperand()).replace(fixedExpression);
			fixedExpression.replace(negation);
		}
	}
}

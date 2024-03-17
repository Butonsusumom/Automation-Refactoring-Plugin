package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Fixes a.equals(b) and Objects.equals(a,b) -> a==b, in case a nd b enums
 */
public class EnumComparisonFix implements LocalQuickFix {

	private static final String FIX_MESSAGE =  InspectionsBundle.message("inspection.comparing.enums.references.use.quickfix");

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return FIX_MESSAGE;
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) problemDescriptor.getPsiElement();
		PsiMethod method = methodCallExpression.resolveMethod();
		boolean isObjectsMethod = Objects.equals(method.getContainingClass().getQualifiedName(),"java.util.Objects");


		PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
		PsiBinaryExpression equalsCall = (PsiBinaryExpression) factory.createExpressionFromText("a==b", null);

		PsiExpression lOperand;
		PsiExpression rOperand;

		if (!isObjectsMethod) {
			lOperand =
					methodCallExpression.getMethodExpression().getQualifierExpression();
			rOperand=methodCallExpression.getArgumentList().getExpressions()[0];
		} else {
			rOperand=methodCallExpression.getArgumentList().getExpressions()[1];
			lOperand=methodCallExpression.getArgumentList().getExpressions()[0];
		}

		Objects.requireNonNull(equalsCall.getROperand()).replace(rOperand);
		Objects.requireNonNull(equalsCall.getLOperand()).replace(lOperand);

		methodCallExpression.replace(equalsCall);
	}
}

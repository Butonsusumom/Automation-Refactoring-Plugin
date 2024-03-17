package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtil;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;

/**
 * Finds a==b if a nd b Objects
 */
public class ObjectComparisonInspection extends AbstractBaseJavaLocalInspectionTool  {

	private static final String DESCRIPTION = InspectionsBundle.message("inspection.comparing.objects.references.problem.descriptor");
	private static final String NAME = InspectionsBundle.message("inspection.comparing.objects.references.display.name");

	private final LocalQuickFix quickFix = new ObjectComparisonFix();

	@NotNull
	public String getDisplayName() {
		return NAME;
	}

	@NotNull
	public String getGroupDisplayName() {
		return SmellType.IMPLEMENTATION.toString();
	}

	private static boolean isNullLiteral(PsiExpression expr) {
		return expr instanceof PsiLiteralExpression && "null".equals(expr.getText());
	}

	private boolean isObject(PsiType type) {
		return type instanceof PsiClassType;
	}

	private boolean isNotEnum(PsiType type) {
		PsiClass psiClass = PsiUtil.resolveClassInType(type);
		return psiClass == null || !psiClass.isEnum();
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitBinaryExpression(PsiBinaryExpression expression) {
				super.visitBinaryExpression(expression);
				IElementType opSign = expression.getOperationTokenType();
				if (opSign == JavaTokenType.EQEQ || opSign == JavaTokenType.NE) {
					PsiExpression lOperand = expression.getLOperand();
					PsiExpression rOperand = expression.getROperand();
					if (rOperand == null || isNullLiteral(lOperand) || isNullLiteral(rOperand)) return;

					PsiType lType = lOperand.getType();
					PsiType rType = rOperand.getType();

					if ((isObject(lType) && isNotEnum(lType))|| (isObject(rType)&& isNotEnum(rType))) {
						holder.registerProblem(expression, DESCRIPTION, quickFix);
					}
				}
			}
		};
	}
}

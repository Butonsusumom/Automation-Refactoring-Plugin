package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Finds a.equals(b) and Objects.equals(a,b) if a nd b enums
 */
public class EnumComparisonInspection extends AbstractBaseJavaLocalInspectionTool {

	private static final String DESCRIPTION = InspectionsBundle.message("inspection.comparing.enums.references.problem.descriptor");
	private static final String NAME = InspectionsBundle.message("inspection.comparing.enums.references.display.name");

	private final LocalQuickFix quickFix = new EnumComparisonFix();

	@NotNull
	public String getDisplayName() {
		return NAME;
	}

	@NotNull
	public String getGroupDisplayName() {
		return SmellType.IMPLEMENTATION.toString();
	}

	private boolean isEnum(PsiType type) {
		PsiClass psiClass = PsiUtil.resolveClassInType(type);
		return psiClass != null && psiClass.isEnum();
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
			public void visitMethodCallExpression(PsiMethodCallExpression expression) {
				super.visitMethodCallExpression(expression);

				PsiReferenceExpression methodExpression = expression.getMethodExpression();
				String methodName = methodExpression.getReferenceName();
				PsiMethod method = expression.resolveMethod();
				boolean isObjectsMethod = Objects.equals(method.getContainingClass().getQualifiedName(),"java.util.Objects");

				if (methodName != null && methodName.equals("equals")) {
					if (!isObjectsMethod) {
						checkEqualsMethod(expression, holder);
					} else {
						checkObjectsEqualsMethod(expression, holder);
					}
				}
			}
		};
	}

	private void checkEqualsMethod(PsiMethodCallExpression expression, ProblemsHolder holder) {
		// Check if any argument is an enum type
		for (PsiExpression argument : expression.getArgumentList().getExpressions()) {
			PsiType type = argument.getType();
			if (isEnum(type)) {
				// Found 'equals()' method call involving enums
				holder.registerProblem(expression, DESCRIPTION, quickFix);
			}
		}
	}

	private void checkObjectsEqualsMethod(PsiMethodCallExpression expression, ProblemsHolder holder) {
		// Check if any argument is an enum type
		for (PsiExpression argument : expression.getArgumentList().getExpressions()) {
			PsiType type = argument.getType();
			if (isEnum(type)) {
				// Found 'Objects.equals()' method call involving enums
				holder.registerProblem(expression, DESCRIPTION, quickFix);
			}
		}
	}
}

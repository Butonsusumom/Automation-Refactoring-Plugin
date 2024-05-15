package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiMethodCallExpression;
import com.siyeh.ig.classmetrics.CyclomaticComplexityVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MethodCyclomaticComplexityFix implements LocalQuickFix {

	private static final String FIX_MESSAGE = InspectionsBundle.message("inspection.cyclomatic.complexity.use.quickfix");

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return FIX_MESSAGE;
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		ApplicationManager.getApplication().invokeLater(() -> {
			boolean shouldRefactor = CyclomaticComplexityDialogsProvider.showStartDialog(project);
			if (shouldRefactor) {
				PsiElement element = problemDescriptor.getPsiElement();
				final int originalComplexity = getComplexity(element);
				boolean refactored = refactor(element);
				if (refactored) {
					final int newComplexity = getComplexity(element);
					CyclomaticComplexityDialogsProvider.showComplexityComparisonDialog(project, originalComplexity, newComplexity);
				}
			}
		});
	}

	private boolean refactor(PsiElement element) {
		int maxComplexity = 1;
		PsiElement complexElement = null;
		final int totalComplexity = getComplexity(element);
		PsiElement[] childrenElement = getChildren(element);

		for (PsiElement child : childrenElement) {
			int complexity = getComplexity(child);
			if (complexity > maxComplexity) {
				complexElement = child;
				maxComplexity = complexity;
			}
		}

		if (complexElement == null) {
			return false;
		}

		if (maxComplexity >= totalComplexity) {
			return refactor(complexElement);
		} else {
			PsiElementCyclomaticExtractVisitor extractVisitor = new PsiElementCyclomaticExtractVisitor();
			complexElement.accept(extractVisitor);
			return extractVisitor.isRefactored();
		}
	}

	private int getComplexity(PsiElement element) {
		CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
		element.accept(visitor);
		return visitor.getComplexity();
	}

	private PsiElement[] getChildren(PsiElement element) {
		PsiElement[] childrenElement;
		if (element instanceof PsiIfStatement) {
			PsiIfStatement ifStatement = (PsiIfStatement) element;
			childrenElement = ArrayUtils.addAll(
					Objects.requireNonNull(ifStatement.getThenBranch()).getChildren(),
					Objects.requireNonNull(ifStatement.getElseBranch()).getChildren()
			);
			childrenElement = ArrayUtils.add(childrenElement, ifStatement.getCondition());
		} else if (element instanceof PsiMethodCallExpression) {
			PsiMethodCallExpression expression = (PsiMethodCallExpression) element;
			childrenElement = expression.getArgumentList().getExpressions();
		} else {
			childrenElement = element.getChildren();
		}
		return childrenElement;
	}
}

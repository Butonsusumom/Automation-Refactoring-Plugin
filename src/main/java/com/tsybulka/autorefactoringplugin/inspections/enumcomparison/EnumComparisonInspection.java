package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethodCallExpression;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspection;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Inspection for detecting improper comparisons of enums using reference equality.
 * This inspection checks for method calls that compare enums using equals() or Objects.equals().
 */
public class EnumComparisonInspection extends BaseCodeInspection {
	private static final String NAME = InspectionsBundle.message("inspection.comparing.enums.references.display.name");

	@NotNull
	public String getDisplayName() {
		return NAME;
	}

	@NotNull
	public String getGroupDisplayName() {
		return SmellType.IMPLEMENTATION.toString();
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@Override
	protected LocalQuickFix getQuickFix() {
		return new EnumComparisonFix();
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitMethodCallExpression(PsiMethodCallExpression expression) {
				EnumComparisonVisitor visitor = new EnumComparisonVisitor();
				expression.accept(visitor);
				List<ImplementationSmell> codeSmells = visitor.getSmellsList();
				for (ImplementationSmell smell : codeSmells) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), getQuickFix());
				}
			}
		};
	}

}

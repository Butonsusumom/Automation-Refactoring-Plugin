package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiElementVisitor;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspection;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Inspection that identifies object reference comparisons using equality (==)
 * or inequality (!=) operators. This inspection is designed to help improve
 * code quality by flagging potential implementation smells.
 */
public class ObjectComparisonInspection extends BaseCodeInspection {

	private static final String NAME = InspectionsBundle.message("inspection.comparing.objects.references.display.name");

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

	protected LocalQuickFix getQuickFix() {
		return new ObjectComparisonFix();
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitBinaryExpression(PsiBinaryExpression expression) {
				ObjectComparisonVisitor visitor = new ObjectComparisonVisitor();
				expression.accept(visitor);
				List<ImplementationSmell> codeSmells = visitor.getSmellsList();
				for (ImplementationSmell smell : codeSmells) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), getQuickFix());
				}
			}
		};
	}

}

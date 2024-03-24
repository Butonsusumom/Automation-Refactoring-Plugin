package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds a.equals(b) and Objects.equals(a,b) if a nd b enums
 */
public class EnumComparisonInspection extends AbstractBaseJavaLocalInspectionTool {
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
				List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
				EnumComparisonVisitor visitor =  new EnumComparisonVisitor(implementationSmellsList);
				expression.accept(visitor);
				for (ImplementationSmell smell : implementationSmellsList) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), quickFix);
				}
			}
		};
	}
}

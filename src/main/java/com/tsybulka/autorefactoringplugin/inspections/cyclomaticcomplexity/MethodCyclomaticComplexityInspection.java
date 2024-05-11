package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds methods which extends allowed cyclomatic complexity
 */
public class MethodCyclomaticComplexityInspection extends AbstractBaseJavaLocalInspectionTool {

	private final MethodCyclomaticComplexityFix quickFix = new MethodCyclomaticComplexityFix();
	private static final String NAME = InspectionsBundle.message("inspection.cyclomatic.complexity.display.name");

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
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitMethod(PsiMethod method) {
				List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
				MethodCyclomaticComplexityVisitor visitor =  new MethodCyclomaticComplexityVisitor(implementationSmellsList);
				method.accept(visitor);
				for (ImplementationSmell smell : implementationSmellsList) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), quickFix);
				}
			}
		};
	}
}

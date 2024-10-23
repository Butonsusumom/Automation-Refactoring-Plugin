package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LongMethodInspection extends AbstractBaseJavaLocalInspectionTool {

	private final LongMethodFix quickFix = new LongMethodFix();
	private static final String NAME = InspectionsBundle.message("inspection.long.method.display.name");

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
				List<ImplementationSmell> smellsList = new ArrayList<>();
				LongMethodVisitor visitor = new LongMethodVisitor(smellsList);
				method.accept(visitor);
				for (ImplementationSmell smell : smellsList) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), quickFix);
				}
			}
		};
	}

}

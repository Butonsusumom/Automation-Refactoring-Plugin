package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

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
 * Finds Object parameter where only 1 filed used in method
 */
public class ObjectMethodParameterInspection extends AbstractBaseJavaLocalInspectionTool {

	private final ObjectMethodParameterFix quickFix = new ObjectMethodParameterFix();
	private static final String NAME = InspectionsBundle.message("inspection.object.parameter.display.name");

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
				ObjectMethodParameterVisitor visitor = new ObjectMethodParameterVisitor(smellsList);
				method.accept(visitor);
				for (ImplementationSmell smell : smellsList) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), quickFix);
				}
			}
		};
	}

}

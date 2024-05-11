package com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds Object parameter where only 1 filed used in method
 */
public class RepeatedObjectCreationInspection extends AbstractBaseJavaLocalInspectionTool {

	private final RepeatedObjectConstantQuickFix quickFix = new RepeatedObjectConstantQuickFix();

	private static final String NAME = InspectionsBundle.message("inspection.repeated.object.creation.display.name");

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
		List<ImplementationSmell> smellsList = new ArrayList<>();
		RepeatedObjectCreationVisitor visitor = new RepeatedObjectCreationVisitor(smellsList);

		return new JavaElementVisitor() {
			@Override
			public void visitLocalVariable(PsiLocalVariable variable) {
				visitor.visitLocalVariable(variable);
			}

			@Override
			public void visitNewExpression(PsiNewExpression expression) {
				visitor.visitNewExpression(expression);
			}

			@Override
			public void visitClass(@NotNull PsiClass psiClass) {
				visitor.visitClass(psiClass);

				// Process smellsList after the visitor has analyzed the file
				for (ImplementationSmell smell : smellsList) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), quickFix);
				}
			}
		};
	}

}

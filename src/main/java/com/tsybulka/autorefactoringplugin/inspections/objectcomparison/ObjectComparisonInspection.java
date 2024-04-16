package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiElementVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds a==b if a nd b Objects
 */
public class ObjectComparisonInspection extends AbstractBaseJavaLocalInspectionTool  {

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
				List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
				ObjectComparisonVisitor visitor =  new ObjectComparisonVisitor(implementationSmellsList);
				expression.accept(visitor);
				for (ImplementationSmell smell : implementationSmellsList) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), quickFix);
				}
			}
		};
	}
}

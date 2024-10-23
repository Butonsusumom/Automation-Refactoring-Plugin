package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspection;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import org.jetbrains.annotations.NotNull;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;

import java.util.List;

/**
 * Inspection class for verifying the naming conventions of test methods.
 * <p>
 * This inspection checks whether test methods adhere to the specified naming
 * pattern: "should_when*testedMethod*_given". If a method's name does not
 * match this pattern, it is flagged as a code smell.
 */
public class TestMethodNamingInspection extends BaseCodeInspection {

	private static final String NAME = InspectionsBundle.message("inspection.test.method.name.display.name");

	@NotNull
	public String getDisplayName() {
		return NAME;
	}

	@NotNull
	public String getGroupDisplayName() {
		return SmellType.TEST.toString();
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	protected LocalQuickFix getQuickFix(){
		return new TestMethodNamingFix();
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitMethod(PsiMethod method) {
				TestMethodNamingVisitor visitor = new TestMethodNamingVisitor();
				method.accept(visitor);
				List<TestSmell> codeSmells = visitor.getSmellsList();
				for (TestSmell smell : codeSmells) {
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), getQuickFix());
				}
			}
		};
	}

}

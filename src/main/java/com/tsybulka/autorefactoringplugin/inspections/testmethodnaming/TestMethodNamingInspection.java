package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if test have proper naming
 */
public class TestMethodNamingInspection extends AbstractBaseJavaLocalInspectionTool {

	private final TestMethodNamingFix quickFix = new TestMethodNamingFix();

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

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitMethod(PsiMethod method) {
				List<TestSmell> smellsList = new ArrayList<>();
				TestMethodNamingVisitor visitor = new TestMethodNamingVisitor(smellsList);
				method.accept(visitor);
				for (TestSmell testSmell : smellsList) {
					holder.registerProblem(testSmell.getPsiElement(), testSmell.getDescription(), quickFix);
				}
			}
		};
	}

}

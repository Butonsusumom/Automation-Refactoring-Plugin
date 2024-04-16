package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterFix;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterVisitor;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ObjectMethodParameterSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if test have proper naming: should_when*testedMethod*_given
 */
public class TestMethodNamingInspection extends AbstractBaseJavaLocalInspectionTool {
	//private final ObjectMethodParameterFix quickFix = new ObjectMethodParameterFix();
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
				new ObjectMethodParameterVisitor(smellsList);
				method.accept(visitor);
				for (ImplementationSmell implementationSmell : smellsList) {
					ObjectMethodParameterSmell smell = (ObjectMethodParameterSmell) implementationSmell;
					holder.registerProblem(smell.getPsiElement(), smell.getDescription());
				}
			}
		};
	}

}

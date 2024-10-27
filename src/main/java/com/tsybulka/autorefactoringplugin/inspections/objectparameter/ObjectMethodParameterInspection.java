package com.tsybulka.autorefactoringplugin.inspections.objectparameter;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspection;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionQuickFix;
import com.tsybulka.autorefactoringplugin.inspections.testmethodnaming.TestMethodNamingFix;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspection class that identifies methods with object parameters
 * where only a single field of the object is utilized.
 * <p>
 * This inspection aims to detect potential implementation smells,
 * suggesting that the method may benefit from refactoring, such as
 * passing primitive types or smaller objects instead of larger objects
 * when only one of their fields is being used.
 */
public class ObjectMethodParameterInspection extends BaseCodeInspection {

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

	protected BaseCodeInspectionQuickFix getQuickFix(){
		return new ObjectMethodParameterFix();
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
					holder.registerProblem(smell.getPsiElement(), smell.getDescription(), getQuickFix());
				}
			}
		};
	}

}

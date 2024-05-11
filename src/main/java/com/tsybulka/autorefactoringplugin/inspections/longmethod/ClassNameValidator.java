package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.tsybulka.autorefactoringplugin.projectanalyses.MetricsCalculationService;

import java.util.Objects;
import java.util.Set;

public class ClassNameValidator implements InputValidator {

	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();
	private final Project project;
	private final PsiDirectory directory;

	public ClassNameValidator(Project project, PsiDirectory directory) {
		this.project = project;
		this.directory = directory;
	}

	@Override
	public boolean checkInput(String inputString) {
		// Check if the class name follows the Java naming convention for classes
		if (!inputString.matches("[A-Z][A-Za-z0-9]*")) {
			return false;  // Class name must start with an uppercase letter and follow CamelCase
		}

		Set<PsiClass> classes = metricsCalculationService.collectPsiClassesFromSrc(project);

		// Check if any class with the given name and package already exists in the project
		for (PsiClass psiClass : classes) {
			String className = psiClass.getName();
			PsiDirectory classDirectory = psiClass.getContainingFile().getContainingDirectory();
			if (Objects.equals(className, inputString) && Objects.equals(classDirectory, directory)) {
				return false;  // A class with this package and name already exists
			}
		}

		return true;  // No class with this name and package exists
	}

	@Override
	public boolean canClose(String inputString) {
		return checkInput(inputString);
	}
}


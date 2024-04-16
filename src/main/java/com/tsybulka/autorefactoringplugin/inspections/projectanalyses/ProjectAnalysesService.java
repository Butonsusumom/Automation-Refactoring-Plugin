package com.tsybulka.autorefactoringplugin.inspections.projectanalyses;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonVisitor;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonVisitor;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterVisitor;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.ClassMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProjectAnalysesService {

	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();

	public ProjectSmellsInfo analyseProject(Project project) {
		List<ClassMetrics> projectClassMetrics = metricsCalculationService.calculateProjectMetrics(project);

		List<PsiClass> classes = metricsCalculationService.collectPsiClassesFromSrc(project);

		List<ImplementationSmell> implementationSmellsList = new ArrayList<>(collectImplementationSmells(classes));
		List<ArchitectureSmell> architectureSmellList = new ArrayList<>(collectArchitectureSmells(project));
		List<TestSmell> testSmellsList = new ArrayList<>(collectTestSmells(project));

		return new ProjectSmellsInfo(implementationSmellsList, architectureSmellList, testSmellsList, projectClassMetrics);
	}

	List<ImplementationSmell> collectImplementationSmells(List<PsiClass> classes) {
		List<ImplementationSmell> implementationSmellsList = new ArrayList<>();

		EnumComparisonVisitor enumComparisonVisitor = new EnumComparisonVisitor(implementationSmellsList);
		ObjectComparisonVisitor objectComparisonVisitor = new ObjectComparisonVisitor(implementationSmellsList);
		ObjectMethodParameterVisitor objectMethodParameterVisitor = new ObjectMethodParameterVisitor(implementationSmellsList);

		for (PsiClass psiClass : classes) {
			psiClass.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitElement(@NotNull PsiElement element) {
					super.visitElement(element);
					element.accept(enumComparisonVisitor);
					element.accept(objectComparisonVisitor);
					element.accept(objectMethodParameterVisitor);
				}
			});

		}

		return implementationSmellsList;
	}

	List<ArchitectureSmell> collectArchitectureSmells(Project project) {
		return new ArrayList<>();
	}

	List<TestSmell> collectTestSmells(Project project) {
		return new ArrayList<>();
	}

}

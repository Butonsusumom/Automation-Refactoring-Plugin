package com.tsybulka.autorefactoringplugin.projectanalyses;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity.MethodCyclomaticComplexityVisitor;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonVisitor;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.LongMethodVisitor;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonVisitor;
import com.tsybulka.autorefactoringplugin.inspections.objectparameter.ObjectMethodParameterVisitor;
import com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation.RepeatedObjectCreationVisitor;
import com.tsybulka.autorefactoringplugin.inspections.scatteredfunctionality.ScatteredFunctionalityVisitor;
import com.tsybulka.autorefactoringplugin.inspections.testmethodnaming.TestMethodNamingVisitor;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture.ArchitectureSmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric.ClassMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectAnalysesService {

	private static final String SCATTERED_FUNCTIONALITY_SMELL_NAME = InspectionsBundle.message("inspection.scattered.functionality.display.name");

	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();

	public ProjectSmellsInfo analyseProject(Project project) {
		List<ClassMetrics> projectClassMetrics = metricsCalculationService.calculateProjectMetrics(project);

		Set<PsiClass> classes = metricsCalculationService.collectPsiClassesFromSrc(project);

		List<ImplementationSmell> implementationSmellsList = new ArrayList<>(collectImplementationSmells(classes));
		List<ArchitectureSmell> architectureSmellList = new ArrayList<>(collectArchitectureSmells(classes));
		List<TestSmell> testSmellsList = new ArrayList<>(collectTestSmells(classes));

		return new ProjectSmellsInfo(implementationSmellsList, architectureSmellList, testSmellsList, projectClassMetrics);
	}

	List<ImplementationSmell> collectImplementationSmells(Set<PsiClass> classes) {
		List<ImplementationSmell> implementationSmellsList = new ArrayList<>();

		EnumComparisonVisitor enumComparisonVisitor = new EnumComparisonVisitor(implementationSmellsList);
		ObjectComparisonVisitor objectComparisonVisitor = new ObjectComparisonVisitor(implementationSmellsList);
		ObjectMethodParameterVisitor objectMethodParameterVisitor = new ObjectMethodParameterVisitor(implementationSmellsList);
		MethodCyclomaticComplexityVisitor cyclomaticComplexityVisitor = new MethodCyclomaticComplexityVisitor(implementationSmellsList);
		RepeatedObjectCreationVisitor repeatedObjectCreationVisitor = new RepeatedObjectCreationVisitor(implementationSmellsList);
		LongMethodVisitor longMethodVisitor = new LongMethodVisitor(implementationSmellsList);

		for (PsiClass psiClass : classes) {
			psiClass.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitElement(@NotNull PsiElement element) {
					super.visitElement(element);
					element.accept(enumComparisonVisitor);
					element.accept(objectComparisonVisitor);
					element.accept(objectMethodParameterVisitor);
					element.accept(cyclomaticComplexityVisitor);
					element.accept(repeatedObjectCreationVisitor);
					element.accept(longMethodVisitor);
				}
			});

		}

		return implementationSmellsList;
	}

	Set<ArchitectureSmell> collectArchitectureSmells(Set<PsiClass> classes) {
		Set<ArchitectureSmell> architectureSmellsList = new HashSet<>();
		Map<Integer, Set<PsiElement>> seenCodeBlocks = new HashMap<>();

		for (PsiClass psiClass : classes) {
			psiClass.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitElement(@NotNull PsiElement element) {
					super.visitElement(element);
					element.accept(new ScatteredFunctionalityVisitor(seenCodeBlocks));
				}
			});
		}

		registerScatteredSmell(architectureSmellsList, seenCodeBlocks);

		return architectureSmellsList;
	}

	List<TestSmell> collectTestSmells(Set<PsiClass> classes) {
		List<TestSmell> testSmellsList = new ArrayList<>();

		TestMethodNamingVisitor testMethodNamingVisitor = new TestMethodNamingVisitor(testSmellsList);

		for (PsiClass psiClass : classes) {
			psiClass.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitElement(@NotNull PsiElement element) {
					super.visitElement(element);
					element.accept(testMethodNamingVisitor);
				}
			});
		}

		return testSmellsList;
	}

	void registerScatteredSmell(Set<ArchitectureSmell> smellsList, Map<Integer, Set<PsiElement>> seenCodeBlocks) {
		Map<Integer, Set<PsiElement>> filteredMap = seenCodeBlocks.entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() >= 2) // Filter entries where the set has 2 or more elements
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		for (Map.Entry<Integer, Set<PsiElement>> element : filteredMap.entrySet()) {
			Set<PsiElement> psiElements = element.getValue();
			String scatteredClassesWithComma = getScatteredClasses(psiElements, ", ");
			String scatteredClassesWithNewLine = getScatteredClasses(psiElements, "\n");
			String scatteredMethods = getScatteredMethods(psiElements);
			String scatteredPackages = getScatteredPackages(psiElements);

			smellsList.add(new ArchitectureSmell(SCATTERED_FUNCTIONALITY_SMELL_NAME, scatteredPackages, InspectionsBundle.message("inspection.scattered.functionality.problem.descriptor", scatteredClassesWithComma), ArchitectureSmellType.SCATTERED_FUNCTIONALITY, psiElements, scatteredClassesWithNewLine, scatteredMethods));
		}
	}

	private String getPackageName(PsiElement element) {
		PsiFile psiFile = element.getContainingFile();
		if (psiFile instanceof PsiJavaFile) { // Check if it's a Java file
			PsiJavaFile javaFile = (PsiJavaFile) psiFile;
			PsiPackageStatement packageStatement = javaFile.getPackageStatement();
			if (packageStatement != null) {
				return packageStatement.getPackageName();
			}
		}
		return null; // No package found or not a Java file
	}

	public String getScatteredClasses(Set<PsiElement> psiElements, String delimiter) {
		Set<String> classNames = psiElements.stream()
				.map(element -> PsiTreeUtil.getParentOfType(element, PsiClass.class))
				.filter(Objects::nonNull)
				.map(PsiClass::getName)
				.collect(Collectors.toSet()); // Use a Set to avoid duplicate class names

		return String.join(delimiter, classNames);
	}

	public String getScatteredPackages(Set<PsiElement> psiElements) {
		Set<String> packageNames = psiElements.stream()
				.map(this::getPackageName)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()); // Use a Set to avoid duplicate class names

		return String.join("\n", packageNames);
	}

	public String getScatteredMethods(Set<PsiElement> psiElements) {
		Set<String> methodNames = psiElements.stream()
				.map(element -> PsiTreeUtil.getParentOfType(element, PsiMethod.class))
				.filter(Objects::nonNull)
				.map(PsiMethod::getName)
				.collect(Collectors.toSet()); // Use a Set to avoid duplicate class names

		return String.join("\n", methodNames);
	}

}

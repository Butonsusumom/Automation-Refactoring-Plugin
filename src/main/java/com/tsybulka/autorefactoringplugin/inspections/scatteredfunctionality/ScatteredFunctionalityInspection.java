package com.tsybulka.autorefactoringplugin.inspections.scatteredfunctionality;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.projectanalyses.MetricsCalculationService;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Finds scattered functionality across project
 */
public class ScatteredFunctionalityInspection extends AbstractBaseJavaLocalInspectionTool {

	private static final String NAME = InspectionsBundle.message("inspection.scattered.functionality.display.name");

	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();

	@NotNull
	public String getDisplayName() {
		return NAME;
	}

	@NotNull
	public String getGroupDisplayName() {
		return SmellType.ARCHITECTURE.toString();
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		List<PsiCodeBlock> codeBlocksCurrentFile = new ArrayList<>();

		Project project = holder.getProject();
		Map<Integer, Set<PsiElement>> seenCodeBlocks = new HashMap<>();

		Set<PsiClass> classes = metricsCalculationService.collectPsiClassesFromSrc(project);

		for (PsiClass psiClass : classes) {
			psiClass.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitElement(@NotNull PsiElement element) {
					super.visitElement(element);
					element.accept(new ScatteredFunctionalityVisitor(seenCodeBlocks));
				}
			});
		}

		registerScatteredSmell(holder, seenCodeBlocks, codeBlocksCurrentFile);

		return new JavaElementVisitor() {
			@Override
			public void visitCodeBlock(PsiCodeBlock block) {
				super.visitCodeBlock(block);
				for (Map.Entry<Integer, Set<PsiElement>> entry : seenCodeBlocks.entrySet()) {
					Set<PsiElement> codeBlocks = entry.getValue();
					if (codeBlocks.size() > 1 && codeBlocks.contains(block)) {
						String scatteredClassesWithComma = getScatteredClasses(codeBlocks);
						holder.registerProblem(block, InspectionsBundle.message("inspection.scattered.functionality.problem.descriptor", scatteredClassesWithComma));
					}
				}
			}
		};
	}

	void registerScatteredSmell(ProblemsHolder holder, Map<Integer, Set<PsiElement>> seenCodeBlocks, List<PsiCodeBlock> codeBlocksCurrentFile) {
		Map<Integer, Set<PsiElement>> filteredMap = seenCodeBlocks.entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() >= 2) // Filter entries where the set has 2 or more elements
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		for (Map.Entry<Integer, Set<PsiElement>> element : filteredMap.entrySet()) {
			Set<PsiElement> psiElements = element.getValue();
			String scatteredClassesWithComma = getScatteredClasses(psiElements);

			for (PsiElement psiElement : element.getValue()) {
				if (codeBlocksCurrentFile.contains((PsiCodeBlock) psiElement)) {
					holder.registerProblem(psiElement, InspectionsBundle.message("inspection.scattered.functionality.problem.descriptor", scatteredClassesWithComma));
				}
			}
		}
	}

	public String getScatteredClasses(Set<PsiElement> psiElements) {
		Set<String> classNames = psiElements.stream()
				.map(element -> PsiTreeUtil.getParentOfType(element, PsiClass.class))
				.filter(Objects::nonNull)
				.map(PsiClass::getName)
				.collect(Collectors.toSet()); // Use a Set to avoid duplicate class names

		return String.join(", ", classNames);
	}

}

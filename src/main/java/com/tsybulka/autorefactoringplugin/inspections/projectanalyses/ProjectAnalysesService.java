package com.tsybulka.autorefactoringplugin.inspections.projectanalyses;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.tsybulka.autorefactoringplugin.inspections.enumcomparison.EnumComparisonVisitor;
import com.tsybulka.autorefactoringplugin.inspections.objectcomparison.ObjectComparisonVisitor;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ClassMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.TestSmell;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProjectAnalysesService {

	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();

	public ProjectSmellsInfo analyseProject(Project project) {
		List<ClassMetrics> projectClassMetrics = metricsCalculationService.calculateProjectMetrics(project);
		List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
		List<ArchitectureSmell> architectureSmellList = new ArrayList<>();
		List<TestSmell> testSmellsList = new ArrayList<>();

		PsiManager psiManager = PsiManager.getInstance(project);
		ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

		Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.allScope(project));

		for (VirtualFile virtualFile : virtualFiles) {
			// Check if the file is under source content and not in libraries
			if (fileIndex.isInSourceContent(virtualFile) && !fileIndex.isInLibrary(virtualFile)) {
				PsiFile psiFile = psiManager.findFile(virtualFile);
				if (psiFile != null) {
					implementationSmellsList.addAll(collectImplementationSmells(psiFile));
					architectureSmellList.addAll(collectArchitectureSmells(project));
					testSmellsList.addAll(collectTestSmells(project));
				}
			}
		}
		return new ProjectSmellsInfo(implementationSmellsList, architectureSmellList, testSmellsList, projectClassMetrics);
	}

	List<ImplementationSmell> collectImplementationSmells(PsiFile psiFile) {
		List<ImplementationSmell> implementationSmellsList = new ArrayList<>();

		EnumComparisonVisitor enumComparisonVisitor = new EnumComparisonVisitor(implementationSmellsList);
		ObjectComparisonVisitor objectComparisonVisitor = new ObjectComparisonVisitor(implementationSmellsList);

		psiFile.accept(new PsiRecursiveElementVisitor() {
			@Override
			public void visitElement(@NotNull PsiElement element) {
				super.visitElement(element);
				element.accept(enumComparisonVisitor);
				element.accept(objectComparisonVisitor);
			}
		});

		return implementationSmellsList;
	}

	List<ArchitectureSmell> collectArchitectureSmells(Project project) {
		return new ArrayList<>();
	}

	List<TestSmell> collectTestSmells(Project project) {
		return new ArrayList<>();
	}

}

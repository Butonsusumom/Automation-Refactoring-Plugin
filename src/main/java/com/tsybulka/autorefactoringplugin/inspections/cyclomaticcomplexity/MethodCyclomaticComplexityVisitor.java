package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.classmetrics.CyclomaticComplexityVisitor;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;

import java.util.List;

public class MethodCyclomaticComplexityVisitor extends CodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.cyclomatic.complexity.display.name");

	private final CyclomaticComplexityVisitor cyclomaticComplexityVisitor = new CyclomaticComplexityVisitor();

	private List<ImplementationSmell> implementationSmellsList;

	public MethodCyclomaticComplexityVisitor(List<ImplementationSmell> implementationSmellsList) {
		this.implementationSmellsList = implementationSmellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return true;
	}

	@Override
	public void visitMethod(PsiMethod method) {
		if (isInspectionEnabled()) {
			cyclomaticComplexityVisitor.reset();
			method.accept(cyclomaticComplexityVisitor);
			int complexity = cyclomaticComplexityVisitor.getComplexity();
			if (complexity >= settings.getCyclomaticComplexity()) {
				registerSmell(method, complexity);
			}
		}
	}

	void registerSmell(PsiMethod method, Integer complexity) {
		// Find the containing class of the expression
		PsiClass containingClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
		String className = "";
		String packageName = "";
		String methodName = method.getName();

		if (containingClass != null) {
			className = containingClass.getName();
			// Find the package name of the class
			PsiFile containingFile = containingClass.getContainingFile();
			if (containingFile instanceof PsiJavaFile) {
				packageName = ((PsiJavaFile) containingFile).getPackageName();
			}
		}

		implementationSmellsList.add(
				new ImplementationSmell(NAME, packageName, InspectionsBundle.message("inspection.cyclomatic.complexity.problem.descriptor", complexity.toString()), ImplementationSmellType.CYCLOMATIC_COMPLEXITY, method, className, methodName));

	}

}

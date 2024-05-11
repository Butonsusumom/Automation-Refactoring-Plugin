package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.attributes.LengthyMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;

import java.util.List;

public class LongMethodVisitor extends CodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.long.method.display.name");

	static final int MAX_LOC = 100;
	static final int MAX_PARAMS = 5;
	static final int MAX_NESTING = 3;

	private List<ImplementationSmell> smellsList;

	public LongMethodVisitor(List<ImplementationSmell> smellsList) {
		this.smellsList = smellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isLengthyMethodCheck();
	}

	@Override
	public void visitMethod(PsiMethod method) {
		if (isInspectionEnabled()) {
			super.visitMethod(method);
			LengthyMetrics lengthyMetrics = new LengthyMetrics(getLinesOfCode(method), getNumberOfParameters(method), calculateNestingDepth(method));
			if (!isLongMethodAcceptable(lengthyMetrics)) {
				registerSmell(method, lengthyMetrics);
			}
		}
	}

	private boolean isLongMethodAcceptable(LengthyMetrics metrics) {
		return metrics.getLoc() <= MAX_LOC && metrics.getNumOfParams() <= MAX_PARAMS && metrics.getMaxNestingDepth() <= MAX_NESTING;
	}

	static int getLinesOfCode(PsiElement element) {
		String[] lines = element.getText().split("\n");
		int codeLinesCount = 0;
		for (String line : lines) {
			line = line.trim();
			if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*")) {
				codeLinesCount++;
			}
		}
		return codeLinesCount;
	}

	static int getNumberOfParameters(PsiMethod method) {
		PsiParameterList parameterList = method.getParameterList();
		return parameterList.getParametersCount();
	}

	public static int calculateNestingDepth(PsiElement element) {
		if (element == null) {
			return 0;
		}

		// Use a recursive function to determine the maximum depth
		return getMaxDepth(element, 0); // Start depth calculation inside the method body
	}

	private static int getMaxDepth(PsiElement element, int currentDepth) {
		int maxDepth = currentDepth;

		// Iterate over all children of the current element
		for (PsiElement child : element.getChildren()) {
			if (isControlFlowElement(child)) {
				// If it's a control flow element or a code block, increase the depth
				int childDepth = getMaxDepth(child, currentDepth + 1);
				maxDepth = Math.max(maxDepth, childDepth);
			} else {
				// Otherwise, continue searching through the current depth
				int childDepth = getMaxDepth(child, currentDepth);
				maxDepth = Math.max(maxDepth, childDepth);
			}
		}
		return maxDepth;
	}

	// Helper method to determine if the PsiElement is a control flow element
	private static boolean isControlFlowElement(PsiElement element) {
		return element instanceof PsiIfStatement ||
				element instanceof PsiForStatement ||
				element instanceof PsiWhileStatement ||
				element instanceof PsiDoWhileStatement ||
				element instanceof PsiSwitchStatement;
	}

	void registerSmell(PsiMethod method, LengthyMetrics lengthyMetrics) {
		PsiClass containingClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
		String className = "";
		String packageName = "";
		String methodName = method.getName();

		if (containingClass != null) {
			className = containingClass.getName();
			PsiFile containingFile = containingClass.getContainingFile();
			if (containingFile instanceof PsiJavaFile) {
				packageName = ((PsiJavaFile) containingFile).getPackageName();
			}
		}

		String reason = buildReasonForSmell(lengthyMetrics);

		smellsList.add(new ImplementationSmell(NAME, packageName, InspectionsBundle.message("inspection.long.method.problem.descriptor", reason), ImplementationSmellType.LONG_METHOD, method, className, methodName));
	}

	private String buildReasonForSmell(LengthyMetrics lengthyMetrics) {
		StringBuilder reasonBuilder = new StringBuilder();
		if (lengthyMetrics.getLoc() > MAX_LOC) {
			reasonBuilder.append("Lines of code exceed " + MAX_LOC + ", current number of lines of code: " + lengthyMetrics.getLoc() + ".");
		}
		if (lengthyMetrics.getNumOfParams() > MAX_PARAMS) {
			reasonBuilder.append("Number of parameters exceed " + MAX_PARAMS + ", current number of parameters: " + lengthyMetrics.getNumOfParams() + ".");
		}
		if (lengthyMetrics.getMaxNestingDepth() > MAX_NESTING) {
			reasonBuilder.append("Nesting depth exceeds " + MAX_NESTING + ", current nesting depth: " + lengthyMetrics.getMaxNestingDepth() + ".");
		}
		return reasonBuilder.toString().trim();
	}
}

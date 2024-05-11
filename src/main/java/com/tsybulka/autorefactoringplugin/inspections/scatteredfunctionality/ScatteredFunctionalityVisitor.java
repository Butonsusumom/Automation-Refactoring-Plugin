package com.tsybulka.autorefactoringplugin.inspections.scatteredfunctionality;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;

import java.util.*;

public class ScatteredFunctionalityVisitor extends CodeInspectionVisitor {

	private static final String REPLACEMENT_VARIABLE_NAME = "var";

	private Map<Integer, Set<PsiElement>> seenCodeBlocks;

	public ScatteredFunctionalityVisitor(Map<Integer, Set<PsiElement>> seenCodeBlocks) {
		this.seenCodeBlocks = seenCodeBlocks;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isScatteredFunctionalityCheck();
	}

	@Override
	public void visitCodeBlock(PsiCodeBlock block) {
		if (isInspectionEnabled()) {
			super.visitCodeBlock(block);
			int lineCount = countLines(block);
			if (lineCount >= 4) {
				processElement(block);
			}
		}
	}

	private void processElement(PsiElement element) {
		String normalizedCode = normalizePsiCodeBlock((PsiCodeBlock) element);
		Integer codeHash = normalizedCode.hashCode();

		if (seenCodeBlocks.containsKey(codeHash)) {
			seenCodeBlocks.get(codeHash).add(element);
		} else {
			HashSet<PsiElement> elements = new HashSet<>();
			elements.add(element);
			seenCodeBlocks.put(codeHash, elements);
		}
	}

	public String normalizePsiCodeBlock(PsiCodeBlock codeBlock) {
		StringBuilder normalizedCode = new StringBuilder();
		Set<String> declaredVariables = collectAllVariables(codeBlock);
		recursiveNormalize(codeBlock, normalizedCode, declaredVariables);
		return normalizedCode.toString().trim();
	}

	private Set<String> collectAllVariables(PsiElement element) {
		Set<String> variables = new HashSet<>();
		// Collect variables from all reachable scopes, possibly by analyzing the entire file or module
		PsiFile containingFile = element.getContainingFile();
		PsiTreeUtil.collectElementsOfType(containingFile, PsiVariable.class).forEach(var -> {
			variables.add(var.getName());
		});
		return variables;
	}

	private void recursiveNormalize(PsiElement element, StringBuilder out, Set<String> declaredVariables) {
		if (element == null || element instanceof PsiWhiteSpace || element instanceof PsiComment) {
			return;
		}

		if (element instanceof PsiReferenceExpression) {
			PsiReferenceExpression refExp = (PsiReferenceExpression) element;
			PsiElement qualifier = refExp.getQualifierExpression();
			if (qualifier != null && declaredVariables.contains(qualifier.getText())) {
				// Replace the target variable name in the qualifier
				out.append(REPLACEMENT_VARIABLE_NAME).append('.');
			} else if (qualifier == null && declaredVariables.contains(refExp.getText())) {
				// Direct references to the variable are replaced
				out.append(REPLACEMENT_VARIABLE_NAME);
			} else {
				recursiveNormalize(qualifier, out, declaredVariables); // Process qualifier first
			}
			if (qualifier != null) { // Append method or field name if it's part of a qualified expression
				out.append(refExp.getReferenceName());
			}
			return;
		}

		if (element instanceof PsiMethodCallExpression) {
			PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
			PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
			if (methodExpression.getQualifierExpression() != null && declaredVariables.contains(methodExpression.getQualifierExpression().getText())) {
				out.append(REPLACEMENT_VARIABLE_NAME).append('.').append(methodExpression.getReferenceName());
			} else {
				recursiveNormalize(methodExpression, out, declaredVariables);
			}
			out.append('(');
			PsiExpression[] args = methodCall.getArgumentList().getExpressions();
			for (int i = 0; i < args.length; i++) {
				recursiveNormalize(args[i], out, declaredVariables);
				if (i < args.length - 1) out.append(", ");
			}
			out.append(')');
			return;
		}

		if (element instanceof PsiJavaToken) {
			out.append(element.getText());
			return;
		}

		// Recurse into other types of elements
		for (PsiElement child : element.getChildren()) {
			recursiveNormalize(child, out, declaredVariables);
		}
	}

	private int countLines(PsiElement element) {
		if (element == null) {
			return 0; // Early return if the element is null
		}

		String text = element.getText();
		if (text == null || text.isEmpty()) {
			return 0; // Handle null or empty string to avoid any exceptions
		}

		// Count lines by splitting on line breaks. This handles various line endings (\r, \n, \r\n)
		return text.split("\\R", -1).length; // The regex "\\R" matches any line ending
	}

}

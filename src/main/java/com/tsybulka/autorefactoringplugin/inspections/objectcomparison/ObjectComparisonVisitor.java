package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.ImplementationSmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;

import java.util.List;

public class ObjectComparisonVisitor extends CodeInspectionVisitor {

	private static final String DESCRIPTION = InspectionsBundle.message("inspection.comparing.objects.references.problem.descriptor");
	private static final String NAME = InspectionsBundle.message("inspection.comparing.objects.references.display.name");

	private List<ImplementationSmell> implementationSmellsList;

	public ObjectComparisonVisitor(List<ImplementationSmell> implementationSmellsList) {
		this.implementationSmellsList = implementationSmellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isObjectComparisonCheck();
	}

	@Override
	public void visitBinaryExpression(PsiBinaryExpression expression) {
		if (isInspectionEnabled()) {
			super.visitBinaryExpression(expression);
			IElementType opSign = expression.getOperationTokenType();
			if (opSign == JavaTokenType.EQEQ || opSign == JavaTokenType.NE) {
				PsiExpression lOperand = expression.getLOperand();
				PsiExpression rOperand = expression.getROperand();
				if (rOperand == null || isNullLiteral(lOperand) || isNullLiteral(rOperand)) return;

				PsiType lType = lOperand.getType();
				PsiType rType = rOperand.getType();

				if ((isObject(lType) && isNotEnum(lType)) || (isObject(rType) && isNotEnum(rType))) {
					registerSmell(expression);
				}
			}
		}
	}

	private static boolean isNullLiteral(PsiExpression expr) {
		return expr instanceof PsiLiteralExpression && "null".equals(expr.getText());
	}

	private boolean isObject(PsiType type) {
		return type instanceof PsiClassType;
	}

	private boolean isNotEnum(PsiType type) {
		PsiClass psiClass = PsiUtil.resolveClassInType(type);
		return psiClass == null || !psiClass.isEnum();
	}

	void registerSmell(PsiExpression expression) {
		// Find the containing class of the expression
		PsiClass containingClass = PsiTreeUtil.getParentOfType(expression, PsiClass.class);
		String className = "";
		String packageName = "";
		String methodName = "";

		if (containingClass != null) {
			className = containingClass.getName();
			// Find the package name of the class
			PsiFile containingFile = containingClass.getContainingFile();
			if (containingFile instanceof PsiJavaFile) {
				packageName = ((PsiJavaFile) containingFile).getPackageName();
			}
		}
		// Find the method name that contains the expression
		PsiMethod containingMethod = PsiTreeUtil.getParentOfType(expression, PsiMethod.class);
		if (containingMethod != null) {
			methodName = containingMethod.getName();
		}

		implementationSmellsList.add(
				new ImplementationSmell(NAME, packageName, DESCRIPTION, ImplementationSmellType.REFERENCE_COMPARISON_INSTEAD_OF_CONTENT, expression, className, methodName));

	}
}

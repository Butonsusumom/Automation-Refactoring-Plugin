package com.tsybulka.autorefactoringplugin.inspections.objectcomparison;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.util.PsiElementsUtils;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for inspecting binary expressions that compare object references.
 * This visitor identifies implementation smells related to object reference comparisons,
 * specifically when using the equality (==) or inequality (!=) operators for objects.
 *
 * @see BaseCodeInspectionVisitor
 */
@Getter
public class ObjectComparisonVisitor extends BaseCodeInspectionVisitor{

	private static final String DESCRIPTION = InspectionsBundle.message("inspection.comparing.objects.references.problem.descriptor");
	private static final String NAME = InspectionsBundle.message("inspection.comparing.objects.references.display.name");

	private List<ImplementationSmell> smellsList = new ArrayList<>();

	@Override
	public boolean isInspectionEnabled() {
		return settings.isObjectComparisonCheck();
	}

	/**
	 * Visits a binary expression and checks for object reference comparisons.
	 * If an object comparison is detected using equality (==) or inequality (!=),
	 * it registers an implementation smell.
	 *
	 * @param expression the binary expression being visited.
	 */
	@Override
	public void visitBinaryExpression(PsiBinaryExpression expression) {
		if (isInspectionEnabled()) {
			super.visitBinaryExpression(expression);
			checkForObjectComparison(expression);
		}
	}

	/**
	 * Checks if the given binary expression uses equality or inequality operators
	 * and registers an implementation smell if it compares object references.
	 *
	 * @param expression the binary expression to check.
	 */
	private void checkForObjectComparison(PsiBinaryExpression expression) {
		IElementType operator = expression.getOperationTokenType();

		if (isComparisonOperator(operator)) {
			PsiExpression leftOperand = expression.getLOperand();
			PsiExpression rightOperand = expression.getROperand();

			if (isValidOperands(leftOperand, rightOperand)) {
				PsiType leftType = leftOperand.getType();
				PsiType rightType = rightOperand.getType();

				if (PsiElementsUtils.isObjectType(leftType) || PsiElementsUtils.isObjectType(rightType)) {
					registerSmell(expression);
				}
			}
		}
	}

	/**
	 * Checks if the provided IElementType is an equality (==) or inequality (!=) operator.
	 *
	 * @param operator the operator to check.
	 * @return true if the operator is == or !=; false otherwise.
	 */
	private boolean isComparisonOperator(IElementType operator) {
		return operator == JavaTokenType.EQEQ || operator == JavaTokenType.NE;
	}

	/**
	 * Validates if the left and right operands are suitable for comparison.
	 *
	 * @param leftOperand  the left operand of the expression.
	 * @param rightOperand the right operand of the expression.
	 */
	private boolean isValidOperands(PsiExpression leftOperand, PsiExpression rightOperand) {
		return rightOperand != null && !isNullLiteral(leftOperand) && !isNullLiteral(rightOperand);
	}

	private boolean isNullLiteral(PsiExpression expression) {
		return expression instanceof PsiLiteralExpression && "null".equals(expression.getText());
	}

	/**
	 * Registers an implementation smell when an object comparison is detected.
	 * It gathers information about the containing class and method and adds
	 * an instance of ImplementationSmell to the provided list.
	 *
	 * @param expression the expression that triggered the smell registration.
	 */
	private void registerSmell(PsiExpression expression) {
		PsiClass containingClass = PsiTreeUtil.getParentOfType(expression, PsiClass.class);
		String className = containingClass != null ? containingClass.getName() : "";
		String packageName = PsiElementsUtils.getPackageName(containingClass);
		String methodName = PsiElementsUtils.getContainingMethodName(expression);

		smellsList.add(new ImplementationSmell(
				NAME,
				packageName,
				DESCRIPTION,
				ImplementationSmellType.REFERENCE_COMPARISON_INSTEAD_OF_CONTENT,
				expression,
				className,
				methodName
		));
	}
}

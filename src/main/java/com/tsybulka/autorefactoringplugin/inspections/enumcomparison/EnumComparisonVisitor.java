package com.tsybulka.autorefactoringplugin.inspections.enumcomparison;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;

import java.util.List;
import java.util.Objects;

public class EnumComparisonVisitor extends CodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.comparing.enums.references.display.name");
	private static final String DESCRIPTION = InspectionsBundle.message("inspection.comparing.enums.references.problem.descriptor");

	private List<ImplementationSmell> implementationSmellsList;

	public EnumComparisonVisitor(List<ImplementationSmell> implementationSmellsList) {
		this.implementationSmellsList = implementationSmellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isEnumComparisonCheck();
	}


	@Override
	public void visitMethodCallExpression(PsiMethodCallExpression expression) {
		if (isInspectionEnabled()) {
			super.visitMethodCallExpression(expression);

			PsiReferenceExpression methodExpression = expression.getMethodExpression();
			String methodName = methodExpression.getReferenceName();
			PsiMethod method = expression.resolveMethod();
			if (method != null && method.getContainingClass() != null) {
				boolean isObjectsMethod = Objects.equals(method.getContainingClass().getQualifiedName(), "java.util.Objects");

				if ("equals".equals(methodName)) {
					if (isObjectsMethod) {
						checkObjectsEqualsMethod(expression);
					} else {
						checkEqualsMethod(expression);
					}
				}
			}
		}
	}

	private void checkEqualsMethod(PsiMethodCallExpression expression) {
		// Check if any argument is an enum type
		for (PsiExpression argument : expression.getArgumentList().getExpressions()) {
			PsiType type = argument.getType();
			if (isEnum(type)) {
				// Found 'equals()' method call involving enums
				registerSmell(expression);
			}
		}
	}

	private void checkObjectsEqualsMethod(PsiMethodCallExpression expression) {
		// Check if any argument is an enum type
		for (PsiExpression argument : expression.getArgumentList().getExpressions()) {
			PsiType type = argument.getType();
			// Found 'Objects.equals()' method call involving enums
			if (!isEnum(type)) {
				return;
			}
		}
		registerSmell(expression);
	}

	boolean isEnum(PsiType type) {
		PsiClass psiClass = PsiUtil.resolveClassInType(type);
		return psiClass != null && psiClass.isEnum();
	}

	void registerSmell(PsiMethodCallExpression expression) {
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
				new ImplementationSmell(NAME, packageName, DESCRIPTION, ImplementationSmellType.CONTENT_COMPARISON_INSTEAD_OF_REFERENCE, expression, className, methodName));

	}
}

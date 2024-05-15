package com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RepeatedObjectConstantQuickFix implements LocalQuickFix {

	private static final Map<String, String> resolvedValues = new HashMap<>();

	@Override
	public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
		return InspectionsBundle.message("inspection.repeated.object.creation.use.quickfix");
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		PsiElement element = descriptor.getPsiElement();
		PsiNewExpression newExpression = extractNewExpression(element);

		if (newExpression == null) return;

		PsiClass containingClass = PsiTreeUtil.getParentOfType(newExpression, PsiClass.class);
		if (containingClass == null) return;

		resolvedValues.clear();
		resolveAllVariables(containingClass);

		String objectKey = createObjectKey(newExpression);
		List<PsiNewExpression> similarExpressions = findSimilarNewExpressions(containingClass, objectKey);

		if (similarExpressions.isEmpty()) return;

		PsiField existingConstant = findExistingConstant(containingClass, objectKey);
		if (existingConstant == null) {
			existingConstant = createNewConstant(containingClass, newExpression);
		}

		replaceExpressionsWithConstant(similarExpressions, existingConstant, JavaPsiFacade.getElementFactory(project));
	}

	private void resolveAllVariables(PsiClass psiClass) {
		psiClass.accept(new JavaRecursiveElementVisitor() {
			@Override
			public void visitLocalVariable(PsiLocalVariable variable) {
				super.visitLocalVariable(variable);
				PsiExpression initializer = variable.getInitializer();
				if (initializer != null) {
					resolvedValues.put(variable.getName(), computeValue(initializer));
				}
			}

			private String computeValue(PsiExpression expression) {
				if (expression instanceof PsiLiteralExpression) {
					return expression.getText();
				} else if (expression instanceof PsiReferenceExpression) {
					PsiReferenceExpression refExpr = (PsiReferenceExpression) expression;
					return resolvedValues.getOrDefault(refExpr.getReferenceName(), refExpr.getText());
				}
				return expression.getText();
			}
		});
	}

	@Nullable
	private PsiNewExpression extractNewExpression(PsiElement element) {
		if (element instanceof PsiNewExpression) {
			return (PsiNewExpression) element;
		} else if (element instanceof PsiDeclarationStatement) {
			PsiDeclarationStatement declaration = (PsiDeclarationStatement) element;
			for (PsiElement declaredElement : declaration.getDeclaredElements()) {
				if (declaredElement instanceof PsiLocalVariable) {
					PsiLocalVariable variable = (PsiLocalVariable) declaredElement;
					PsiExpression initializer = variable.getInitializer();
					if (initializer instanceof PsiNewExpression) {
						return (PsiNewExpression) initializer;
					}
				}
			}
		}
		return null;
	}

	private List<PsiNewExpression> findSimilarNewExpressions(PsiClass psiClass, String objectKey) {
		List<PsiNewExpression> expressions = new ArrayList<>();
		psiClass.accept(new JavaRecursiveElementVisitor() {
			@Override
			public void visitNewExpression(PsiNewExpression expression) {
				super.visitNewExpression(expression);
				if (objectKey.equals(createObjectKey(expression)) && !isAssignedToConstant(expression)) {
					expressions.add(expression);
				}
			}

			private boolean isAssignedToConstant(PsiNewExpression expression) {
				PsiElement parent = expression.getParent();

				// Check if it's assigned to a field that is final and has a constant initializer
				if (parent instanceof PsiField) {
					PsiField field = (PsiField) parent;
					return field.hasModifierProperty(PsiModifier.FINAL) && field.hasInitializer();
				}

				// Check if it's assigned to a variable that is final and has a constant initializer
				if (parent instanceof PsiVariable) {
					PsiVariable variable = (PsiVariable) parent;
					return variable.hasModifierProperty(PsiModifier.FINAL) && variable.hasInitializer();
				}

				// Otherwise, it's not assigned to a constant
				return false;
			}
		});
		return expressions;
	}

	private PsiField findExistingConstant(PsiClass psiClass, String objectKey) {
		for (PsiField field : psiClass.getFields()) {
			if (field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.FINAL)) {
				PsiExpression initializer = field.getInitializer();
				if (initializer instanceof PsiNewExpression && objectKey.equals(createObjectKey((PsiNewExpression) initializer))) {
					return field;
				}
			}
		}
		return null;
	}

	private PsiField createNewConstant(PsiClass psiClass, PsiNewExpression newExpression) {
		String typeName = Objects.requireNonNull(newExpression.getClassReference()).getQualifiedName();
		String constantName = generateConstantName(typeName, psiClass);
		String newFieldText = String.format("private static final %s %s = %s;", typeName, constantName, createObjectKey(newExpression));
		PsiField newField = JavaPsiFacade.getElementFactory(psiClass.getProject()).createFieldFromText(newFieldText, psiClass);

		// Find the correct insertion point for the new constant
		PsiElement firstStaticFinalField = null;
		for (PsiField field : psiClass.getFields()) {
			if (field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.FINAL)) {
				firstStaticFinalField = field;
				break;
			}
		}

		if (firstStaticFinalField != null) {
			psiClass.addBefore(newField, firstStaticFinalField);
		} else {
			psiClass.add(newField);
		}

		return newField;
	}

	private String generateConstantName(String typeName, PsiClass psiClass) {
		String className = typeName.substring(typeName.lastIndexOf('.') + 1);
		String baseName = className.toUpperCase() + "_CONSTANT";
		String constantName = baseName;
		int index = 1;
		while (psiClass.findFieldByName(constantName, false) != null) {
			constantName = baseName + index++;
		}
		return constantName;
	}

	private void replaceExpressionsWithConstant(List<PsiNewExpression> expressions, PsiField constant, PsiElementFactory elementFactory) {
		String constantName = constant.getName();
		for (PsiNewExpression expr : expressions) {
			PsiExpression newConstantRef = elementFactory.createExpressionFromText(constantName, expr);
			expr.replace(newConstantRef);
		}
	}

	public static String createObjectKey(PsiNewExpression expression) {
		PsiJavaCodeReferenceElement classReference = expression.getClassReference();
		if (classReference == null) return null;

		String className = classReference.getQualifiedName();
		PsiExpression[] args = Objects.requireNonNull(expression.getArgumentList()).getExpressions();
		StringBuilder keyBuilder = new StringBuilder("new ").append(className).append("(");

		for (PsiExpression arg : args) {
			keyBuilder.append(normalizeArgument(arg)).append(", ");
		}
		if (args.length > 0) {
			keyBuilder.setLength(keyBuilder.length() - 2); // Remove the last comma and space
		}
		keyBuilder.append(")");
		return keyBuilder.toString();
	}

	public static String normalizeArgument(PsiExpression arg) {
		if (arg instanceof PsiLiteralExpression) {
			return arg.getText();
		} else if (arg instanceof PsiReferenceExpression) {
			PsiReferenceExpression ref = (PsiReferenceExpression) arg;
			String refName = ref.getReferenceName();
			if (resolvedValues.containsKey(refName)) {
				return resolvedValues.get(refName);
			}
		} else if (arg instanceof PsiNewExpression) {
			return createObjectKey((PsiNewExpression) arg);
		}
		return arg.getText(); // Fallback to raw text if no other resolution is possible
	}

}



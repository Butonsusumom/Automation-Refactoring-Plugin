package com.tsybulka.autorefactoringplugin.inspections.repeatedobjectcreation;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.tsybulka.autorefactoringplugin.inspections.CodeInspectionVisitor;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmellType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RepeatedObjectCreationVisitor extends CodeInspectionVisitor {

	private static final String NAME = InspectionsBundle.message("inspection.repeated.object.creation.display.name");
	private static final String DESCRIPTION = InspectionsBundle.message("inspection.repeated.object.creation.problem.descriptor");

	private List<ImplementationSmell> smellsList;

	private final Map<String, List<PsiNewExpression>> objectInstances = new HashMap<>();
	private static final Map<String, String> constantValues = new HashMap<>();

	public RepeatedObjectCreationVisitor(List<ImplementationSmell> smellsList) {
		this.smellsList = smellsList;
	}

	@Override
	public boolean isInspectionEnabled() {
		return settings.isScatteredFunctionalityCheck();
	}

	@Override
	public void visitLocalVariable(PsiLocalVariable variable) {
		super.visitLocalVariable(variable);
		if (variable.getInitializer() instanceof PsiLiteralExpression) {
			constantValues.put(variable.getName(), variable.getInitializer().getText());
		} else if (variable.getInitializer() instanceof PsiReferenceExpression) {
			PsiReferenceExpression ref = (PsiReferenceExpression) variable.getInitializer();
			if (constantValues.containsKey(ref.getReferenceName())) {
				constantValues.put(variable.getName(), constantValues.get(ref.getReferenceName()));
			}
		}
	}

	@Override
	public void visitNewExpression(PsiNewExpression expression) {
		super.visitNewExpression(expression);
		String objectKey = createObjectKey(expression);
		if (objectKey != null) {
			objectInstances.computeIfAbsent(objectKey, k -> new ArrayList<>()).add(expression);
		}
	}

	public static String createObjectKey(PsiNewExpression expression) {
		PsiJavaCodeReferenceElement classReference = expression.getClassReference();
		if (classReference == null) return null;

		String className = classReference.getQualifiedName();
		PsiExpression[] args = expression.getArgumentList().getExpressions();
		StringBuilder keyBuilder = new StringBuilder(className).append("(");

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
			if (constantValues.containsKey(refName)) {
				return constantValues.get(refName);
			}
		} else if (arg instanceof PsiNewExpression) {
			return createObjectKey((PsiNewExpression) arg);
		}
		return arg.getText(); // Fallback to raw text if no other resolution is possible
	}

	@Override
	public void visitClass(@NotNull PsiClass psiClass) {
		if (isInspectionEnabled()) {
			super.visitClass(psiClass);
			psiClass.accept(new PsiRecursiveElementVisitor() {
				@Override
				public void visitElement(@NotNull PsiElement element) {
					super.visitElement(element); // Call the parent class's visitElement method
					RepeatedObjectCreationVisitor.this.visitElement(element); // Handle any additional logic
				}
			});
			objectInstances.values().forEach(expressions -> {
				// Filter out changed elements and constants
				List<PsiNewExpression> unchangedElements = new ArrayList<>();
				for (PsiNewExpression expression : expressions) {
					if (isUnchanged(expression) && !isConstant(expression)) {
						unchangedElements.add(expression);
					}
				}

				// Register smell only if more than one unchanged element is left
				if (unchangedElements.size() > 1) {
					unchangedElements.stream().filter(x -> !isAssignedToConstant(x)).forEach(this::registerSmell);
				}
			});
			objectInstances.clear();
			constantValues.clear();
		}
	}

	private boolean isAssignedToConstant(PsiNewExpression expression) {
		PsiElement parent = expression.getParent();

		// Check if the parent is a field declaration
		if (parent instanceof PsiField) {
			PsiField field = (PsiField) parent;
			return field.hasModifierProperty(PsiModifier.FINAL) && field.hasInitializer();
		}

		// Check if the parent is a local variable declaration
		if (parent instanceof PsiVariable) {
			PsiVariable variable = (PsiVariable) parent;
			return variable.hasModifierProperty(PsiModifier.FINAL) && variable.hasInitializer();
		}

		// Otherwise, the expression is not assigned to a constant
		return false;
	}

	private boolean isUnchanged(PsiElement element) {
		if (element instanceof PsiLocalVariable) {
			PsiLocalVariable variable = (PsiLocalVariable) element;
			PsiElement nextSibling = variable.getNextSibling();
			while (nextSibling != null) {
				if (nextSibling instanceof PsiExpressionStatement) {
					PsiExpressionStatement expressionStatement = (PsiExpressionStatement) nextSibling;
					PsiExpression expression = expressionStatement.getExpression();

					// Check for assignment to the variable
					if (expression instanceof PsiAssignmentExpression) {
						PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression) expression;
						if (isAssignmentToVariable(variable, assignmentExpression)) {
							return false;
						}
					}

					// Check for method calls on the variable
					else if (expression instanceof PsiMethodCallExpression) {
						PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) expression;
						if (isMethodCallOnVariable(variable, methodCallExpression)) {
							return false;
						}
					}
				}
				nextSibling = nextSibling.getNextSibling();
			}
		}
		return true;
	}

	private boolean isAssignmentToVariable(PsiLocalVariable variable, PsiAssignmentExpression assignmentExpression) {
		if (assignmentExpression.getLExpression() instanceof PsiReferenceExpression) {
			PsiReferenceExpression leftExpression = (PsiReferenceExpression) assignmentExpression.getLExpression();
			return leftExpression.resolve() == variable;
		}
		return false;
	}

	private boolean isMethodCallOnVariable(PsiLocalVariable variable, PsiMethodCallExpression methodCallExpression) {
		PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
		PsiElement resolvedElement = methodExpression.resolve();

		if (resolvedElement instanceof PsiMethod) {
			PsiMethod method = (PsiMethod) resolvedElement;
			if (!method.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
				if (methodExpression.getQualifierExpression() instanceof PsiReferenceExpression) {
					PsiReferenceExpression qualifier = (PsiReferenceExpression) methodExpression.getQualifierExpression();
					return qualifier.resolve() == variable;
				}
			}
		}
		return false;
	}

	private boolean isConstant(PsiElement element) {
		if (element instanceof PsiField) {
			PsiField field = (PsiField) element;
			return field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.FINAL);
		} else if (element instanceof PsiReferenceExpression) {
			PsiElement resolved = ((PsiReferenceExpression) element).resolve();
			return resolved instanceof PsiField && isConstant(resolved);
		}
		return false;
	}

	void registerSmell(PsiElement element) {
		PsiClass containingClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
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

		PsiMethod containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
		if (containingMethod != null) {
			methodName = containingMethod.getName();
		}

		smellsList.add(new ImplementationSmell(NAME, packageName, DESCRIPTION, ImplementationSmellType.REPEATED_OBJECT_CREATION, element, className, methodName));
	}
}

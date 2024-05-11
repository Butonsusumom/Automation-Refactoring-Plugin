package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.util.Query;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.attributes.LengthyMetrics;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tsybulka.autorefactoringplugin.inspections.longmethod.LongMethodVisitor.*;

public class LongMethodFix implements LocalQuickFix {

	private static final String FIX_MESSAGE = InspectionsBundle.message("inspection.long.method.use.quickfix");

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return FIX_MESSAGE;
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
		ApplicationManager.getApplication().invokeLater(() -> {
			PsiMethod element = (PsiMethod) problemDescriptor.getPsiElement();
			LengthyMetrics originalLengthyMetrics = new LengthyMetrics(
					getLinesOfCode(element),
					getNumberOfParameters(element),
					calculateNestingDepth(element));
			boolean refactored;
			if (originalLengthyMetrics.getLoc() > MAX_LOC) {
				refactored = refactor(element, "loc");
			} else if (originalLengthyMetrics.getMaxNestingDepth() > MAX_NESTING) {
				refactored = refactor(element, "nestingDepth");
			} else {
				refactored = refactorMethod(element);
			}
			if (refactored) {
				LengthyMetrics newLengthyMetrics = new LengthyMetrics(getLinesOfCode(element), getNumberOfParameters(element), calculateNestingDepth(element));
				LongMethodDialogsProvider.showComplexityComparisonDialog(project, originalLengthyMetrics, newLengthyMetrics);
			}

		});
	}

	public boolean refactorMethod(PsiMethod method) {
		Project project = method.getProject();
		boolean shouldRefactor = LongMethodDialogsProvider.showStartExtractParametersDialog(project);
		if (shouldRefactor) {
			PsiDirectory directory = method.getContainingClass().getContainingFile().getContainingDirectory();

			PsiParameterList oldParameters = method.getParameterList();
			if (method.getParameterList().getParameters().length <= 3) {
				return false;  // Not enough parameters to refactor
			}

			PsiElementFactory factory = PsiElementFactory.getInstance(project);

			String defaultClassName = Character.toUpperCase(method.getName().charAt(0)) + method.getName().substring(1) + "Parameters";
			Messages.InputDialog dialog = new Messages.InputDialog(
					project,
					"Enter the new class name for the parameters:",
					"Refactor Method Parameters",
					Messages.getQuestionIcon(),
					defaultClassName,
					new ClassNameValidator(project, directory));

			dialog.show();
			if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
				WriteCommandAction.runWriteCommandAction(project, () -> {
					String className = dialog.getInputString();
					//generateConstructorsAndGetters(paramClass, factory);
					PsiParameter newParam = factory.createParameter("params", PsiType.getTypeByName(className, project, GlobalSearchScope.allScope(project)));
					method.getParameterList().replace(factory.createParameterList(new String[]{newParam.getName()}, new PsiType[]{newParam.getType()}));

					updateMethodBody(method, oldParameters.getParameters(), newParam.getName());
					updateMethodCalls(method, className, factory, project);
				});
			}

			return true;  // Refactoring was initiated
		}
		return false;
	}

	private PsiClass createParameterClass(PsiElementFactory factory, PsiDirectory directory, String className, PsiParameter[] parameters) {
		PsiClass paramClass = factory.createClass(className);
		for (PsiParameter parameter : parameters) {
			PsiField field = factory.createField(parameter.getName(), parameter.getType());
			paramClass.add(field);
		}
		generateConstructorsAndGetters(paramClass, factory);
		directory.add(paramClass);
		return paramClass;
	}

	private void generateConstructorsAndGetters(PsiClass paramClass, PsiElementFactory factory) {
		PsiMethod defaultConstructor = factory.createConstructor();
		PsiMethod fullConstructor = factory.createConstructor();

		for (PsiField field : paramClass.getFields()) {
			// Create getters for each field
			PsiMethod getter = factory.createMethod("get" + capitalize(field.getName()), field.getType());
			getter.getBody().add(factory.createStatementFromText("return this." + field.getName() + ";", getter));
			paramClass.add(getter);

			// Create constructor parameters and assign them
			fullConstructor.getParameterList().add(factory.createParameter(field.getName(), field.getType()));
			String assignmentText = "this." + field.getName() + " = " + field.getName() + ";";
			fullConstructor.getBody().add(factory.createStatementFromText(assignmentText, fullConstructor));
		}

		// Add constructors to the class
		paramClass.add(defaultConstructor);
		paramClass.add(fullConstructor);
	}

	private void updateMethodBody(PsiMethod method, PsiParameter[] oldParameters, String newParameterName) {
		PsiCodeBlock methodBody = method.getBody();
		if (methodBody != null) {
			// Create a map of parameter name replacements
			Map<String, String> replacements = Arrays.stream(oldParameters)
					.collect(Collectors.toMap(PsiParameter::getName, p -> newParameterName + ".get" + capitalize(p.getName()) + "()"));

			// Process each statement in the method body
			Arrays.stream(methodBody.getStatements())
					.forEach(statement -> replaceVariableUsages(statement, replacements));
		}
	}

	private void replaceVariableUsages(PsiElement element, Map<String, String> replacements) {
		if (element instanceof PsiReferenceExpression) {
			PsiReferenceExpression refExpr = (PsiReferenceExpression) element;
			String refName = refExpr.getReferenceName();
			if (replacements.containsKey(refName)) {
				PsiExpression newExpression = JavaPsiFacade.getElementFactory(element.getProject())
						.createExpressionFromText(replacements.get(refName), element);
				refExpr.replace(newExpression);
			}
		}

		// Recursively handle all children of this element
		for (PsiElement child : element.getChildren()) {
			replaceVariableUsages(child, replacements);
		}
	}


	/**
	 * Updates all calls to a specified method, replacing the entire method call with a new instantiation of a given class.
	 *
	 * @param project   the current project
	 * @param method    the method whose calls will be updated
	 * @param className the class name to instantiate in the method call
	 * @param factory   the PsiElementFactory for creating new PSI elements
	 */
	private void updateMethodCalls(PsiMethod method, String className, PsiElementFactory factory, Project project) {
			ApplicationManager.getApplication().runReadAction(() -> {
				// Search for all usages of the method in the project scope
				Query<PsiReference> query = MethodReferencesSearch.search(method, GlobalSearchScope.projectScope(project), true);
				for (PsiReference reference : query) {
					PsiElement element = reference.getElement();
						PsiElement parent = element.getParent();
						if (parent instanceof PsiMethodCallExpression) {
							WriteCommandAction.runWriteCommandAction(project, () -> {
								PsiMethodCallExpression methodCall = (PsiMethodCallExpression) parent;
								PsiExpressionList argumentList = methodCall.getArgumentList();
								PsiExpression[] oldArguments = argumentList.getExpressions();

								// Build the new method call as a string
								StringBuilder newMethodCallBuilder = new StringBuilder(methodCall.getMethodExpression().getReferenceName()).append("(new ").append(className);
								newMethodCallBuilder.append("(");
								for (int i = 0; i < oldArguments.length; i++) {
									if (i > 0) newMethodCallBuilder.append(", ");
									newMethodCallBuilder.append(oldArguments[i].getText());
								}
								newMethodCallBuilder.append("))");

								// Create the new method call expression
								PsiExpression newMethodCall = factory.createExpressionFromText(newMethodCallBuilder.toString(), null);

								// Replace the old method call with the new one
								methodCall.replace(newMethodCall);
							});
						}
					}
			});
	}


	private String capitalize(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	private boolean refactor(PsiElement element, String type) {
		Project project = element.getProject();
		boolean shouldRefactor = LongMethodDialogsProvider.showStartDialog(project);
		if (shouldRefactor) {
			int maxComplexity = 1;
			PsiElement complexElement = null;
			final int totalComplexity = getComplexity(element, type);
			PsiElement[] childrenElement = getChildren(element);

			for (PsiElement child : childrenElement) {
				int complexity = getComplexity(child, type);
				if (complexity > maxComplexity) {
					complexElement = child;
					maxComplexity = complexity;
				}
			}

			if (complexElement == null) {
				return false;
			}

			if (maxComplexity >= totalComplexity) {
				return refactor(complexElement, type);
			} else {
				PsiElementLengthExtractVisitor extractVisitor = new PsiElementLengthExtractVisitor();
				complexElement.accept(extractVisitor);
				return extractVisitor.isRefactored();
			}
		}
		return false;
	}

	private int getComplexity(PsiElement element, String type) {
		int complexity = 1;
		switch (type) {
			case "loc":
				complexity = getLinesOfCode(element);
				break;
			case "nestingDepth":
				complexity = calculateNestingDepth(element);
				break;
		}
		return complexity;
	}

	private PsiElement[] getChildren(PsiElement element) {
		PsiElement[] childrenElement;
		if (element instanceof PsiIfStatement) {
			PsiIfStatement ifStatement = (PsiIfStatement) element;
			childrenElement = ArrayUtils.addAll(ifStatement.getThenBranch().getChildren(), ifStatement.getElseBranch().getChildren());
			childrenElement = ArrayUtils.add(childrenElement, ifStatement.getCondition());
		} else if (element instanceof PsiMethodCallExpression) {
			PsiMethodCallExpression expression = (PsiMethodCallExpression) element;
			childrenElement = expression.getArgumentList().getExpressions();
		} else {
			childrenElement = element.getChildren();
		}
		return childrenElement;
	}
}

package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.BaseCodeInspectionQuickFix;
import com.tsybulka.autorefactoringplugin.util.messagebundles.DialoguesBundle;
import com.tsybulka.autorefactoringplugin.util.messagebundles.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.util.validator.MethodNameValidator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * QuickFix implementation to rename test methods according to the specified naming convention.
 * This fix provides a dialog for the user to enter a new method name.
 */
public class TestMethodNamingFix extends BaseCodeInspectionQuickFix {

	private static final String RENAME_TEST_METHOD_DIALOGUE_MESSAGE =DialoguesBundle.message("dialogue.rename.test.method.message");
	private static final String RENAME_TEST_METHOD_DIALOGUE_TITLE =DialoguesBundle.message("dialogue.rename.test.method.title");

	private MethodNameValidator methodNameValidator = new MethodNameValidator();

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return InspectionsBundle.message("inspection.test.method.name.use.quickfix");
	}

	/**
	 * Applies the quick fix by showing a dialog to the user to rename the test method.
	 *
	 * @param project    The current project.
	 * @param descriptor The problem descriptor for the method that needs to be renamed.
	 */
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		PsiIdentifier methodIdentifier = (PsiIdentifier) descriptor.getPsiElement();
		PsiMethod method = (PsiMethod) methodIdentifier.getParent();

		if (method != null) {
			showRenameDialog(project, method);
		}
	}

	/**
	 * Displays an input dialog to allow the user to enter a new method name.
	 *
	 * @param project The current project.
	 * @param method  The method to be renamed.
	 */
	private void showRenameDialog(@NotNull Project project, @NotNull PsiMethod method) {
		String namingRegExp = settings.getTestMethodNamingRegExp();

		ApplicationManager.getApplication().invokeLater(() -> {
			Messages.InputDialog dialog = createInputDialog(project, namingRegExp);

			dialog.show();
			if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
				renameMethod(project, method, dialog.getInputString());
			}
		});
	}

	/**
	 * Creates an input dialog for renaming the method.
	 *
	 * @param project      The current project.
	 * @param namingRegExp The regular expression for valid naming conventions.
	 */
	private Messages.InputDialog createInputDialog(@NotNull Project project, String namingRegExp) {
		return new Messages.InputDialog(
				project,
				RENAME_TEST_METHOD_DIALOGUE_MESSAGE,
				RENAME_TEST_METHOD_DIALOGUE_TITLE,
				Messages.getQuestionIcon(),
				namingRegExp,
				methodNameValidator
		);
	}

	/**
	 * Renames the specified method to the new name provided by the user.
	 *
	 * @param project The current project.
	 * @param method  The method to rename.
	 * @param newName The new name for the method.
	 */
	private void renameMethod(@NotNull Project project, @NotNull PsiMethod method, String newName) {
		if (newName != null && !newName.isEmpty()) {
			WriteCommandAction.runWriteCommandAction(project, () -> {
				method.setName(newName);
			});
		}
	}
}

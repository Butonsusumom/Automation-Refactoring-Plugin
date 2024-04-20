package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.tsybulka.autorefactoringplugin.inspections.InspectionsBundle;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class TestMethodNamingFix implements LocalQuickFix {

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return InspectionsBundle.message("inspection.test.method.name.use.quickfix");
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		String namingRegExp = PluginSettings.getInstance().getTestMethodNamingRegExp();

		PsiIdentifier methodIdentifier = (PsiIdentifier) descriptor.getPsiElement();
		PsiMethod method = (PsiMethod) methodIdentifier.getParent();
		if (method != null) {
			ApplicationManager.getApplication().invokeLater(() -> {
				Messages.InputDialog dialog = new Messages.InputDialog(
						project,
						"Enter the new name for the method:",
						"Rename Test Method",
						Messages.getQuestionIcon(),
						namingRegExp,
						new MethodNameValidator());

				dialog.show();
				if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
					WriteCommandAction.runWriteCommandAction(project, () -> {
						String newName = dialog.getInputString();
						if (newName != null && !newName.isEmpty()) {
							method.setName(newName);
						}
					});
				}
			});
		}
	}
}

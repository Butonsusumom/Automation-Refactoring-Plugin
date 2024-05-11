package com.tsybulka.autorefactoringplugin.inspections.longmethod;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.attributes.LengthyMetrics;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.dialog.IdentifyElementForRefacrotingDialog;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.dialog.LengthyComparisonDialog;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.dialog.StartExtractParametersRefactoringDialog;
import com.tsybulka.autorefactoringplugin.inspections.longmethod.dialog.StartRefactoringDialog;

public class LongMethodDialogsProvider {

	public static boolean showStartDialog(Project project) {
		//disable for demo
		if (PropertiesComponent.getInstance().getBoolean(StartRefactoringDialog.DISMISSED)) {
			return true;
		}
		StartRefactoringDialog dialog = new StartRefactoringDialog(project);
		dialog.show();
		return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
	}

	public static boolean showStartExtractParametersDialog(Project project) {
		//disable for demo
		if (PropertiesComponent.getInstance().getBoolean(StartRefactoringDialog.DISMISSED)) {
			return true;
		}
		StartExtractParametersRefactoringDialog dialog = new StartExtractParametersRefactoringDialog(project);
		dialog.show();
		return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
	}

	public static boolean showIdentifyComplexElementDialog(Project project, PsiElement element) {
		IdentifyElementForRefacrotingDialog dialog = new IdentifyElementForRefacrotingDialog(project, true);
		dialog.setElement(element);
		dialog.show();
		return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
	}

	public static boolean showIdentifyComplexElementsDialog(Project project, PsiElement[] elements) {
		IdentifyElementForRefacrotingDialog dialog = new IdentifyElementForRefacrotingDialog(project, false);
		dialog.setElement(elements);
		dialog.show();
		return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
	}

	public static boolean showComplexityComparisonDialog(Project project, LengthyMetrics originalMetrics, LengthyMetrics newMetrics) {
		LengthyComparisonDialog dialog = new LengthyComparisonDialog(project, true);
		dialog.setOriginalLoc(originalMetrics.getLoc());
		dialog.setOriginalNumOfParams(originalMetrics.getNumOfParams());
		dialog.setOriginalNestingDepth(originalMetrics.getMaxNestingDepth());

		dialog.setNewLoc(newMetrics.getLoc());
		dialog.setNewNumOfParams(newMetrics.getNumOfParams());
		dialog.setNewNestingDepth(newMetrics.getMaxNestingDepth());
		dialog.show();
		return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
	}
}

package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ComplexityComparisonDialog extends DialogWrapper {

	private JPanel myMainPanel;
	private JLabel originalLoc;
	private JLabel newNestingDepth;

	public ComplexityComparisonDialog(@Nullable Project project, boolean canBeParent) {
		super(project, canBeParent);
		setTitle("Cyclomatic Complexity Refactoring Outcome");
		init();
	}

	protected void init() {
		super.init();
	}

	public void setOriginalComplexity(int complexity) {
		originalLoc.setText("Original Cyclomatic Complexity: " + complexity);
	}

	public void setNewComplexity(int complexity) {
		newNestingDepth.setText("New Cyclomatic Complexity: " + complexity);
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return myMainPanel;
	}

	@NotNull
	@Override
	protected Action @NotNull [] createActions() {
		return new Action[]{getOKAction()}; // Return only the OK action
	}

}

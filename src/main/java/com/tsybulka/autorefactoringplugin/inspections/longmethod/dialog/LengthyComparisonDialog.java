package com.tsybulka.autorefactoringplugin.inspections.longmethod.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LengthyComparisonDialog extends DialogWrapper {

	private JPanel myMainPanel;
	private JLabel originalLoc;
	private JLabel originalNumOfParams;
	private JLabel originalNestingDepth;
	private JLabel newLoc;
	private JLabel newNumOfParams;
	private JLabel newNestingDepth;

	public LengthyComparisonDialog(@Nullable Project project, boolean canBeParent) {
		super(project, canBeParent);
		setTitle("Lengthy Method Refactoring Outcome");
		init();
	}

	protected void init() {
		super.init();
	}

	public void setOriginalLoc(int loc) {
		originalLoc.setText("Original Lines of Code: " + loc);
	}

	public void setOriginalNumOfParams(int numOfParams) {
		originalNumOfParams.setText("Original Number of parameters: " + numOfParams);
	}

	public void setOriginalNestingDepth(int nestingDepth) {
		originalNestingDepth.setText("Original Nesting Depth: " + nestingDepth);
	}

	public void setNewLoc(int loc) {
		newLoc.setText("New Lines of Code: " + loc);
	}

	public void setNewNumOfParams(int numOfParams) {
		newNumOfParams.setText("New Number of parameters: " + numOfParams);
	}

	public void setNewNestingDepth(int nestingDepth) {
		newNestingDepth.setText("New Nesting Depth: " + nestingDepth);
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

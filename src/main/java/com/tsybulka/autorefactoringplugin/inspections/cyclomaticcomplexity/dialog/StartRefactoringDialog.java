package com.tsybulka.autorefactoringplugin.inspections.cyclomaticcomplexity.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class StartRefactoringDialog extends DialogWrapper {

	public static String DISMISSED = "DISMISSED";

	protected final PluginSettings settings = PluginSettings.getInstance();

	public StartRefactoringDialog(Project project) {
		super(project, true);
		setTitle("Start Method Refactoring to Reduce Cyclomatic Complexity.");
		init();
	}

	protected void init() {
		super.init();

	}

	@Override
	protected JComponent createNorthPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = JBUI.insets(4, 0, 4, 4);

		//first line
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Cyclomatic complexity is a metric used to indicate the complexity of a program."), gbc);

		//second line
		gbc.gridy++;
		panel.add(new JLabel("Excessive cyclomatic complexity normally means the code is hard to understand."), gbc);

		gbc.gridwidth = 1;
		gbc.gridy = gbc.gridy + 2;
		JLabel thresholdLabel = new JLabel("Current complexity threshold: " + settings.getCyclomaticComplexity());
		panel.add(thresholdLabel, gbc);

		//third line
		gbc.gridwidth = 1;
		gbc.gridy++;
		panel.add(new JLabel("Click Start Refactoring to begin step by step method refactoring."), gbc);

		return panel;
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return null;
	}

	@NotNull
	@Override
	protected Action getOKAction() {
		Action okAction = super.getOKAction();
		okAction.putValue(Action.NAME, "Start Refactoring");
		return okAction;
	}

}

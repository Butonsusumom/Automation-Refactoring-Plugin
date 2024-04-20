package com.tsybulka.autorefactoringplugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.projectanalyses.ProjectAnalysesService;
import com.tsybulka.autorefactoringplugin.ui.ReportDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Action allows open project analyses dialog through Tools menu
 */
public class AnalyzeCodeAction extends AnAction {

	private final ProjectAnalysesService projectAnalysesService = new ProjectAnalysesService();

	@Override
	public void actionPerformed(AnActionEvent event) {
		ProjectSmellsInfo smellsInfo = projectAnalysesService.analyseProject(event.getProject());

		EventQueue.invokeLater(() -> {
			try {
				// call analyses and pass results
				ReportDialog dialog = new ReportDialog(smellsInfo);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				ImageIcon icon = new ImageIcon(getClass().getResource("/icons/pluginIcon.svg"));
				dialog.setIconImage(icon.getImage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}

package com.tsybulka.autorefactoringplugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.tsybulka.autorefactoringplugin.inspections.oopmetrics.MetricsCalculationService;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ClassMetrics;
import com.tsybulka.autorefactoringplugin.ui.ReportDialog;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Action allows open project analyses dialog through Tools menu
 */
public class AnalyzeCodeAction extends AnAction {

	private final MetricsCalculationService metricsCalculationService = new MetricsCalculationService();

	@Override
	public void actionPerformed(AnActionEvent event) {
		// TODO: delete this stubbing
		List<ClassMetrics> projectClassMetrics = metricsCalculationService.calculateProjectMetrics(event.getProject());
		ProjectSmellsInfo smellsInfo = ProjectSmellsInfo.builder().totalImplementationSmells(8).totalTestSmells(2).totalArchitectureSmells(10).classMetricsList(projectClassMetrics)
				.architectureSmellList(Collections.singletonList(ArchitectureSmell.builder().name("Cool smell name").smellType("Cool smell type").classPackage("package.class").description("Some text text text text").build())).build();

		EventQueue.invokeLater(() -> {
			try {
				// call analyses and pass results
				ReportDialog dialog = new ReportDialog(smellsInfo);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				ImageIcon icon = new ImageIcon(getClass().getResource("/icons/pluginIcon.png"));
				dialog.setIconImage(icon.getImage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}

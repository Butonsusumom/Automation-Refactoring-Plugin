package com.tsybulka.autorefactoringplugin.ui;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.architecture.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.implementation.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.test.TestSmell;
import com.tsybulka.autorefactoringplugin.ui.component.MetricBarChartService;
import com.tsybulka.autorefactoringplugin.ui.component.MetricPieChartService;
import com.tsybulka.autorefactoringplugin.ui.component.SmellTableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportDialogUnitTest {

	private ReportDialog reportDialog;

	private MockedStatic<ApplicationManager> mockedApplicationManager;
	private MockedStatic<EditorColorsManager> mockedEditorColorsManager;

	@BeforeEach
	void setUp() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		mockedEditorColorsManager = mockStatic(EditorColorsManager.class);

		Application mockApplication = mock(Application.class);
		EditorColorsManager mockEditorColorsManagerInstance = mock(EditorColorsManager.class);
		EditorColorsScheme mockEditorColorsScheme = mock(EditorColorsScheme.class);

		//when(mockApplication.getService(EditorColorsManager.class)).thenReturn(mockEditorColorsManagerInstance);
		when(mockEditorColorsManagerInstance.getGlobalScheme()).thenReturn(mockEditorColorsScheme);
		when(mockEditorColorsScheme.getDefaultForeground()).thenReturn(Color.BLACK);

		mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);
		mockedEditorColorsManager.when(EditorColorsManager::getInstance).thenReturn(mockEditorColorsManagerInstance);

		reportDialog = new ReportDialog(new ProjectSmellsInfo());
		MetricBarChartService realBarChartService = new MetricBarChartService();
		MetricPieChartService realPieChartService = new MetricPieChartService();
		SmellTableService realSmellTableService = new SmellTableService();

		reportDialog.barChartService = realBarChartService;
		reportDialog.pieChartService = realPieChartService;
		reportDialog.smellTableService = realSmellTableService;
	}

	@AfterEach
	void tearDown() {
		mockedApplicationManager.close();
		mockedEditorColorsManager.close();
	}

	@Test
	void testProjectLevelSmellsBtnActionListener() {
		reportDialog.projectLevelSmellsBtnActionListener();
		assertEquals(UiBundle.message("report.title.project.smells"), reportDialog.getTitleLabel().getText());
	}

	@Test
	void testImplementationSmellBtnActionListener() {
		reportDialog.implementationSmellBtnActionListener();

		assertEquals(UiBundle.message("report.title.implementation.smells"), reportDialog.getTitleLabel().getText());
	}

	@Test
	void testArchitectureSmellBtnActionListener() {
		reportDialog.architectureSmellBtnActionListener();

		assertEquals(UiBundle.message("report.title.architecture.smells"), reportDialog.getTitleLabel().getText());
	}

	@Test
	void testTestSmellBtnActionListener() {
		reportDialog.testSmellBtnActionListener();

		assertEquals(UiBundle.message("report.title.test.smells"), reportDialog.getTitleLabel().getText());
	}

	@Test
	void testCreateUiPanels() {
		reportDialog.createUiPanels();

		assertNotNull(reportDialog.getLeftPanel());
		assertNotNull(reportDialog.getRightPanel());
		assertNotNull(reportDialog.getTopPanel());
		assertNotNull(reportDialog.getBottomLeftPanel());
		assertNotNull(reportDialog.getBottomRightPanel());
		assertNotNull(reportDialog.getChartPanel());
	}

	@Test
	void testCreateBarChart() {
		reportDialog.createBarChart();

		assertNotNull(reportDialog.getBottomRightPanel());
	}

	@Test
	void testCreatePieChart() {
		List<Number> chartData = Arrays.asList(1, 2, 3);
		List<String> chartLabels = Arrays.asList("A", "B", "C");

		reportDialog.createPieChart(chartData, chartLabels);

		assertNotNull(reportDialog.getChartPanel());
	}

	@Test
	void testShowSmellsWithFrequency() {
		HashMap<String, Integer> frequencyMap = new HashMap<>();
		frequencyMap.put("Smell A", 2);
		frequencyMap.put("Smell B", 3);

		reportDialog.showSmellsWithFrequency(frequencyMap);

		JTable table = (JTable) ((JScrollPane) reportDialog.getBottomRightPanel().getComponent(0)).getViewport().getView();
		assertEquals(2, table.getRowCount());
	}
}

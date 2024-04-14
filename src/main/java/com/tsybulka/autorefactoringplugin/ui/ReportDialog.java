package com.tsybulka.autorefactoringplugin.ui;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ArchitectureSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ClassMetrics;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ImplementationSmell;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.TestSmell;
import com.tsybulka.autorefactoringplugin.ui.component.MetricBarChartService;
import com.tsybulka.autorefactoringplugin.ui.component.MetricPieChartService;
import com.tsybulka.autorefactoringplugin.ui.component.SmellTableService;
import com.tsybulka.autorefactoringplugin.ui.component.TextAreaRenderer;
import org.knowm.xchart.*;

public class ReportDialog extends JDialog {

	private static final String SMELL_TYPE_COLUMN = UiBundle.message("report.table.column.smell.type");
	private static final String FREQUENCY_COLUMN = UiBundle.message("report.table.column.frequency");
	private static final String PROJECT_SMELLS_TITLE = UiBundle.message("report.title.project.smells");
	private static final String ARCHITECTURE_SMELLS_TITLE = UiBundle.message("report.title.architecture.smells");
	private static final String IMPLEMENTATION_SMELLS_TITLE = UiBundle.message("report.title.implementation.smells");
	private static final String TEST_SMELLS_TITLE = UiBundle.message("report.title.test.smells");
	private static final String REPORT_DIALOGUE_TITLE = UiBundle.message("report.title");

	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel chartPanel;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private JPanel bottomLeftPanel;
	private JPanel bottomRightPanel;
	private JPanel bottomMostPanel;
	private JButton implementationSmellsBtn;
	private JButton testSmellsBtn;
	private JButton architectureSmellsBtn;
	private JButton projectLevelSmellsBtn;
	private JLabel titleLabel;
	private ProjectSmellsInfo smellsInfo;
	private final SmellTableService smellTableService = new SmellTableService();
	private final MetricBarChartService barChartService = new MetricBarChartService();

	private final MetricPieChartService pieChartService = new MetricPieChartService();
	private final Color editorBackgroundColor;

	public ReportDialog(ProjectSmellsInfo smellsInfo) {
		this.smellsInfo = smellsInfo;

		setModal(true);
		setResizable(true);
		setTitle(REPORT_DIALOGUE_TITLE);
		getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		editorBackgroundColor = UIManager.getColor("EditorPane.background");
		contentPanel.setBackground(editorBackgroundColor);
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(JBUI.Borders.empty(5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		createUiPanels();

		projectLevelSmellsBtn.addActionListener(e -> projectLevelSmellsBtnActionListener());
		implementationSmellsBtn.addActionListener(e -> implementationSmellBtnActionListener());
		architectureSmellsBtn.addActionListener(e -> architectureSmellBtnActionListener());
		testSmellsBtn.addActionListener(e -> testSmellBtnActionListener());

		bottomPanel = new JPanel(new GridLayout(1, 2));
		bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
		bottomPanel.add(bottomRightPanel, BorderLayout.EAST);
		rightPanel.add(topPanel, BorderLayout.NORTH);
		rightPanel.add(bottomPanel, BorderLayout.CENTER);
		rightPanel.add(bottomMostPanel, BorderLayout.SOUTH);
		getContentPane().add(leftPanel, BorderLayout.WEST);
		getContentPane().add(rightPanel, BorderLayout.CENTER);
	}

	public void projectLevelSmellsBtnActionListener() {
		titleLabel.setText(PROJECT_SMELLS_TITLE);
		createDonutChart();
		createBarChart();
		createMetricsTable();
	}

	public void implementationSmellBtnActionListener() {
		titleLabel.setText(IMPLEMENTATION_SMELLS_TITLE);
		List<ImplementationSmell> implementationSmellsList = smellsInfo.getImplementationSmellsList();
		HashMap<String, Integer> frequencyMap = new HashMap<>();
		List<Number> pieChartData = new ArrayList<>();
		List<String> pieChartLabels = new ArrayList<>();
		for (ImplementationSmell smell : implementationSmellsList) {
			String smellName = smell.getName();
			if (frequencyMap.containsKey(smellName)) {
				frequencyMap.put(smellName, frequencyMap.get(smellName) + 1);
			} else {
				frequencyMap.put(smellName, 1);
			}
		}
		for (String value : frequencyMap.keySet()) {
			pieChartData.add(frequencyMap.get(value));
			pieChartLabels.add(value);
		}
		createPieChart(pieChartData, pieChartLabels);
		showSmellsWithFrequency(frequencyMap);
		bottomMostPanel = smellTableService.showImplementationSmellsTable(implementationSmellsList, bottomMostPanel);
	}

	public void architectureSmellBtnActionListener() {
		titleLabel.setText(ARCHITECTURE_SMELLS_TITLE);
		List<ArchitectureSmell> architectureSmellList = smellsInfo.getArchitectureSmellList();
		HashMap<String, Integer> frequencyMap = new HashMap<>();
		List<Number> pieChartData = new ArrayList<>();
		List<String> pieChartLabels = new ArrayList<>();
		for (ArchitectureSmell smell : architectureSmellList) {
			String smellName = smell.getName();
			if (frequencyMap.containsKey(smellName)) {
				frequencyMap.put(smellName, frequencyMap.get(smellName) + 1);
			} else {
				frequencyMap.put(smellName, 1);
			}
		}
		for (String value : frequencyMap.keySet()) {
			pieChartData.add(frequencyMap.get(value));
			pieChartLabels.add(value);
		}
		createPieChart(pieChartData, pieChartLabels);
		showSmellsWithFrequency(frequencyMap);
		bottomMostPanel = smellTableService.showArchitectureSmellsTable(architectureSmellList, bottomMostPanel);
	}

	public void testSmellBtnActionListener() {
		titleLabel.setText(TEST_SMELLS_TITLE);
		List<TestSmell> testSmellsList = smellsInfo.getTestSmellsList();
		HashMap<String, Integer> frequencyMap = new HashMap<>();
		List<Number> pieChartData = new ArrayList<>();
		List<String> pieChartLabels = new ArrayList<>();
		for (TestSmell smell : testSmellsList) {
			String smellName = smell.getName();
			if (frequencyMap.containsKey(smellName)) {
				frequencyMap.put(smellName, frequencyMap.get(smellName) + 1);
			} else {
				frequencyMap.put(smellName, 1);
			}
		}
		for (String value : frequencyMap.keySet()) {
			pieChartData.add(frequencyMap.get(value));
			pieChartLabels.add(value);
		}
		createPieChart(pieChartData, pieChartLabels);
		showSmellsWithFrequency(frequencyMap);
		bottomMostPanel = smellTableService.showTestSmellsTable(testSmellsList, bottomMostPanel);
	}

	public void createUiPanels() {
		createLeftPanel();
		createRightPanel();
		createTopPanel();
		createBottomLeftPanel();
		createBottomRightPanel();
		createChartPanel();
		createBottomMostPanel();
	}

	public void createLeftPanel() {
		leftPanel = new JPanel();
		leftPanel.removeAll();
		leftPanel.setLayout(new GridLayout(4, 0));
		projectLevelSmellsBtn = new JButton(PROJECT_SMELLS_TITLE);
		implementationSmellsBtn = new JButton(IMPLEMENTATION_SMELLS_TITLE);
		architectureSmellsBtn = new JButton(ARCHITECTURE_SMELLS_TITLE);
		testSmellsBtn = new JButton(TEST_SMELLS_TITLE);

		Font defaultFont = new JLabel().getFont();
		Font buttonFont = new Font(defaultFont.getName(), Font.BOLD, 13);
		projectLevelSmellsBtn.setFont(buttonFont);
		implementationSmellsBtn.setFont(buttonFont);
		architectureSmellsBtn.setFont(buttonFont);
		testSmellsBtn.setFont(buttonFont);

		leftPanel.setBackground(editorBackgroundColor);
		leftPanel.add(projectLevelSmellsBtn);
		leftPanel.add(architectureSmellsBtn);
		leftPanel.add(implementationSmellsBtn);
		leftPanel.add(testSmellsBtn);
	}

	public void createRightPanel() {
		rightPanel = new JPanel();
		rightPanel.removeAll();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setBackground(editorBackgroundColor);
	}

	public void createTopPanel() {
		topPanel = new JPanel();
		topPanel.removeAll();
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		topPanel.setBackground(UIUtil.getPanelBackground());
		titleLabel = new JLabel();
		Font labelFont = titleLabel.getFont();
		titleLabel.setText(PROJECT_SMELLS_TITLE);
		titleLabel.setFont(new Font(labelFont.getName(), Font.BOLD, 24));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		topPanel.add(titleLabel, BorderLayout.CENTER);
	}

	public void createBottomLeftPanel() {
		bottomLeftPanel = new JPanel();
		bottomLeftPanel.removeAll();
		bottomLeftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		bottomLeftPanel.setBackground(editorBackgroundColor);
	}

	public void createBottomRightPanel() {
		bottomRightPanel = new JPanel();
		createBarChart();
	}

	public void createBarChart() {
		bottomRightPanel.removeAll();
		bottomRightPanel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 10));
		CategoryChart chart = barChartService.getBarChart(smellsInfo);
		XChartPanel xChartPanel = new XChartPanel(chart);
		bottomRightPanel.add(xChartPanel, BorderLayout.CENTER);
	}

	public void createChartPanel() {
		chartPanel = new JPanel();
		createDonutChart();
	}

	public void createDonutChart() {
		chartPanel.removeAll();
		chartPanel.setLayout(new BorderLayout());
		PieChart chart = pieChartService.getDonutPieChart(smellsInfo);
		XChartPanel xChartPanel = new XChartPanel(chart);
		chartPanel.add(xChartPanel);
		bottomLeftPanel.setLayout(new BorderLayout());
		bottomLeftPanel.add(chartPanel, BorderLayout.CENTER);
	}

	public void createPieChart(List<Number> chartData, List<String> chartLabels) {
		chartPanel.removeAll();
		PieChart chart = pieChartService.getPieChart(chartData, chartLabels);
		XChartPanel xChartPanel = new XChartPanel(chart);
		chartPanel.add(xChartPanel);
	}

	public void createBottomMostPanel() {
		bottomMostPanel = new JPanel();
		createMetricsTable();
	}

	public void createMetricsTable() {
		bottomMostPanel.removeAll();
		bottomMostPanel.setLayout(new BorderLayout());
		bottomMostPanel.setBackground(editorBackgroundColor);
		bottomRightPanel.setBorder(BorderFactory.createEmptyBorder(-10, 0, 0, 10));
		List<ClassMetrics> classMetricsList = smellsInfo.getClassMetricsList();
		bottomMostPanel = smellTableService.showClassMetricsTable(classMetricsList, bottomMostPanel);
	}

	public void showSmellsWithFrequency(HashMap<String, Integer> frequencyMap) {
		JTable table = new JTable() {
			public boolean editCellAt(int row, int column, java.util.EventObject e) {
				return false;
			}
		};
		table.setBackground(UIUtil.getPanelBackground());
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.setDefaultRenderer(Object.class, centerRenderer);
		bottomRightPanel.removeAll();
		bottomRightPanel.setBorder(BorderFactory.createEmptyBorder(100, 20, 0, 10));
		DefaultTableModel model = new DefaultTableModel();
		table.setModel(model);
		model.addColumn(SMELL_TYPE_COLUMN);
		model.addColumn(FREQUENCY_COLUMN);

		table.getColumn(SMELL_TYPE_COLUMN).setPreferredWidth(250);
		table.getColumn(SMELL_TYPE_COLUMN).setCellRenderer(new TextAreaRenderer());

		if (frequencyMap.keySet().isEmpty()) {
			Object[] row = new Object[1];
			row[0] = "No smells detected for this type";
			model.addRow(row);
		}
		for (String value : frequencyMap.keySet()) {
			Object[] row = new Object[2];
			row[0] = value;
			row[1] = frequencyMap.get(value);
			model.addRow(row);
		}

		JScrollPane scrollPane = new JBScrollPane(table);
		scrollPane.setBackground(UIUtil.getPanelBackground());
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		bottomRightPanel.setBackground(UIUtil.getPanelBackground());
		bottomRightPanel.setLayout(new BorderLayout());
		bottomRightPanel.add(scrollPane, BorderLayout.CENTER);
	}
}

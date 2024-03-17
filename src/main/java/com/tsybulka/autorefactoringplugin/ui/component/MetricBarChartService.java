package com.tsybulka.autorefactoringplugin.ui.component;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.util.ui.UIUtil;
import com.tsybulka.autorefactoringplugin.model.metric.ClassMetricType;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import com.tsybulka.autorefactoringplugin.model.smell.codesmell.ClassMetrics;
import com.tsybulka.autorefactoringplugin.ui.UiBundle;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class used to create BarChart components for code analyses report
 */
public class MetricBarChartService {

	private static final String SMELL_DENSITY_TITLE = UiBundle.message("report.chart.bar.title.smell.density");

	public CategoryChart getBarChart(ProjectSmellsInfo smellsInfo) {

		CategoryChart chart = new CategoryChartBuilder().width(300).height(300).title(SMELL_DENSITY_TITLE).xAxisTitle("").yAxisTitle("").build();

		Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
		Color backgroundColor = UIUtil.getPanelBackground();
		Color[] sliceColors = new Color[]{
				new Color(34, 42, 134),
				new Color(58, 65, 148),
				new Color(83, 89, 161),
				new Color(107, 113, 175),
				new Color(132, 137, 188)
		};

		java.util.List<ClassMetrics> classMetrics = smellsInfo.getClassMetricsList();
		java.util.List<String> xData = Arrays.stream(SmellType.values()).map(SmellType::getValue).collect(Collectors.toList());
		java.util.List<Double> yData = new ArrayList<>();

		int totalLOC = getTotalLOC(classMetrics);
		yData.add(calculateSmellDensity(smellsInfo.getTotalImplementationSmells(), totalLOC));
		yData.add(calculateSmellDensity(smellsInfo.getTotalArchitectureSmells(), totalLOC));
		yData.add(calculateSmellDensity(smellsInfo.getTotalTestSmells(), totalLOC));

		chart.addSeries(SMELL_DENSITY_TITLE, xData, yData);

		chart.getStyler().setSeriesColors(sliceColors);
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
		chart.getStyler().setPlotBackgroundColor(backgroundColor);
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setToolTipBackgroundColor(backgroundColor);
		chart.getStyler().setToolTipBorderColor(annotationColor);
		chart.getStyler().setToolTipHighlightColor(backgroundColor);
		chart.getStyler().setPlotBorderColor(backgroundColor);
		chart.getStyler().setToolTipsEnabled(true);
		chart.getStyler().setAnnotationLineColor(annotationColor);
		chart.getStyler().setAnnotationTextPanelBackgroundColor(backgroundColor);
		chart.getStyler().setChartBackgroundColor(backgroundColor);
		chart.getStyler().setXAxisLabelRotation(35);
		chart.getStyler().setAnnotationTextFontColor(annotationColor);
		chart.getStyler().setLabelsFontColor(annotationColor);
		chart.getStyler().setAxisTickLabelsColor(annotationColor);
		chart.getStyler().setAxisTickMarksColor(annotationColor);
		chart.getStyler().setMarkerSize(8);
		chart.getStyler().setChartFontColor(annotationColor);
		chart.getStyler().setPlotGridVerticalLinesVisible(false);
		chart.getStyler().setPlotGridHorizontalLinesVisible(false);

		return chart;
	}

	/**
	 * Calculates code smell type density
	 *
	 * @param totalSmell - total number of smells
	 * @param totalLOC - total number of lines
	 */
	private Double calculateSmellDensity(int totalSmell, int totalLOC) {
		if (totalLOC == 0) return 0.0; // Avoid division by zero

		double result = (double) (totalSmell * 100) / (double) totalLOC;
		return Math.round(result * 100.0) / 100.0;
	}

	private int getTotalLOC(List<ClassMetrics> classMetrics) {
		int totalLOC = 0;
		for (ClassMetrics classMetric : classMetrics) {
			totalLOC += classMetric.getMetrics().get(ClassMetricType.LINES_OF_CODE);
		}
		return totalLOC;
	}
}

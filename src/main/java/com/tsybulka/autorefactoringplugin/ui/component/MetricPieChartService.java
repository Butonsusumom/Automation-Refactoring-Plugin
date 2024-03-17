package com.tsybulka.autorefactoringplugin.ui.component;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.util.ui.UIUtil;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;

import java.awt.*;
import java.util.List;

/**
 * Class used to create BarChart components for code analyses report
 */
public class MetricPieChartService {

	public PieChart getPieChart(List<Number> chartData, List<String> chartLabels) {
		PieChart chart = new PieChartBuilder().width(700).height(700).title("").build();

		Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
		Color backgroundColor = UIUtil.getPanelBackground();

		for (int i = 0; i < chartData.size(); i++) {
			chart.addSeries(chartLabels.get(i), chartData.get(i));
		}
		Color[] sliceColors = new Color[]{
				new Color(9, 18, 121),
				new Color(34, 42, 134),
				new Color(58, 65, 148),
				new Color(83, 89, 161),
				new Color(107, 113, 175),
				new Color(132, 137, 188),
				new Color(157, 160, 201),
				new Color(181, 184, 215),
				new Color(206, 208, 228)
		};

		PieStyler styler = chart.getStyler();

		styler.setSeriesColors(sliceColors);
		styler.setToolTipsEnabled(true);
		styler.setToolTipBackgroundColor(backgroundColor);
		styler.setToolTipBorderColor(annotationColor);
		styler.setToolTipHighlightColor(backgroundColor);
		styler.setLegendBackgroundColor(backgroundColor);
		styler.setChartBackgroundColor(backgroundColor);
		styler.setPlotBackgroundColor(backgroundColor);
		styler.setPlotBorderColor(backgroundColor);
		styler.setPlotContentSize(.9);
		styler.setLegendBorderColor(backgroundColor);
		styler.setChartFontColor(annotationColor);
		styler.setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Pie);

		return chart;
	}

	public PieChart getDonutPieChart(ProjectSmellsInfo smellsInfo) {
		PieChart chart = new PieChartBuilder().width(300).height(300).title("").build();

		Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
		Color backgroundColor = UIUtil.getPanelBackground();

		chart.addSeries(SmellType.ARCHITECTURE.getValue(), smellsInfo.getTotalArchitectureSmells());
		chart.addSeries(SmellType.IMPLEMENTATION.getValue(), smellsInfo.getTotalImplementationSmells());
		chart.addSeries(SmellType.TEST.getValue(), smellsInfo.getTotalTestSmells());

		Color[] sliceColors = new Color[]{
				new Color(34, 42, 134),
				new Color(58, 65, 148),
				new Color(83, 89, 161),
				new Color(107, 113, 175),
				new Color(132, 137, 188)
		};

		PieStyler styler = chart.getStyler();

		styler.setSeriesColors(sliceColors);
		styler.setToolTipsEnabled(true);
		styler.setToolTipBackgroundColor(backgroundColor);
		styler.setToolTipBorderColor(annotationColor);
		styler.setToolTipHighlightColor(backgroundColor);
		styler.setLegendBackgroundColor(backgroundColor);
		styler.setChartBackgroundColor(backgroundColor);
		styler.setPlotBackgroundColor(backgroundColor);
		styler.setPlotBorderColor(backgroundColor);
		styler.setPlotContentSize(.9);
		styler.setLegendVisible(true);
		styler.setLabelsFontColor(Color.RED);
		styler.setLegendBorderColor(backgroundColor);
		styler.setDonutThickness(.5);
		styler.setChartFontColor(annotationColor);
		styler.setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Donut);

		return chart;
	}
}

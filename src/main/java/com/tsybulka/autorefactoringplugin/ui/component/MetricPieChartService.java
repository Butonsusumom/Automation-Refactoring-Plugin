package com.tsybulka.autorefactoringplugin.ui.component;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.tsybulka.autorefactoringplugin.model.smell.ProjectSmellsInfo;
import com.tsybulka.autorefactoringplugin.model.smell.SmellType;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class used to create BarChart components for code analyses report
 */
public class MetricPieChartService {

	public PieChart getPieChart(List<Number> chartData, List<String> chartLabels) {
		PieChart chart = new PieChartBuilder().width(700).height(700).title("").build();

		Color annotationColor = EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
		Color backgroundColor = UIUtil.getPanelBackground();

		// Process chart labels to insert line breaks or shorten them as necessary
		List<String> processedLabels = chartLabels.stream()
				.map(label -> insertLineBreaks(label, 20)) // Assuming a max length of 20 characters per line
				.collect(Collectors.toList());

		for (int i = 0; i < chartData.size(); i++) {
			chart.addSeries(processedLabels.get(i), chartData.get(i));
		}

		JBColor[] sliceColors = new JBColor[]{
				new JBColor(new Color(95, 105, 210), new Color(34, 42, 134)),
				new JBColor(new Color(130, 130, 220), new Color(58, 65, 148)),
				new JBColor(new Color(150, 155, 230), new Color(83, 89, 161)),
				new JBColor(new Color(165, 170, 240), new Color(107, 113, 175)),
				new JBColor(new Color(180, 185, 250), new Color(132, 137, 188)),
				new JBColor(new Color(195, 200, 255), new Color(157, 160, 201)),
				new JBColor(new Color(210, 215, 255), new Color(181, 184, 215)),
				new JBColor(new Color(225, 230, 255), new Color(206, 208, 228)),
				new JBColor(new Color(240, 245, 255), new Color(220, 232, 240))
		};

		PieStyler styler = chart.getStyler();

		styler.setSeriesColors(sliceColors);
		styler.setToolTipsEnabled(true);
		styler.setToolTipBackgroundColor(backgroundColor);
		styler.setToolTipBorderColor(annotationColor);
		styler.setToolTipHighlightColor(backgroundColor);
		styler.setLegendBackgroundColor(backgroundColor);
		styler.setLegendPadding(10);
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

		JBColor[] sliceColors = new JBColor[]{
				new JBColor(new Color(95, 105, 210), new Color(34, 42, 134)),
				new JBColor(new Color(130, 130, 220), new Color(58, 65, 148)),
				new JBColor(new Color(150, 155, 230), new Color(83, 89, 161)),
				new JBColor(new Color(165, 170, 240), new Color(107, 113, 175)),
				new JBColor(new Color(180, 185, 250), new Color(132, 137, 188))
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
		styler.setLabelsFontColor(JBColor.RED);
		styler.setLegendBorderColor(backgroundColor);
		styler.setDonutThickness(.5);
		styler.setChartFontColor(annotationColor);
		styler.setDefaultSeriesRenderStyle(PieSeries.PieSeriesRenderStyle.Donut);

		return chart;
	}

	/**
	 * Inserts line breaks into a string to ensure no line exceeds maxLength.
	 * This is a simplistic approach and might break words. Consider using a more
	 * sophisticated text wrapping library for better results.
	 *
	 * @param text      - given Text
	 * @param maxLength - given max length of the text
	 */
	private String insertLineBreaks(String text, int maxLength) {
		StringBuilder sb = new StringBuilder(text);
		int i = 0;
		while ((i = sb.indexOf(" ", i + maxLength)) != -1) {
			sb.replace(i, i + 1, "\n");
		}
		return sb.toString();
	}
}

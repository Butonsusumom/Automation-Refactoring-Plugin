package com.tsybulka.autorefactoringplugin.settings;

import com.esotericsoftware.kryo.NotNull;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import lombok.Data;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

@Data
public class PluginSettingsComponent {

	private JPanel settingsPanel;
	@NotNull
	private JCheckBox scatteredFunctionalityCheckBox = new JBCheckBox("Scattered functionality check");
	@NotNull
	private JCheckBox enumComparisonCheckBox = new JBCheckBox("Enum reference comparison check");
	@NotNull
	private JCheckBox objectComparisonCheckBox = new JBCheckBox("Objects content comparison check");
	@NotNull
	private JCheckBox objectMethodParameterCheckBox = new JBCheckBox("Object method parameter check");
	@NotNull
	private JTextField cyclomaticComplexityNumericalField = new JBTextField();
	@NotNull
	private JTextField testMethodNamingRegexField = new JBTextField();
	@NotNull
	private JLabel errorCyclomaticComplexityLabel = new JBLabel("<html>Methods with cyclomatic complexity over 20 are viewed as complex, potentially impacting maintainability.<br/> Please reduce maximum allowed cyclomatic complexity.</html>");
	@NotNull
	private JLabel exampleTestMethodNamingLabel = new JBLabel("<html>Example: should.*_when.*_given.*</html>");
	@NotNull
	private JLabel errorTestMethodNamingLabel = new JBLabel("<html>Entered value is not a valid regular expression.</html>");

	public PluginSettingsComponent() {
		// Ensure numerical input only
		((AbstractDocument) cyclomaticComplexityNumericalField.getDocument()).setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				if (string.matches("\\d*")) {
					super.insertString(fb, offset, string, attr);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				if (text.matches("\\d*")) {
					super.replace(fb, offset, length, text, attrs);
				}
			}
		});

		configureLabels();

		settingsPanel = FormBuilder.createFormBuilder()
				.addComponent(new TitledSeparator("Method Complexity Settings"), 1)
				.addLabeledComponent(new JBLabel("Maximum allowed cyclomatic complexity for methods: "), cyclomaticComplexityNumericalField, 1, false)
				.addComponent(errorCyclomaticComplexityLabel, 1)
				.addComponent(new TitledSeparator("Test Naming Convention"), 1)
				.addLabeledComponent(new JBLabel("Regex for test method naming: "), testMethodNamingRegexField, 1, false)
				.addComponent(exampleTestMethodNamingLabel, 1)
				.addComponent(errorTestMethodNamingLabel, 1)
				.addComponent(new TitledSeparator("Enable/Disable Code Inspections"), 1)
				.addComponent(scatteredFunctionalityCheckBox, 1)
				.addComponent(enumComparisonCheckBox, 1)
				.addComponent(objectComparisonCheckBox, 1)
				.addComponent(objectMethodParameterCheckBox,1)
				.addComponentFillVertically(new JPanel(), 0)
				.getPanel();
	}

	public JPanel getPanel() {
		return settingsPanel;
	}

	private void configureLabels() {
		errorCyclomaticComplexityLabel.setForeground(JBColor.RED);
		errorCyclomaticComplexityLabel.setVisible(false);

		errorTestMethodNamingLabel.setForeground(JBColor.RED);
		errorTestMethodNamingLabel.setVisible(false);

		Font font = exampleTestMethodNamingLabel.getFont();
		Color color = exampleTestMethodNamingLabel.getForeground();
		Color newColor = color.brighter().brighter().brighter().brighter().brighter().brighter().brighter();

		exampleTestMethodNamingLabel.setFont(new Font(font.getName(), font.getStyle(), font.getSize()-2));
		exampleTestMethodNamingLabel.setForeground(newColor);
	}

}
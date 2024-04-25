package com.tsybulka.autorefactoringplugin.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PluginConfigurable implements Configurable {
	private PluginSettingsComponent pluginSettingsComponent;
	private final PluginSettings settings = PluginSettings.getInstance();

	@Nls(capitalization = Nls.Capitalization.Title)
	@Override
	public String getDisplayName() {
		return "AutoRefactor Plugin Settings";
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		pluginSettingsComponent = new PluginSettingsComponent();
		return pluginSettingsComponent.getPanel();
	}

	@Override
	public boolean isModified() {
		return pluginSettingsComponent.getScatteredFunctionalityCheckBox().isSelected() != settings.isScatteredFunctionalityCheck() ||
				pluginSettingsComponent.getEnumComparisonCheckBox().isSelected() != settings.isEnumComparisonCheck() ||
				pluginSettingsComponent.getObjectComparisonCheckBox().isSelected() != settings.isObjectComparisonCheck() ||
				pluginSettingsComponent.getObjectMethodParameterCheckBox().isSelected() != settings.isObjectMethodParameterCheck() ||
				!Objects.equals(Integer.parseInt(pluginSettingsComponent.getCyclomaticComplexityNumericalField().getText()), settings.getCyclomaticComplexity()) ||
				!Objects.equals(pluginSettingsComponent.getTestMethodNamingRegexField().getText(), settings.getTestMethodNamingRegExp());
	}

	@Override
	public void apply() {
		settings.setScatteredFunctionalityCheck(pluginSettingsComponent.getScatteredFunctionalityCheckBox().isSelected());
		settings.setEnumComparisonCheck(pluginSettingsComponent.getEnumComparisonCheckBox().isSelected());
		settings.setObjectComparisonCheck(pluginSettingsComponent.getObjectComparisonCheckBox().isSelected());
		settings.setObjectMethodParameterCheck(pluginSettingsComponent.getObjectMethodParameterCheckBox().isSelected());
		if (isCyclomaticComplexityValid(pluginSettingsComponent)) {
			settings.setCyclomaticComplexity(Integer.parseInt(pluginSettingsComponent.getCyclomaticComplexityNumericalField().getText()));
		} else {
			pluginSettingsComponent.getErrorCyclomaticComplexityLabel().setVisible(true);
		}
		if (isRegExpValid(pluginSettingsComponent)) {
			settings.setTestMethodNamingRegExp(pluginSettingsComponent.getTestMethodNamingRegexField().getText());
		} else {
			pluginSettingsComponent.getErrorTestMethodNamingLabel().setVisible(true);
		}
	}

	@Override
	public void reset() {
		pluginSettingsComponent.getScatteredFunctionalityCheckBox().setSelected(settings.isScatteredFunctionalityCheck());
		pluginSettingsComponent.getEnumComparisonCheckBox().setSelected(settings.isEnumComparisonCheck());
		pluginSettingsComponent.getObjectComparisonCheckBox().setSelected(settings.isObjectComparisonCheck());
		pluginSettingsComponent.getObjectMethodParameterCheckBox().setSelected(settings.isObjectMethodParameterCheck());
		pluginSettingsComponent.getCyclomaticComplexityNumericalField().setText(String.valueOf(settings.getCyclomaticComplexity()));
		pluginSettingsComponent.getTestMethodNamingRegexField().setText(settings.getTestMethodNamingRegExp());
	}

	@Override
	public void disposeUIResources() {
		pluginSettingsComponent = null;
	}

	private boolean isCyclomaticComplexityValid(PluginSettingsComponent settings) {
		return Integer.parseInt(settings.getCyclomaticComplexityNumericalField().getText()) <= 20;
	}

	private boolean isRegExpValid(PluginSettingsComponent settings) {
		try {
			Pattern.compile(settings.getTestMethodNamingRegexField().getText());
			return true;  // No exception thrown, so the regex is valid
		} catch (PatternSyntaxException e) {
			return false;  // Exception thrown, regex is invalid
		}
	}

}
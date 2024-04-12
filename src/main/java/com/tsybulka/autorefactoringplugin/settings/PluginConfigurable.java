package com.tsybulka.autorefactoringplugin.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class PluginConfigurable implements Configurable {
	private PluginSettingsComponent pluginSettingsComponent;

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
		PluginSettings settings = PluginSettings.getInstance();
		return pluginSettingsComponent.getEnumComparisonCheckBox().isSelected() != settings.isEnumComparisonCheck() ||
				pluginSettingsComponent.getObjectComparisonCheckBox().isSelected() != settings.isObjectComparisonCheck() ||
				!Objects.equals(Integer.parseInt(pluginSettingsComponent.getCyclomaticComplexityNumericalField().getText()), settings.getCyclomaticComplexity());
	}

	@Override
	public void apply() {
		PluginSettings settings = PluginSettings.getInstance();
		if (areSettingsValid(pluginSettingsComponent)) {
			settings.setEnumComparisonCheck(pluginSettingsComponent.getEnumComparisonCheckBox().isSelected());
			settings.setObjectComparisonCheck(pluginSettingsComponent.getObjectComparisonCheckBox().isSelected());
			settings.setCyclomaticComplexity(Integer.parseInt(pluginSettingsComponent.getCyclomaticComplexityNumericalField().getText()));
		} else {
			pluginSettingsComponent.getErrorCyclomaticComplexityLabel().setVisible(true);
		}
	}

	@Override
	public void reset() {
		PluginSettings settings = PluginSettings.getInstance();
		pluginSettingsComponent.getEnumComparisonCheckBox().setSelected(settings.isEnumComparisonCheck());
		pluginSettingsComponent.getObjectComparisonCheckBox().setSelected(settings.isObjectComparisonCheck());
		pluginSettingsComponent.getCyclomaticComplexityNumericalField().setText(String.valueOf(settings.getCyclomaticComplexity()));
	}

	@Override
	public void disposeUIResources() {
		pluginSettingsComponent = null;
	}

	private boolean areSettingsValid(PluginSettingsComponent settings) {
		return Integer.parseInt(settings.getCyclomaticComplexityNumericalField().getText()) <= 20;
	}

}
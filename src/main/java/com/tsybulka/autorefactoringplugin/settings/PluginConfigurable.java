package com.tsybulka.autorefactoringplugin.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

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
		return pluginSettingsComponent.getEnumComparisonCheckBox().isSelected() != settings.isEnumComparisonCheck() ||
				pluginSettingsComponent.getObjectComparisonCheckBox().isSelected() != settings.isObjectComparisonCheck() ||
				pluginSettingsComponent.getObjectMethodParameterCheckBox().isSelected() != settings.isObjectMethodParameterCheck() ||
				!Objects.equals(Integer.parseInt(pluginSettingsComponent.getCyclomaticComplexityNumericalField().getText()), settings.getCyclomaticComplexity());
	}

	@Override
	public void apply() {
		if (areSettingsValid(pluginSettingsComponent)) {
			settings.setEnumComparisonCheck(pluginSettingsComponent.getEnumComparisonCheckBox().isSelected());
			settings.setObjectComparisonCheck(pluginSettingsComponent.getObjectComparisonCheckBox().isSelected());
			settings.setObjectMethodParameterCheck(pluginSettingsComponent.getObjectMethodParameterCheckBox().isSelected());
			settings.setCyclomaticComplexity(Integer.parseInt(pluginSettingsComponent.getCyclomaticComplexityNumericalField().getText()));
		} else {
			pluginSettingsComponent.getErrorCyclomaticComplexityLabel().setVisible(true);
		}
	}

	@Override
	public void reset() {
		pluginSettingsComponent.getEnumComparisonCheckBox().setSelected(settings.isEnumComparisonCheck());
		pluginSettingsComponent.getObjectComparisonCheckBox().setSelected(settings.isObjectComparisonCheck());
		pluginSettingsComponent.getObjectMethodParameterCheckBox().setSelected(settings.isObjectMethodParameterCheck());
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
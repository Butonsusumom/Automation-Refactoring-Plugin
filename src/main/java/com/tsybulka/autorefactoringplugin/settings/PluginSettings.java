package com.tsybulka.autorefactoringplugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@State(
		name = "PluginSettings",
		storages = {@Storage("PluginSettings.xml")}
)
@Data
final public class PluginSettings implements PersistentStateComponent<PluginSettings> {

	private boolean objectComparisonCheck = true;
	private boolean enumComparisonCheck = true;
	private int cyclomaticComplexity = 10;

	@Nullable
	@Override
	public PluginSettings getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull PluginSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public static PluginSettings getInstance() {
		return ApplicationManager.getApplication().getService(PluginSettings.class);
	}
}

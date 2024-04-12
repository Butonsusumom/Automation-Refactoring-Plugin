package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.psi.JavaElementVisitor;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;

public abstract class CodeInspectionVisitor extends JavaElementVisitor {

	protected final PluginSettings settings = PluginSettings.getInstance();

	public abstract boolean isInspectionEnabled();
}

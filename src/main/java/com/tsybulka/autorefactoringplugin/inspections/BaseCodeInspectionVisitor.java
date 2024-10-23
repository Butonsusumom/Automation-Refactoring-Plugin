package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.psi.JavaElementVisitor;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;

/**
 * Base class for visitors that implement custom code inspection logic
 * in a plugin.
 * <p>
 * This abstract class extends {@link JavaElementVisitor} to provide a foundation
 * for creating specific visitors that can traverse Java elements within
 * a code inspection framework. It holds an instance of {@link PluginSettings}
 * to access the plugin's configuration settings.
 * </p>
 *
 * <h4>Usage</h4>
 * <p>
 * Subclass this class to create specific inspection visitors. Implement the
 * {@code isInspectionEnabled} method to control whether the inspection logic
 * should be executed for the visited elements.
 * </p>
 */
public abstract class BaseCodeInspectionVisitor extends JavaElementVisitor {

	protected final PluginSettings settings = PluginSettings.getInstance();

	/**
	 * Determines whether the inspection is enabled for the current context.
	 *
	 * @return {@code true} if the inspection is enabled; {@code false} otherwise.
	 */
	public abstract boolean isInspectionEnabled();
}

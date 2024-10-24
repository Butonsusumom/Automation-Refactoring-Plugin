package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for code inspection quick fixes.
 * This class implements the {@link LocalQuickFix} interface and provides
 * a common framework for creating quick fixes related to code inspections.
 * Each quick fix subclass should specify its own family name and implement
 * the logic for applying the fix.
 */
public abstract class BaseCodeInspectionQuickFix implements LocalQuickFix {

	protected final PluginSettings settings = PluginSettings.getInstance();


	/**
	 * Returns the family name of the quick fix.
	 * This name is used to group related quick fixes together.
	 *
	 * @return a string representing the family name of the quick fix.
	 */
	public abstract String getFamilyName();

	/**
	 * Applies the quick fix to the specified project based on the provided problem descriptor.
	 * This method should contain the logic to modify the code as per the fix being implemented.
	 *
	 * @param project    The current project where the fix is being applied.
	 * @param descriptor The problem descriptor that provides context about the issue to be fixed.
	 */
	public abstract void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor);
}

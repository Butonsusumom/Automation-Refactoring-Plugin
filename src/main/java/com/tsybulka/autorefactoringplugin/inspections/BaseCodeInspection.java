package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;

/**
 * Base class for custom code inspections in a plugin.
 * <p>
 * This abstract class extends {@link com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool}
 * and provides a foundation for defining Java code inspections to detect issues like code smells,
 * bugs, or coding standard violations.
 * </p>
 *
 * <h4>Usage</h4>
 * <p>
 * Extend this class and override methods such as {@code checkMethod}, {@code checkClass},
 * or {@code checkField} to implement custom inspection logic.
 * </p>
 */
public abstract class BaseCodeInspection extends AbstractBaseJavaLocalInspectionTool{

	protected abstract LocalQuickFix getQuickFix();

}

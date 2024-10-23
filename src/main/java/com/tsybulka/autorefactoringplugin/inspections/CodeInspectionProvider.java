package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

/**
 * Provides a mechanism to dynamically retrieve custom inspection classes
 * for the code inspection framework in a plugin.
 * <p>
 * This class implements the {@link InspectionToolProvider} interface,
 * allowing it to scan for all subclasses of {@link BaseCodeInspection}
 * within the specified package.
 * </p>
 */
public class CodeInspectionProvider implements InspectionToolProvider {

	/**
	 * Returns an array of classes that extend {@link BaseCodeInspection}.
	 * This method uses the Reflections library to dynamically scan the specified package
	 * for inspection classes at runtime.
	 *
	 * @return An array of classes that extend {@link BaseCodeInspection}.
	 */
	@Override
	public Class[] getInspectionClasses() {
		ClassLoader classLoader = this.getClass().getClassLoader();

		Reflections reflections = new Reflections(
				new ConfigurationBuilder()
						.addClassLoader(classLoader)
						.forPackages("com.tsybulka")
						.addScanners(new SubTypesScanner(false))
		);
		Set<Class<? extends BaseCodeInspection>> classes = reflections.getSubTypesOf(BaseCodeInspection.class);
		return classes.toArray(new Class[0]);
	}
}
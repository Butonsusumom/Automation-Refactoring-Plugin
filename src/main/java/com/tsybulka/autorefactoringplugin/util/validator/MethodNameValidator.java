package com.tsybulka.autorefactoringplugin.util.validator;

import com.intellij.openapi.ui.InputValidator;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;

import java.util.regex.Pattern;

/**
 * Validator for method names based on a specified naming convention.
 * This class implements the {@link InputValidator} interface to provide
 * validation functionality for method names against a regular expression pattern.
 */
public class MethodNameValidator implements InputValidator {
	private final Pattern pattern;

	/**
	 * Constructs a {@link MethodNameValidator} using the naming regular expression
	 * defined in the plugin settings.
	 */
	public MethodNameValidator() {
		String namingRegExp = PluginSettings.getInstance().getTestMethodNamingRegExp();
		this.pattern = Pattern.compile(namingRegExp);
	}

	/**
	 * Checks if the given input string matches the defined naming convention.
	 *
	 * @param inputString the method name to be validated.
	 * @return true if the input string matches the naming convention; false otherwise.
	 */
	@Override
	public boolean checkInput(String inputString) {
		return isValidMethodName(inputString);
	}

	/**
	 * Checks if the input string is valid and determines if the dialog can be closed.
	 *
	 * @param inputString the method name to be validated.
	 * @return true if the input string is valid; false otherwise.
	 */
	@Override
	public boolean canClose(String inputString) {
		return checkInput(inputString);
	}

	/**
	 * Validates if the provided method name matches the naming convention.
	 *
	 * @param methodName the method name to be validated.
	 * @return true if the method name matches the naming convention; false otherwise.
	 */
	private boolean isValidMethodName(String methodName) {
		return pattern.matcher(methodName).matches();
	}
}

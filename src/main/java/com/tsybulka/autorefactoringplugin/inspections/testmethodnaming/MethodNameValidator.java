package com.tsybulka.autorefactoringplugin.inspections.testmethodnaming;

import com.intellij.openapi.ui.InputValidator;
import com.tsybulka.autorefactoringplugin.settings.PluginSettings;

import java.util.regex.Pattern;

public class MethodNameValidator implements InputValidator {
	private final Pattern pattern;

	public MethodNameValidator() {
		String namingRegExp = PluginSettings.getInstance().getTestMethodNamingRegExp();
		// Define the pattern for your naming convention here
		this.pattern = Pattern.compile(namingRegExp);
	}

	@Override
	public boolean checkInput(String inputString) {
		return pattern.matcher(inputString).matches();
	}

	@Override
	public boolean canClose(String inputString) {
		return checkInput(inputString);
	}
}

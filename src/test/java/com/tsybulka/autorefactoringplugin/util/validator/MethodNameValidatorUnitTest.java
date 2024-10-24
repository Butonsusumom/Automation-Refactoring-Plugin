package com.tsybulka.autorefactoringplugin.util.validator;

import com.tsybulka.autorefactoringplugin.settings.PluginSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MethodNameValidatorUnitTest {
	private MethodNameValidator methodNameValidator;

	@BeforeEach
	void setUp() {
		MockedStatic<PluginSettings> mockedSettings = Mockito.mockStatic(PluginSettings.class);
		PluginSettings pluginSettings = mock(PluginSettings.class);

		when(PluginSettings.getInstance()).thenReturn(pluginSettings);
		when(pluginSettings.getTestMethodNamingRegExp()).thenReturn("test[A-Za-z]+");

		methodNameValidator = new MethodNameValidator();

		mockedSettings.close();
	}

	@Test
	void shouldReturnTrue_whenCheckInput_givenValidTestMethodName() {
		// given
		String validMethodName = "testValidMethod";

		// when
		boolean result = methodNameValidator.checkInput(validMethodName);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalse_whenCheckInput_givenInvalidTestMethodName() {
		// given
		String invalidMethodName = "invalidMethod";

		// when
		boolean result = methodNameValidator.checkInput(invalidMethodName);

		// then
		assertThat(result).isFalse();
	}

	@Test
	void shouldReturnTrue_whenCanClose_givenValidTestMethodName() {
		// given
		String validMethodName = "testMethodCanClose";

		// when
		boolean result = methodNameValidator.canClose(validMethodName);

		// then
		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalse_whenCanClose_givenInvalidTestMethodName() {
		// given
		String invalidMethodName = "methodCannotClose";

		// when
		boolean result = methodNameValidator.canClose(invalidMethodName);

		// then
		assertThat(result).isFalse();
	}
}
package com.tsybulka.autorefactoringplugin.settings;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginConfigurableTest {

	@Mock
	private PluginSettingsComponent mockPluginSettingsComponent;
	private static PluginSettings mockSettings;
	@Mock
	private JCheckBox mockCheckBox;
	@Mock
	private JTextField mockTextField;
	@Mock
	private JTextField mockNumberTextField;
	@Mock
	private JLabel mockErrorLabel;

	private PluginConfigurable pluginConfigurable;

	private static MockedStatic<ApplicationManager> mockedApplicationManager;

	@BeforeAll
	static void setUp() {
		mockedApplicationManager = mockStatic(ApplicationManager.class);
		Application mockApplication = mock(Application.class);
		mockSettings = mock(PluginSettings.class);

		mockedApplicationManager.when(ApplicationManager::getApplication).thenReturn(mockApplication);
		when(mockApplication.getService(PluginSettings.class)).thenReturn(mockSettings);
		when(mockSettings.isEnumComparisonCheck()).thenReturn(true);
	}

	@BeforeEach
	void init() {
		pluginConfigurable = new PluginConfigurable();
		pluginConfigurable.pluginSettingsComponent = mockPluginSettingsComponent;

	}

	@AfterAll
	static void tearDown() {
		mockedApplicationManager.close();
	}

	@Test
	void testIsModified() {

		when(mockPluginSettingsComponent.getScatteredFunctionalityCheckBox()).thenReturn(mockCheckBox);

		when(mockCheckBox.isSelected()).thenReturn(true);
		when(mockSettings.isScatteredFunctionalityCheck()).thenReturn(false);

		assertTrue(pluginConfigurable.isModified());
	}

	@Test
	void testApply() {

		when(mockPluginSettingsComponent.getScatteredFunctionalityCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getEnumComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectMethodParameterCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getLengthyMethodCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getRecurringObjectCreationCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getCyclomaticComplexityNumericalField()).thenReturn(mockNumberTextField);
		when(mockPluginSettingsComponent.getTestMethodNamingRegexField()).thenReturn(mockTextField);
		when(mockPluginSettingsComponent.getErrorCyclomaticComplexityLabel()).thenReturn(mockErrorLabel);
		when(mockPluginSettingsComponent.getErrorTestMethodNamingLabel()).thenReturn(mockErrorLabel);

		when(mockCheckBox.isSelected()).thenReturn(true);
		when(mockNumberTextField.getText()).thenReturn("10");
		when(mockTextField.getText()).thenReturn("validRegex");

		pluginConfigurable.apply();

		verify(mockSettings).setScatteredFunctionalityCheck(true);
		verify(mockSettings).setCyclomaticComplexity(10);
		verify(mockSettings, times(2)).setTestMethodNamingRegExp("validRegex");
	}

	@Test
	void testApplyWithInvalidCyclomaticComplexity() {

		when(mockPluginSettingsComponent.getScatteredFunctionalityCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getEnumComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectMethodParameterCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getLengthyMethodCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getRecurringObjectCreationCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getCyclomaticComplexityNumericalField()).thenReturn(mockNumberTextField);
		when(mockPluginSettingsComponent.getTestMethodNamingRegexField()).thenReturn(mockTextField);
		when(mockPluginSettingsComponent.getErrorCyclomaticComplexityLabel()).thenReturn(mockErrorLabel);
		when(mockPluginSettingsComponent.getErrorTestMethodNamingLabel()).thenReturn(mockErrorLabel);

		when(mockTextField.getText()).thenReturn( "validRegex");
		when(mockNumberTextField.getText()).thenReturn("30");

		pluginConfigurable.apply();

		verify(mockSettings, never()).setCyclomaticComplexity(anyInt());
		verify(mockErrorLabel).setVisible(true);
	}

	@Test
	void testApplyWithInvalidRegex() {

		when(mockPluginSettingsComponent.getScatteredFunctionalityCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getEnumComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectMethodParameterCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getLengthyMethodCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getRecurringObjectCreationCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getCyclomaticComplexityNumericalField()).thenReturn(mockNumberTextField);
		when(mockPluginSettingsComponent.getTestMethodNamingRegexField()).thenReturn(mockTextField);
		when(mockPluginSettingsComponent.getErrorCyclomaticComplexityLabel()).thenReturn(mockErrorLabel);
		when(mockPluginSettingsComponent.getErrorTestMethodNamingLabel()).thenReturn(mockErrorLabel);

		when(mockNumberTextField.getText()).thenReturn("10");
		when(mockTextField.getText()).thenReturn("validRegex");

		pluginConfigurable.apply();

		verify(mockErrorLabel, times(2)).setVisible(false);
	}

	@Test
	void testReset() {

		when(mockPluginSettingsComponent.getScatteredFunctionalityCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getEnumComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectComparisonCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getObjectMethodParameterCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getLengthyMethodCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getRecurringObjectCreationCheckBox()).thenReturn(mockCheckBox);
		when(mockPluginSettingsComponent.getCyclomaticComplexityNumericalField()).thenReturn(mockTextField);
		when(mockPluginSettingsComponent.getTestMethodNamingRegexField()).thenReturn(mockTextField);
		when(mockSettings.isScatteredFunctionalityCheck()).thenReturn(true);
		when(mockSettings.getCyclomaticComplexity()).thenReturn(10);
		when(mockSettings.getTestMethodNamingRegExp()).thenReturn("validRegex");

		pluginConfigurable.reset();

		verify(mockCheckBox, times(2)).setSelected(true);
		verify(mockTextField).setText("10");
		verify(mockTextField).setText("validRegex");
	}

	@Test
	void testCreateComponent() {

		PluginSettingsComponent expectedPluginSettingsComponent = new PluginSettingsComponent();
		JPanel expected = expectedPluginSettingsComponent.getPanel();

		JComponent result = pluginConfigurable.createComponent();

		assertEquals(result.getLayout().getClass(), expected.getLayout().getClass());
	}

	@Test
	void testDisposeUIResources() {
		pluginConfigurable.createComponent(); // Initialize the component
		pluginConfigurable.disposeUIResources();
		assertNull(pluginConfigurable.pluginSettingsComponent);
	}

	@Test
	void testIsCyclomaticComplexityValid_givenValidComplexity() {
		when(mockPluginSettingsComponent.getCyclomaticComplexityNumericalField()).thenReturn(mockNumberTextField);

		when(mockNumberTextField.getText()).thenReturn("10");
		assertTrue(pluginConfigurable.isCyclomaticComplexityValid(mockPluginSettingsComponent));
	}

	@Test
	void testIsCyclomaticComplexityValid_givenInvalidComplexity() {
		when(mockPluginSettingsComponent.getCyclomaticComplexityNumericalField()).thenReturn(mockNumberTextField);

		when(mockNumberTextField.getText()).thenReturn("30");
		assertFalse(pluginConfigurable.isCyclomaticComplexityValid(mockPluginSettingsComponent));
	}

	@Test
	void testIsRegExpValid_givenValidRegex() {
		when(mockPluginSettingsComponent.getTestMethodNamingRegexField()).thenReturn(mockTextField);

		when(mockTextField.getText()).thenReturn("validRegex");
		assertTrue(pluginConfigurable.isRegExpValid(mockPluginSettingsComponent));
	}

	@Test
	void testIsRegExpValid_givenInvalidRegex() {
		when(mockPluginSettingsComponent.getTestMethodNamingRegexField()).thenReturn(mockTextField);

		when(mockTextField.getText()).thenReturn("[invalidRegex");
		assertFalse(pluginConfigurable.isRegExpValid(mockPluginSettingsComponent));
	}
}

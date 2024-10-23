package com.tsybulka.autorefactoringplugin.util.messagebundles;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Utility class for managing localized messages in the inspection plugin.
 * <p>
 * This final class extends {@link com.intellij.DynamicBundle} and provides methods
 * for retrieving localized strings from a resource bundle. The resource bundle is specified by the
 * {@link #BUNDLE} constant, which points to the properties file containing the messages.
 * </p>
 *
 * <h4>Usage</h4>
 * <p>
 * Use the {@link #message(String, Object...)} method to obtain localized messages by passing
 * the corresponding keys and parameters. This allows for easy internationalization and localization
 * of inspection-related messages.
 * </p>
 */
public final class InspectionsBundle extends DynamicBundle {

	private static final InspectionsBundle CURRENT_INSTANCE = new InspectionsBundle();

	@NonNls
	public static final String BUNDLE = "messages.inspection";

	private InspectionsBundle() {
		super(BUNDLE);
	}

	/**
	 * Retrieves a localized message from the resource bundle.
	 *
	 * @param key The key of the message in the resource bundle.
	 * @param params Optional parameters to be substituted in the message.
	 * @return The localized message as a {@code String}.
	 */
	public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
									  Object @NotNull ... params) {
		return CURRENT_INSTANCE.getMessage(key, params);
	}

}

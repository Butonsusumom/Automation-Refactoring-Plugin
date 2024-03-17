package com.tsybulka.autorefactoringplugin.inspections;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class InspectionsBundle extends DynamicBundle {

	private static final InspectionsBundle CURRENT_INSTANCE = new InspectionsBundle();

	@NonNls
	public static final String BUNDLE = "messages.inspection";

	private InspectionsBundle() {
		super(BUNDLE);
	}

	public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
									  Object @NotNull ... params) {
		return CURRENT_INSTANCE.getMessage(key, params);
	}

}

package com.tsybulka.autorefactoringplugin.ui;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class UiBundle extends DynamicBundle {

	private static final com.tsybulka.autorefactoringplugin.ui.UiBundle CURRENT_INSTANCE = new com.tsybulka.autorefactoringplugin.ui.UiBundle();

	@NonNls
	public static final String BUNDLE = "messages.ui";

	private UiBundle() {
		super(BUNDLE);
	}

	public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
									  Object @NotNull ... params) {
		return CURRENT_INSTANCE.getMessage(key, params);
	}

}

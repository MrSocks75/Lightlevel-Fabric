package dev.mrsocks.lightlevel;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class LightLevel implements ClientModInitializer
{
	private static final KeyMapping.Category KEYBIND_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("lightlevel", "category"));
	private static final String TOGGLE_KEYBIND = "key.lightlevel.toggle";
	private static final String SETTINGS_KEYBIND = "key.lightlevel.settings";
	private static KeyMapping keyToggle;
	private static KeyMapping keySettings;

	public enum Mode
	{
		OFF,
		BASIC,
		ADVANCED
	}

	private static Mode mode = Mode.OFF;

	public static Mode getMode()
	{
		return mode;
	}

	@Override
	public void onInitializeClient()
	{
		LLConfig.load();

		keyToggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(TOGGLE_KEYBIND, InputConstants.KEY_F9, KEYBIND_CATEGORY));
		keySettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(SETTINGS_KEYBIND, InputConstants.KEY_F10, KEYBIND_CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(client ->
		{
			while (keyToggle.consumeClick())
				mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];

			while (keySettings.consumeClick())
				client.gui.setScreen(new LLConfigScreen(client.gui.screen()));
		});
		LevelRenderEvents.COLLECT_SUBMITS.register(LLWorldRender::collectSubmits);
	}
}

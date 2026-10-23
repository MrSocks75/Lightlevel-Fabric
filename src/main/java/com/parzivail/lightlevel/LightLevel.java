package com.parzivail.lightlevel;

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
	private static KeyMapping keyToggle;

	private static boolean enabled;

	public static boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void onInitializeClient()
	{
		keyToggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(TOGGLE_KEYBIND, InputConstants.KEY_F9, KEYBIND_CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(client ->
		{
			while (keyToggle.consumeClick())
				enabled = !enabled;
		});
		LevelRenderEvents.COLLECT_SUBMITS.register(LLWorldRender::collectSubmits);
	}
}

package dev.mrsocks.lightlevel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class LLWorldRender
{
	private static final int FULL_BRIGHT = 0xF000F0;

	public static void collectSubmits(LevelRenderContext context)
	{
		var mode = LightLevel.getMode();
		if (mode == LightLevel.Mode.OFF)
			return;

		var client = Minecraft.getInstance();

		var player = client.player;
		var world = client.level;
		if (player == null || world == null)
			return;

		var f = client.font;

		var camera = context.levelState().cameraRenderState;
		var pos = camera.pos;
		var frustum = camera.cullFrustum;

		var matrices = context.poseStack();
		var collector = context.submitNodeCollector();

		matrices.pushPose();

		var showBothValues = mode == LightLevel.Mode.ADVANCED;

		var s = showBothValues ? 1 / 32f : 1 / 16f;

		var playerPos = player.blockPosition();
		matrices.translate(-pos.x, -pos.y, -pos.z);
		matrices.translate(playerPos.getX(), playerPos.getY(), playerPos.getZ());

		var mutablePos = playerPos.mutable();
		var q = new Matrix4f().rotateX((float)Math.PI / 2);

		for (int x = -16; x < 16; x++)
			for (int y = -16; y < 4; y++)
				for (int z = -16; z < 16; z++)
				{
					var queryPos = mutablePos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);

					if (!frustum.isVisible(new AABB(queryPos)) || !isTopSolid(world, queryPos.below(), player) || isTopSolid(world, queryPos, player))
						continue;

					matrices.pushPose();
					matrices.translate(x, y, z);
					matrices.mulPose(q);

					matrices.scale(s, s, 1);

					var blockLight = world.getBrightness(LightLayer.BLOCK, queryPos);
					var skyLight = world.getBrightness(LightLayer.SKY, queryPos);

					var color = 0xFFFFFF; // spawn never

					if (blockLight == 0)
					{
						if (skyLight == 0)
							color = 0xFF0000; // Spawn at any time
						else
							color = 0xFFFF00; // Spawn only at night
					}

					if (showBothValues)
					{
						drawNumber(matrices, collector, f, "■" + blockLight, color, 13, 8);
						drawNumber(matrices, collector, f, "☀" + skyLight, color, 21, 23);
					}
					else
						drawNumber(matrices, collector, f, String.valueOf(blockLight), color, 9, 8);

					matrices.popPose();
				}

		matrices.popPose();
	}

	private static boolean isTopSolid(net.minecraft.world.level.BlockGetter world, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.Entity entity)
	{
		return world.getBlockState(pos).entityCanStandOn(world, pos, entity);
	}

	private static void drawNumber(PoseStack matrices, SubmitNodeCollector collector, Font f, String str, int color, int offsetX, int offsetY)
	{
		var text = FormattedCharSequence.forward(str, Style.EMPTY);

		matrices.pushPose();
		matrices.translate(offsetX - str.length() * 3.5f, offsetY + 1 - f.lineHeight / 2f, 0);
		collector.submitText(matrices, 0, 0, text, false, Font.DisplayMode.POLYGON_OFFSET, FULL_BRIGHT, 0xFF000000 | (color & 0x3F3F3F), 0, 0);
		matrices.translate(-1.1, -0.9, -0.0005);
		collector.submitText(matrices, 0, 0, text, false, Font.DisplayMode.POLYGON_OFFSET, FULL_BRIGHT, 0xFF000000 | color, 0, 0);
		matrices.popPose();
	}
}

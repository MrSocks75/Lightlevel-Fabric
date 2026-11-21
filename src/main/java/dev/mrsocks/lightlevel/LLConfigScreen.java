package dev.mrsocks.lightlevel;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

public class LLConfigScreen extends Screen
{
	private static final Pattern HEX = Pattern.compile("[0-9a-fA-F]{6}");
	private static final int ROW_HEIGHT = 28;
	private static final int TEXT_COLOR = 0xFFE0E0E0;
	private static final int INVALID_COLOR = 0xFFFF5555;

	private final @Nullable Screen parent;

	public LLConfigScreen(@Nullable Screen parent)
	{
		super(Component.translatable("lightlevel.config.title"));
		this.parent = parent;
	}

	private int rowY(int index)
	{
		return this.height / 2 - 40 + index * ROW_HEIGHT;
	}

	@Override
	protected void init()
	{
		var cx = this.width / 2;

		for (var category : LLConfig.Category.values())
		{
			var y = rowY(category.ordinal());

			var box = new EditBox(this.font, cx - 40, y, 70, 20, Component.translatable("lightlevel.config." + category.id));
			box.setMaxLength(6);
			box.setValue(LLConfig.toHex(LLConfig.getColor(category)));
			box.setResponder(value ->
			{
				var valid = HEX.matcher(value).matches();
				box.setTextColor(valid ? TEXT_COLOR : INVALID_COLOR);
				if (valid)
					LLConfig.setColor(category, Integer.parseInt(value, 16));
			});
			this.addRenderableWidget(box);

			this.addRenderableWidget(Button.builder(Component.translatable("lightlevel.config.reset"), button -> box.setValue(LLConfig.toHex(category.defaultColor)))
			                               .bounds(cx + 62, y, 50, 20)
			                               .build());
		}

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
		                               .bounds(cx - 100, rowY(LLConfig.Category.values().length) + 12, 200, 20)
		                               .build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
	{
		super.extractRenderState(graphics, mouseX, mouseY, a);

		var cx = this.width / 2;

		graphics.centeredText(this.font, this.title, cx, rowY(0) - 30, 0xFFFFFFFF);

		for (var category : LLConfig.Category.values())
		{
			var y = rowY(category.ordinal());

			graphics.text(this.font, Component.translatable("lightlevel.config." + category.id), cx - 150, y + 6, TEXT_COLOR);

			// color swatch with a 1px border
			graphics.fill(cx + 35, y - 1, cx + 57, y + 21, 0xFFA0A0A0);
			graphics.fill(cx + 36, y, cx + 56, y + 20, 0xFF000000 | LLConfig.getColor(category));
		}
	}

	@Override
	public void onClose()
	{
		LLConfig.save();
		this.minecraft.gui.setScreen(this.parent);
	}
}

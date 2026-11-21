package dev.mrsocks.lightlevel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LLConfig
{
	public enum Category
	{
		SAFE("safe", 0xFFFFFF), // spawn never
		NIGHT("night", 0xFFFF00), // spawn only at night
		ALWAYS("always", 0xFF0000); // spawn at any time

		public final String id;
		public final int defaultColor;

		Category(String id, int defaultColor)
		{
			this.id = id;
			this.defaultColor = defaultColor;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("lightlevel");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("lightlevel.json");

	private static final int[] colors = new int[Category.values().length];

	static
	{
		for (var category : Category.values())
			colors[category.ordinal()] = category.defaultColor;
	}

	public static int getColor(Category category)
	{
		return colors[category.ordinal()];
	}

	public static void setColor(Category category, int color)
	{
		colors[category.ordinal()] = color & 0xFFFFFF;
	}

	public static String toHex(int color)
	{
		return String.format("%06X", color & 0xFFFFFF);
	}

	public static void load()
	{
		if (!Files.exists(PATH))
			return;

		try
		{
			var json = JsonParser.parseString(Files.readString(PATH)).getAsJsonObject();
			for (var category : Category.values())
				if (json.has(category.id))
					setColor(category, Integer.parseUnsignedInt(json.get(category.id).getAsString(), 16));
		}
		catch (IOException | RuntimeException e)
		{
			LOGGER.warn("Failed to read {}, using default colors", PATH, e);
		}
	}

	public static void save()
	{
		var json = new JsonObject();
		for (var category : Category.values())
			json.addProperty(category.id, toHex(getColor(category)));

		try
		{
			Files.writeString(PATH, GSON.toJson(json));
		}
		catch (IOException e)
		{
			LOGGER.warn("Failed to write {}", PATH, e);
		}
	}
}

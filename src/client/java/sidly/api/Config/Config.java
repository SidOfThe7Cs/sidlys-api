package sidly.api.Config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    public static final KeyBinding OPEN_CONFIG_SCREEN_KEY = new KeyBinding(
            "open GUI editor", // description
            InputUtil.Type.KEYSYM,        // Input type (keyboard)
            InputUtil.fromTranslationKey("key.keyboard.k").getCode(),
            "sidly"    // Category for the keybinding
    );

    private static final Path CONFIG_PATH = Paths.get("config", "/sidly/main config");
    public static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of("sidly", "config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(CONFIG_PATH)
                            .setJson5(true)
                            .build())
                    .build();

    @SerialEntry public static List<TextHudElement> hudElements = new ArrayList<>();

    private static final List<Runnable> saveListeners = new ArrayList<>();

    public static void addHudElement(TextHudElement newE){
        for (TextHudElement e : hudElements){
            if (newE.getName().equals(e.getName())) return;
        }
        hudElements.add(newE);
    }
    public static TextHudElement getHudElement(String name){
        for (TextHudElement e : hudElements){
            if (e.getName().equals(name)) return e;
        }
        return null;
    }

    private static Map<String, List<Option<?>>> categories = new HashMap<>();

    public static Screen createConfigScreen(Screen parent) {

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Text.of("Why are you using the narrator?"));

        // Create a single category
        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Text.of("Settings")); // Set the name for the category

        // Iterate over all categories
        for (Map.Entry<String, List<Option<?>>> category : categories.entrySet()) {
            String name = category.getKey();
            List<Option<?>> options = category.getValue();
            if (!options.isEmpty()) {
                OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
                        .name(Text.of(name));

                // Add options to the group
                for (Option<?> option : options) {
                    groupBuilder.option(option);
                }

                // Add the group to the category
                categoryBuilder.group(groupBuilder.build());
            }
        }

        // Build the category and add it to the builder
        builder.category(categoryBuilder.build());

        return builder
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }

    public static void addOption(String category, Option<?> option) {
        if (!categories.containsKey(category)) categories.put(category, new ArrayList<>());
        categories.get(category).add(option);
    }

    public static void save() {
        HANDLER.save(); // save main config

        // Call all registered save hooks
        for (Runnable runnable : saveListeners) {
            runnable.run();
        }
    }

    public static void registerSaveCallback(Runnable callback) {
        saveListeners.add(callback);
    }

    public static void load() {
        HANDLER.load();
    }

}

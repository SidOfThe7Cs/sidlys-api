package sidly.api.Config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Config {

    public static final KeyBinding OPEN_CONFIG_SCREEN_KEY = new KeyBinding(
            "open GUI editor", // description
            InputUtil.Type.KEYSYM,        // Input type (keyboard)
            InputUtil.fromTranslationKey("key.keyboard.k").getCode(),
            "sidly"    // Category for the keybinding
    );

    private static final Path CONFIG_PATH = Paths.get("config", "/sidly/APIconfig");
    public static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of("sidly", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry public static List<TextHudElement> hudElements = new ArrayList<>();

    @SerialEntry public static List<MobHighlight> mobHighlights = new ArrayList<>();
    public static List<EntityType<?>> allTypes = new ArrayList<>();


    private static final List<Runnable> saveListeners = new ArrayList<>();

    public static void addHudElement(TextHudElement newE) {
        for (TextHudElement e : hudElements) {
            if (newE.getName().equals(e.getName())) return;
        }
        hudElements.add(newE);
    }

    public static TextHudElement getHudElement(String name) {
        for (TextHudElement e : hudElements) {
            if (e.getName().equals(name)) return e;
        }
        return null;
    }

    private static Map<String, List<Option<?>>> categories = new HashMap<>();

    public static Screen createConfigScreen(Screen parent) {

        cleanMobHighlightOptions(); // clears all of them from the option screen and all that are empty from mobHighlights
        mobHighlights.add(new MobHighlight(Color.white, "none"));
        for (int i = 0; i < mobHighlights.size(); i++) {
            addModHighlight(i);
        }

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

    public static void cleanMobHighlightOptions() {
        // Remove options from the config screen itself
        List<Option<?>> options = categories.get("esp");
        if (options != null) {
            options.removeIf(option -> option.name().equals(Text.of("Entity to Highlight")));
            options.removeIf(option -> option.name().equals(Text.of("color for ^")));
        }

        // Remove mobHighlights with entityTypeId "none" from the list of saved highlights
        mobHighlights.removeIf(entry -> "none".equals(entry.getEntityTypeId()));
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

    public static Config get() {
        return HANDLER.instance();
    }

    public static void addConfigOptions() {

        for (EntityType<?> type : Registries.ENTITY_TYPE) {
            allTypes.add(type);
        }
    }

    private static void addModHighlight(int index) {
        MobHighlight highlight = mobHighlights.get(index);
        Option<?> option;

        option = Option.<String>createBuilder()
                .name(Text.of("Entity to Highlight"))
                .description(OptionDescription.of(Text.of("Select an entity to highlight, reopen the config screen to set more highlights, click the reset button to remove the option from the screen")))
                .binding(
                        "none",
                        highlight::getEntityTypeId,
                        highlight::setEntityTypeId
                )
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .values(allTypes.stream()
                                .map(type -> Registries.ENTITY_TYPE.getId(type).getPath()) // strip "minecraft:"
                                .toList())
                )
                .build();
        sidly.api.Config.Config.addOption("esp", option);

        option = Option.<Color>createBuilder()
                .name(Text.of("color for ^"))
                .description(OptionDescription.of(Text.of("")))
                .binding(
                        highlight.getHighlightColor(),
                        highlight::getHighlightColor,
                        highlight::setHighlightColor
                )
                .controller(ColorControllerBuilder::create)
                .build();
        sidly.api.Config.Config.addOption("esp", option);
    }

}

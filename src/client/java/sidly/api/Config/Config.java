package sidly.api.Config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
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
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.IntFunction;

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


    private static final List<Runnable> saveListeners = new ArrayList<>();
    private static final List<DynamicOption> dynamicOptions = new ArrayList<>();


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

    private static Map<String, List<Option<?>>> groups = new HashMap<>();

    public static Screen createConfigScreen(Screen parent) {

        // main screen builder
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder().title(Text.of("Why are you using the narrator?"));

        // Create a group
        ConfigCategory.Builder settingsCategory = ConfigCategory.createBuilder().name(Text.of("Settings")); // Set the name for the group

        // Iterate over all groups
        for (Map.Entry<String, List<Option<?>>> group : groups.entrySet()) {
            String name = group.getKey();
            List<Option<?>> options = group.getValue();
            if (!options.isEmpty()) {
                OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
                        .name(Text.of(name));

                // Add options to the group
                for (Option<?> option : options) {
                    groupBuilder.option(option);
                }

                // Add the group to the category
                settingsCategory.group(groupBuilder.build());
            }
        }

        // Build the category and add it to the builder
        builder.category(settingsCategory.build());


        ConfigCategory.Builder dynamicOptionsCategory = ConfigCategory.createBuilder().name(Text.of("Dynamic Options"));
        for (DynamicOption dynamicOption : dynamicOptions) { // for each type of dynamic option
            dynamicOption.clean(); // removes all blank options then adds one
            for (int i = 0; i < dynamicOption.getSize(); i++) { // for each option in that type
                List<Option<?>> options = dynamicOption.getOptions(i); // get the list of each option for that option group
                OptionGroup.Builder groupBuilder = OptionGroup.createBuilder().name(Text.of(String.valueOf(i))); // create a group for that dynamic option
                if (!options.isEmpty()) {

                    // Add options to the group
                    for (Option<?> option : options) {
                        groupBuilder.option(option);
                    }

                    // Add the group to the category
                    dynamicOptionsCategory.group(groupBuilder.build());
                }
            }
        }

        // Build the category and add it to the builder
        builder.category(dynamicOptionsCategory.build());


        return builder
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }

    public static void addOption(String category, Option<?> option) {
        if (!groups.containsKey(category)) groups.put(category, new ArrayList<>());
        groups.get(category).add(option);
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

    public static void init() {
        registerDynamicOption(Config::getMobHighlight, mobHighlights, Config::cleanMobHighlights);
    }

    private static List<Option<?>> getMobHighlight(int index) {
        MobHighlight highlight = mobHighlights.get(index);
        List<Option<?>> options = new ArrayList<>();
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
                        .values(Registries.ENTITY_TYPE.stream()
                                .map(type -> Registries.ENTITY_TYPE.getId(type).getPath()) // strip "minecraft:"
                                .toList())
                )
                .build();
        options.add(option);

        option = Option.<Color>createBuilder()
                .name(Text.of("color"))
                .description(OptionDescription.of(Text.of("")))
                .binding(
                        highlight.getHighlightColor(),
                        highlight::getHighlightColor,
                        highlight::setHighlightColor
                )
                .controller(ColorControllerBuilder::create)
                .build();
        options.add(option);

        option = Option.<Boolean>createBuilder()
                .name(Text.of("draw line"))
                .description(OptionDescription.of(Text.of("")))
                .binding(
                        false,
                        highlight::getDrawLine,
                        highlight::setDrawLine
                )
                .controller(TickBoxControllerBuilder::create)
                .build();
        options.add(option);

        option = Option.<Boolean>createBuilder()
                .name(Text.of("send chat message on spawn"))
                .description(OptionDescription.of(Text.of("")))
                .binding(
                        false,
                        highlight::getChatNotification,
                        highlight::setChatNotification
                )
                .controller(TickBoxControllerBuilder::create)
                .build();
        options.add(option);

        option = Option.<String>createBuilder()
                .name(Text.of("sound to play on spawn"))
                .description(OptionDescription.of(Text.of("")))
                .binding(
                        "none",
                        highlight::getSoundEventId,
                        highlight::setSoundEventId
                )
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .values(Registries.SOUND_EVENT.stream()
                                .map(type -> {
                                    Identifier id = Registries.SOUND_EVENT.getId(type);
                                    if (id != null) return id.getPath();
                                    else return "none";
                                })
                                .toList())
                )
                .build();
        options.add(option);

        return options;
    }

    private static void cleanMobHighlights(){
        mobHighlights.removeIf(entry -> "none".equals(entry.getEntityTypeId()));
        mobHighlights.add(new MobHighlight());
    }

    public static void registerDynamicOption(IntFunction<List<Option<?>>> getter, List<?> size, Runnable cleanFunc){
        dynamicOptions.add(new DynamicOption(getter, size, cleanFunc));
    }

}

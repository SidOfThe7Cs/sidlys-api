package sidly.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
import sidly.api.Config.Config;
import sidly.api.Config.MobHighlight;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ForEachEntity {
    public static List<UUID> seenEntities = new ArrayList<>();

    public static void onClientTick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;
        Iterable<Entity> entities = client.world.getEntities();

        for (Entity e : entities) {
            if (e == null) continue;
            boolean seen = false;
            if (!seenEntities.contains(e.getUuid())) seenEntities.add(e.getUuid());
            else seen = true;
            String type = e.getType().getName().getString();

            for (MobHighlight mobHighlight : Config.mobHighlights) {
                if (mobHighlight.matchesEntity(e)) {
                    DrawQueue.add(ctx -> Utils.drawBoxEdges(ctx, e.getBoundingBox(), 1, mobHighlight.getHighlightColor()));
                    if (mobHighlight.getDrawLine()) {
                        DrawQueue.add(ctx -> Utils.drawLineFromCrosshair(ctx, e.getPos(), 1, mobHighlight.getHighlightColor()));
                    }
                    if (!seen) {
                        if (mobHighlight.getChatNotification()) {
                            Color color = mobHighlight.getHighlightColor();
                            ChatMessageUtils.sendChatMessage(Text.literal(type + " spawned").setStyle(Style.EMPTY.withColor(color.getRGB())));
                        }
                        SoundEvent sound = mobHighlight.getSoundEvent();
                        if (sound != null) {
                            client.player.playSound(sound);
                        }
                    }
                }
            }
        }
    }
}

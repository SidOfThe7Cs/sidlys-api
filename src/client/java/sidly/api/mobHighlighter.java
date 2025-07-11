package sidly.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import sidly.api.Config.Config;
import sidly.api.Config.MobHighlight;

public class mobHighlighter {

    public static void onClientTick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;
        Iterable<Entity> entities = client.world.getEntities();

        for (Entity e : entities) {
            if (e == null) continue;
            for (MobHighlight mobHighlight : Config.mobHighlights) {
                if (mobHighlight.matchesEntity(e)) {
                    DrawQueue.add(ctx -> Utils.drawBoxEdges(ctx, e.getBoundingBox(), 1, mobHighlight.getHighlightColor()));
                }
            }
        }
    }
}

package sidly.api;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import sidly.api.Config.Config;
import java.util.function.Consumer;

public class SidlysApiModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Config.load();
		Config.init();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());
		Runtime.getRuntime().addShutdownHook(new Thread(Config::save));

		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, Utils.MY_HUD_LAYER, this::drawToHud));
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {
		// Your logic first
		DrawQueue.clear();

		// Then call registered callbacks
		for (Consumer<MinecraftClient> cb : Utils.afterTickCallbacks) {
			cb.accept(client);
		}

		ForEachEntity.onClientTick(client);
	}


	private void drawToHud(DrawContext context, RenderTickCounter renderTickCounter) {
		DrawQueue.render(context);
	}
}
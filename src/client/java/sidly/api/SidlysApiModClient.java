package sidly.api;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import sidly.api.Config.Config;

public class SidlysApiModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			Config.HANDLER.save();
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Config.HANDLER.save();
		}));

	}
}
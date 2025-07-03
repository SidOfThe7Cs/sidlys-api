package sidly.api.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {

	// lets me get the fov so i can render from 3d to screen manually correctly
	@Invoker("getFov")
	public abstract float invokeGetFov(Camera camera, float tickDelta, boolean changingFov);
}

package sidly.api.Config;

import net.minecraft.client.gui.DrawContext;

public interface HudElement {
    String getName();
    void render(DrawContext drawContext);
    boolean isHovering(double mouseX, double mouseY);
    void onMouseDragged(double mouseX, double mouseY);
    void onMouseClicked(double mouseX, double mouseY);
    void onMouseReleased();
}

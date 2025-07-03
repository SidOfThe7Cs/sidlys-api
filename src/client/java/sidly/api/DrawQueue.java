package sidly.api;

import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DrawQueue {
    private static final List<Consumer<DrawContext>> queue = new ArrayList<>();

    public static void add(Consumer<DrawContext> drawCall) {
        queue.add(drawCall);
    }

    public static void render(DrawContext context) {
        for (Consumer<DrawContext> drawCall : queue) {
            drawCall.accept(context);
        }
    }

    public static void clear() {
        queue.clear();
    }
}

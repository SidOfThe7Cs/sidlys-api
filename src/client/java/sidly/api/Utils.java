package sidly.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import sidly.api.mixin.client.EntityAccessor;
import sidly.api.mixin.client.GameRendererInvoker;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    public static final Identifier MY_HUD_LAYER = Identifier.of("wynntools", "hud-example-layer");

    //draws line on screen from xy to xy
    public static void drawLine(DrawContext context, Vec2f pos1, Vec2f pos2, float thickness, Color color) {

        context.draw(vertexConsumers -> {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getGui());

            float dx = pos2.x - pos1.x;
            float dy = pos2.y - pos1.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len == 0) return;

            // Normalize direction vector
            dx /= len;
            dy /= len;

            // Perpendicular vector for thickness
            float px = -dy * (thickness / 2);
            float py = dx * (thickness / 2);

            float xA1 = pos1.x + px;
            float yA1 = pos1.y + py;
            float xA2 = pos1.x - px;
            float yA2 = pos1.y - py;
            float xB1 = pos2.x + px;
            float yB1 = pos2.y + py;
            float xB2 = pos2.x - px;
            float yB2 = pos2.y - py;


            vc.vertex(matrix, xA1, yA1, 0).color(color.getRGB());
            vc.vertex(matrix, xB1, yB1, 0).color(color.getRGB());
            vc.vertex(matrix, xB2, yB2, 0).color(color.getRGB());
            vc.vertex(matrix, xA2, yA2, 0).color(color.getRGB());
        });
    }
    //draws line from world coords to world coords
    public static void drawLine(DrawContext context, Vec3d a, Vec3d b, float thickness, Color color){
        Vec2f pos1 = worldToScreenCoords(a);
        Vec2f pos2 = worldToScreenCoords(b);
        drawLine(context, pos1, pos2, thickness, color);
    }
    private static void drawLine(DrawContext context, Vec3i corner1, Vec3i corner2, float thickness, Color color) {
        Vec3d pos1 = new Vec3d(corner1);
        Vec3d pos2 = new Vec3d(corner2);
        drawLine(context, pos1, pos2, thickness, color);
    }
    // single pos draws from center of screen to world coords
    public static void drawLineFromCrosshair(DrawContext context, Vec3d pos, float thickness, Color color){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        Vec2f screenCenter = new Vec2f(client.getWindow().getScaledWidth() / 2.0F, client.getWindow().getScaledHeight() / 2.0F);
        Vec2f pos2 = worldToScreenCoords(pos);
        drawLine(context, screenCenter, pos2, thickness, color);
    }
    public static void drawBoxEdges(DrawContext context, Box box, float thickness, Color color) {
        Vec3d[] corners = new Vec3d[]{
                new Vec3d( box.minX, box.minY, box.minZ),
                new Vec3d( box.maxX, box.minY, box.minZ),
                new Vec3d( box.maxX, box.maxY, box.minZ),
                new Vec3d( box.minX, box.maxY, box.minZ),
                new Vec3d( box.minX, box.minY, box.maxZ),
                new Vec3d( box.maxX, box.minY, box.maxZ),
                new Vec3d( box.maxX, box.maxY, box.maxZ),
                new Vec3d( box.minX, box.maxY, box.maxZ)
        };

        int[][] edges = new int[][]{
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // bottom face
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // top face
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // vertical edges
        };

        for (int[] edge : edges) {
            if (thickness == 0) drawLine(context, corners[edge[0]], corners[edge[1]], thickness, color);
            else drawLine(context, corners[edge[0]], corners[edge[1]], thickness, color);
        }
    }
    public static void drawBlockEdges(DrawContext context, Vec3i pos, float thickness, Color color) {
        // Define the corners of the block based on the Vec3i position
        Vec3i[] corners = new Vec3i[]{
                new Vec3i(pos.getX(), pos.getY(), pos.getZ()), // Min corner (x, y, z)
                new Vec3i(pos.getX() + 1, pos.getY(), pos.getZ()), // Max X corner
                new Vec3i(pos.getX() + 1, pos.getY(), pos.getZ() + 1), // Max X, Z corner
                new Vec3i(pos.getX(), pos.getY(), pos.getZ() + 1), // Max Z corner
                new Vec3i(pos.getX(), pos.getY() + 1, pos.getZ()), // Max Y corner
                new Vec3i(pos.getX() + 1, pos.getY() + 1, pos.getZ()), // Max X, Y corner
                new Vec3i(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), // Max X, Y, Z corner
                new Vec3i(pos.getX(), pos.getY() + 1, pos.getZ() + 1), // Max Y, Z corner
        };

        // Define the edges of the block (pairs of corners to connect)
        int[][] edges = new int[][]{
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, // Bottom face
                {4, 5}, {5, 6}, {6, 7}, {7, 4}, // Top face
                {0, 4}, {1, 5}, {2, 6}, {3, 7}  // Vertical edges
        };

        // Loop through each edge and draw it
        for (int[] edge : edges) {
            drawLine(context, corners[edge[0]], corners[edge[1]], thickness, color);
        }
    }
    static void drawItemEdges(DrawContext context, ItemEntity item, float thickness, Color color){
        drawBoxEdges(context, item.getBoundingBox().expand(0.2), thickness, color);
    }

    public static Vec2f worldToScreenCoords(Vec3d worldCoords){
        // check nulls
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return null;
        if (client.player == null) return null;

        // get stuff
        Camera camera = client.gameRenderer.getCamera();
        GameRenderer renderer = client.gameRenderer;;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        Vec3d cameraPos = camera.getPos();
        float yaw = (float)Math.toRadians(camera.getYaw());
        float pitch = (float)Math.toRadians(-camera.getPitch());
        double FOV = ((GameRendererInvoker) (Object) renderer).invokeGetFov(
                camera,
                client.getRenderTickCounter().getTickProgress(true),
                true
        );

        // Calculate relative position to camera
        double dx = worldCoords.x - cameraPos.x;
        double dy = worldCoords.y - cameraPos.y;
        double dz = worldCoords.z - cameraPos.z;
        dx *= -1; // we dont ask questions we just provide solutions

        // Rotate based on yaw (horizontal rotation)
        double x = dx * Math.cos(yaw) - dz * Math.sin(yaw);
        double z = dx * Math.sin(yaw) + dz * Math.cos(yaw);

        // Rotate based on pitch (vertical rotation)
        double y = dy * Math.cos(pitch) - z * Math.sin(pitch);
        z = dy * Math.sin(pitch) + z * Math.cos(pitch);

        // if behind camera dont do weird stuff
        if (z <= 0.1) {
            z = 0.1;
        }


        // Perspective projection
        double scale = Math.min(screenWidth, screenHeight) / (2.0 * Math.tan(Math.toRadians(FOV) / 2));
        double screenX = (x / z) * scale + (double) screenWidth / 2;
        double screenY = (-y / z) * scale + (double) screenHeight / 2;

        int minSize = 4;
        int edgeSize = minSize * 3;
        int size = (int) (100 / z);
        size = Math.max(size, minSize);

        if (screenX < 0) {
            screenX = 0;
            size = edgeSize;
        }
        if (screenY < 0) {
            screenY = 0;
            size = edgeSize;
        }
        if (screenX > screenWidth) {
            screenX = screenWidth;
            size = edgeSize;
        }
        if (screenY > screenHeight) {
            screenY = screenHeight;
            size = edgeSize;
        }
        return new Vec2f((float) screenX, (float) screenY);
    }


    public static String millisToHMS(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h");
        if (minutes > 0 || hours > 0) sb.append(minutes).append("m");
        sb.append(secs).append("s");

        return sb.toString();
    }

    public static void setGlowing(Entity e){
        byte flags = e.getDataTracker().get(EntityAccessor.getFlags());
        // Set bit 6 (glowing)
        flags |= 1 << 6;
        e.getDataTracker().set(EntityAccessor.getFlags(), flags);
    }
    //add method that has color option

    //returns the literally string of the Unicode eg: U+1234
    public static String convertCustomCharacterToUnicode(String input) {
        return input.codePoints().mapToObj(cp -> String.format("U+%04X", cp)).collect(Collectors.joining(" "));
    }

    public static List<Text> getTooltip(ItemStack item){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return new ArrayList<>();

        return item.getTooltip(Item.TooltipContext.DEFAULT, client.player, TooltipType.BASIC);

    }
}

package sidly.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class ChatMessageUtils {

    public static void sendChatCommand(String command) { // command starts directly NOT with a slash
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player != null && command != null && !command.isEmpty()) {
            player.networkHandler.sendChatCommand(command);
        }
    }

    public static void sendChatMessage(String message) { //this sends client side only
        sendChatMessage(Text.literal(message));
    }

    public static void sendChatMessage(Text message) { //this sends client side only
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player != null && message != null) {
            player.sendMessage(message, false);
        }
    }

    public static String removeColorCodes(String message) {
        // Regular expression to match Minecraft color codes
        return message.replaceAll("§.", "");
    }

    public static String removeNonAscii(String message){
        String cleaned = removeColorCodes(message);
        cleaned = cleaned.replaceAll("[^\\x00-\\x7F]", "");
        cleaned = cleaned.replace("\n", "");
        cleaned = cleaned.replace("  ", " ");
        return cleaned.trim();
    }
}

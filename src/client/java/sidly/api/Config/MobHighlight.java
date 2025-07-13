package sidly.api.Config;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.awt.*;

public class MobHighlight{
    private String entityTypeId = "none";
    private Color highlightColor = Color.white;
    private Boolean drawLine = false;
    private String soundEventId = "none";
    private Boolean chatNotification = false;

    public MobHighlight() {}


    public Boolean getChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(Boolean chatNotification) {
        this.chatNotification = chatNotification;
    }

    public SoundEvent getSoundEvent() {
        if (soundEventId == null || soundEventId.equals("none")) return null;

        return Registries.SOUND_EVENT.get(Identifier.of(soundEventId));
    }

    public void setSoundEventId(String soundEvent) {
        this.soundEventId = soundEvent;
    }

    public String getSoundEventId() {
        return this.soundEventId;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public boolean matchesEntity(Entity entity) {
        if (entity == null || entityTypeId == null || entityTypeId.equals("none")) return false;

        EntityType<?> storedType = Registries.ENTITY_TYPE.get(Identifier.of(entityTypeId));
        return entity.getType().equals(storedType);
    }

    public EntityType<?> getEntityType() {
        return Registries.ENTITY_TYPE.get(Identifier.of(entityTypeId));
    }

    public String  getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String  entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public Boolean getDrawLine() {
        return drawLine;
    }

    public void setDrawLine(Boolean drawLine) {
        this.drawLine = drawLine;
    }
}

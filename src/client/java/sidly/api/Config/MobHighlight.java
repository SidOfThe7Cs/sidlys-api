package sidly.api.Config;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.awt.*;

public class MobHighlight {
    private Color highlightColor;
    private String entityTypeId;

    public MobHighlight(Color highlightColor, EntityType<?> type) {
        this.highlightColor = highlightColor;
        this.entityTypeId = Registries.ENTITY_TYPE.getId(type).getPath();
    }
    public MobHighlight(Color highlightColor, String type) {
        this.highlightColor = highlightColor;
        this.entityTypeId = type;
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

    public String  getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String  entityTypeId) {
        this.entityTypeId = entityTypeId;
    }
}

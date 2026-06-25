package net.minestom.server.entity.ai.brain.memory;

import java.util.List;
import java.util.Optional;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;

public class EntityTracker implements PositionTracker {
    private final Entity entity;
    private final boolean trackEyeHeight;
    private final boolean targetEyeHeight;

    public EntityTracker(Entity entity, boolean trackEyeHeight) {
        this(entity, trackEyeHeight, false);
    }

    public EntityTracker(Entity entity, boolean trackEyeHeight, boolean targetEyeHeight) {
        this.entity = entity;
        this.trackEyeHeight = trackEyeHeight;
        this.targetEyeHeight = targetEyeHeight;
    }

    @Override
    public Vec currentPosition() {
        return this.trackEyeHeight
                ? this.entity.getPosition().add(0.0, this.entity.getEyeHeight(), 0.0).asVec()
                : this.entity.getPosition().asVec();
    }

    @Override
    public Point currentBlockPosition() {
        return this.targetEyeHeight
                ? this.entity.getPosition().add(0.0, this.entity.getEyeHeight(), 0.0).asBlockVec()
                : this.entity.getPosition().asBlockVec();
    }

    @Override
    public boolean isVisibleBy(EntityCreature body) {
        if (this.entity instanceof LivingEntity livingEntity) {
            if (livingEntity.isDead()) {
                return false;
            } else {
                Optional<List<LivingEntity>> visibleEntities = body.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
                return visibleEntities.isPresent() && visibleEntities.get().contains(livingEntity);
            }
        } else {
            return true;
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public String toString() {
        return "EntityTracker for " + this.entity;
    }
}

package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.ai.navigation.WaterBoundPathNavigation;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;

/**
 * Base class for water-bound mobs, mirroring vanilla {@code WaterAnimal}. Uses water navigation and
 * suffocates (takes dry-out damage) when left out of water.
 */
public abstract class WaterAnimal extends EntityCreature {
    private int airSupply = 300;

    protected WaterAnimal(final EntityType entityType) {
        super(entityType);
    }

    @Override
    protected PathNavigation createNavigation() {
        return new WaterBoundPathNavigation(this);
    }

    @Override
    protected boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void update(final long time) {
        super.update(time);
        if (isDead()) {
            return;
        }
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }
        final Pos position = getPosition();
        if (!instance.isChunkLoaded(position)) {
            return;
        }
        if (PathBlocks.isWater(instance.getBlock(position))) {
            this.airSupply = 300;
        } else {
            this.airSupply--;
            if (this.airSupply <= -20) {
                this.airSupply = 0;
                damage(DamageType.DRY_OUT, 2.0f);
            }
        }
    }
}

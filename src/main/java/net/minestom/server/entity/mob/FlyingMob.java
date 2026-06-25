package net.minestom.server.entity.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.control.FlyingMoveControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.navigation.FlyingPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;

/**
 * Base class for flying mobs, mirroring vanilla {@code FlyingMob}/{@code Mob} flyers. Uses flying
 * navigation and a hovering flying move control so the mob does not fall when idle.
 */
public abstract class FlyingMob extends EntityCreature {
    protected FlyingMob(final EntityType entityType) {
        super(entityType);
        setNoGravity(true);
    }

    @Override
    protected MoveControl createMoveControl() {
        return new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected PathNavigation createNavigation() {
        return new FlyingPathNavigation(this);
    }
}

package net.minestom.server.entity.ai.targeting;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;

public class Sensing {
    private final EntityCreature mob;
    private final IntSet seen = new IntOpenHashSet();
    private final IntSet unseen = new IntOpenHashSet();

    public Sensing(final EntityCreature mob) {
        this.mob = mob;
    }

    public void tick() {
        this.seen.clear();
        this.unseen.clear();
    }

    public boolean hasLineOfSight(final Entity target) {
        int targetId = target.getEntityId();
        if (this.seen.contains(targetId)) {
            return true;
        } else if (this.unseen.contains(targetId)) {
            return false;
        } else {
            boolean hasLineOfSight = this.mob.hasLineOfSight(target);
            if (hasLineOfSight) {
                this.seen.add(targetId);
            } else {
                this.unseen.add(targetId);
            }

            return hasLineOfSight;
        }
    }
}

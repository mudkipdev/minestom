package net.minestom.server.entity.ai.goal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.util.DefaultRandomPos;
import net.minestom.server.entity.ai.util.GoalUtils;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Function;

public class PanicGoal extends Goal {
    public static final int WATER_CHECK_DISTANCE_VERTICAL = 1;
    protected final EntityCreature mob;
    protected final double speedModifier;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected boolean isRunning;
    private final Function<EntityCreature, TagKey<DamageType>> panicCausingDamageTypes;

    public PanicGoal(final EntityCreature mob, final double speedModifier) {
        this(mob, speedModifier, TagKey.<DamageType>ofHash("#minecraft:panic_causes"));
    }

    public PanicGoal(final EntityCreature mob, final double speedModifier, final TagKey<DamageType> panicCausingDamageTypes) {
        this(mob, speedModifier, entity -> panicCausingDamageTypes);
    }

    public PanicGoal(final EntityCreature mob, final double speedModifier, final Function<EntityCreature, TagKey<DamageType>> panicCausingDamageTypes) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.panicCausingDamageTypes = panicCausingDamageTypes;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.shouldPanic()) {
            return false;
        } else {
            if (this.mob.isOnFire()) {
                Point blockPos = this.lookForWater(this.mob.getInstance(), this.mob, 5);
                if (blockPos != null) {
                    this.posX = (double) blockPos.blockX();
                    this.posY = (double) blockPos.blockY();
                    this.posZ = (double) blockPos.blockZ();
                    return true;
                }
            }

            return this.findRandomPosition();
        }
    }

    protected boolean shouldPanic() {
        return this.mob.getLastDamageSource() != null && this.is(this.mob.getLastDamageSource(), this.panicCausingDamageTypes.apply(this.mob));
    }

    protected boolean findRandomPosition() {
        Vec pos = DefaultRandomPos.getPos(this.mob, 5, 4);
        if (pos == null) {
            return false;
        } else {
            this.posX = pos.x();
            this.posY = pos.y();
            this.posZ = pos.z();
            return true;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected Point lookForWater(final Instance level, final EntityCreature mob, final int xzDist) {
        Point mobPosition = new BlockVec(mob.getPosition());
        if (!level.isChunkLoaded(mobPosition) || !this.isCollisionShapeEmpty(level.getBlock(mobPosition))) {
            return null;
        } else {
            for (Point pos : withinManhattan(mobPosition, xzDist, WATER_CHECK_DISTANCE_VERTICAL, xzDist)) {
                if (GoalUtils.isWater(mob, pos)) {
                    return pos;
                }
            }

            return null;
        }
    }

    private boolean is(final Damage damage, final TagKey<DamageType> tag) {
        final RegistryTag<DamageType> registryTag = MinecraftServer.getDamageTypeRegistry().getTag(tag);
        return registryTag != null && registryTag.contains(damage.getType());
    }

    private boolean isCollisionShapeEmpty(final Block block) {
        final Shape collisionShape = block.registry().collisionShape();
        return collisionShape.relativeStart().equals(collisionShape.relativeEnd());
    }

    private static Iterable<Point> withinManhattan(final Point origin, final int reachX, final int reachY, final int reachZ) {
        final int maxDepth = reachX + reachY + reachZ;
        final int originX = origin.blockX();
        final int originY = origin.blockY();
        final int originZ = origin.blockZ();
        return () -> new java.util.Iterator<>() {
            private Point next;
            private boolean computed;
            private boolean ended;
            private final int[] cursor = new int[3];
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            private Point computeNext() {
                if (this.zMirror) {
                    this.zMirror = false;
                    this.cursor[2] = originZ - (this.cursor[2] - originZ);
                    return new BlockVec(this.cursor[0], this.cursor[1], this.cursor[2]);
                } else {
                    Point found;
                    for (found = null; found == null; this.y++) {
                        if (this.y > this.maxY) {
                            this.x++;
                            if (this.x > this.maxX) {
                                this.currentDepth++;
                                if (this.currentDepth > maxDepth) {
                                    this.ended = true;
                                    return null;
                                }

                                this.maxX = Math.min(reachX, this.currentDepth);
                                this.x = -this.maxX;
                            }

                            this.maxY = Math.min(reachY, this.currentDepth - Math.abs(this.x));
                            this.y = -this.maxY;
                        }

                        int xx = this.x;
                        int yy = this.y;
                        int zz = this.currentDepth - Math.abs(xx) - Math.abs(yy);
                        if (zz <= reachZ) {
                            this.zMirror = zz != 0;
                            this.cursor[0] = originX + xx;
                            this.cursor[1] = originY + yy;
                            this.cursor[2] = originZ + zz;
                            found = new BlockVec(this.cursor[0], this.cursor[1], this.cursor[2]);
                        }
                    }

                    return found;
                }
            }

            @Override
            public boolean hasNext() {
                if (!this.computed) {
                    this.next = this.computeNext();
                    this.computed = true;
                }
                return !this.ended && this.next != null;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                this.computed = false;
                return this.next;
            }
        };
    }
}

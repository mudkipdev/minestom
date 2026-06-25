package net.minestom.server.entity;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.vehicle.PlayerInputs;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.entity.ai.MobBehaviors;
import net.minestom.server.entity.ai.brain.Brain;
import net.minestom.server.entity.ai.control.BodyRotationControl;
import net.minestom.server.entity.ai.control.JumpControl;
import net.minestom.server.entity.ai.control.LookControl;
import net.minestom.server.entity.ai.control.MoveControl;
import net.minestom.server.entity.ai.navigation.GroundPathNavigation;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.ai.targeting.Sensing;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityCreature extends LivingEntity {

    private int removalAnimationDelay = 1000;
    private int ambientSoundTime;
    private int airSupply = 300;

    private final GoalSelector goalSelector = new GoalSelector();
    private final GoalSelector targetSelector = new GoalSelector();
    private final Sensing sensing = new Sensing(this);
    private final Random random = new Random();

    protected Brain<?> brain = new Brain<>();

    private final MoveControl moveControl;
    private final LookControl lookControl = new LookControl(this);
    private final JumpControl jumpControl = new JumpControl(this);
    private final BodyRotationControl bodyRotationControl = new BodyRotationControl(this);
    private final PathNavigation pathNavigation;

    private Entity target;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public EntityCreature(EntityType entityType, UUID uuid) {
        super(entityType, uuid);
        this.moveControl = createMoveControl();
        this.pathNavigation = createNavigation();
        heal();
        MobBehaviors.install(this);
    }

    public EntityCreature(EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    /**
     * Creates the movement control for this creature. Override to use a non-ground control such as
     * {@link net.minestom.server.entity.ai.control.FlyingMoveControl}.
     *
     * @return the movement control
     */
    protected MoveControl createMoveControl() {
        return new MoveControl(this);
    }

    /**
     * Creates the pathfinding navigation for this creature. Override to use a non-ground navigation such
     * as {@link net.minestom.server.entity.ai.navigation.FlyingPathNavigation}.
     *
     * @return the navigation
     */
    protected PathNavigation createNavigation() {
        return new GroundPathNavigation(this);
    }

    @Override
    public void update(long time) {
        // AI
        this.sensing.tick();
        this.targetSelector.tick();
        this.goalSelector.tick();
        tickBrain();

        // Path finding
        this.pathNavigation.tick();
        this.moveControl.tick();
        this.lookControl.tick();
        this.jumpControl.tick();
        this.bodyRotationControl.clientTick();

        if (!isDead() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            playSound(getAmbientSound());
        }

        tickEnvironment();

        super.update(time);
    }

    protected boolean isSunSensitive() {
        return false;
    }

    protected boolean isSensitiveToWater() {
        return false;
    }

    public boolean interact(final Player player, final PlayerHand hand) {
        return false;
    }

    protected @Nullable Player getControllingRider() {
        for (final Entity passenger : getPassengers()) {
            if (passenger instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    protected void steerWithRider(final Player rider, final double speed) {
        final float yaw = rider.getPosition().yaw();
        setView(yaw, 0.0f);
        final PlayerInputs inputs = rider.inputs();
        double forward = (inputs.forward() ? 1.0 : 0.0) - (inputs.backward() ? 1.0 : 0.0);
        final double strafe = (inputs.right() ? 1.0 : 0.0) - (inputs.left() ? 1.0 : 0.0);
        if (forward == 0.0 && strafe == 0.0) {
            return;
        }
        if (forward < 0.0) {
            forward *= 0.5;
        }
        final double radians = Math.toRadians(yaw);
        final double sin = Math.sin(radians);
        final double cos = Math.cos(radians);
        final Vec velocity = getVelocity();
        setVelocity(new Vec((forward * -sin + strafe * cos) * speed, velocity.y(), (forward * cos + strafe * sin) * speed));
    }

    protected boolean canBreatheUnderwater() {
        return false;
    }

    public void onStruckByLightning() {
    }

    private void tickEnvironment() {
        if (isDead()) return;
        final Instance instance = getInstance();
        if (instance == null) return;
        final Pos position = getPosition();
        if (!instance.isChunkLoaded(position)) return;

        if (isSensitiveToWater() && isInWaterOrRain(instance, position)) {
            damage(DamageType.DROWN, 1.0f);
        }
        if (isSunSensitive() && getFireTicks() < 1 && isExposedToSunlight(instance, position)) {
            setFireTicks(160);
        }
        if (!canBreatheUnderwater() && headInWater(instance, position)) {
            this.airSupply--;
            if (this.airSupply <= -20) {
                this.airSupply = 0;
                damage(DamageType.DROWN, 2.0f);
            }
        } else {
            this.airSupply = 300;
        }
    }

    private boolean headInWater(final Instance instance, final Pos position) {
        final int x = position.blockX();
        final int y = (int) Math.floor(position.y() + getEyeHeight());
        final int z = position.blockZ();
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) return false;
        return instance.getBlock(x, y, z).compare(Block.WATER);
    }

    protected boolean isUnderWater() {
        final Instance instance = getInstance();
        return instance != null && headInWater(instance, getPosition());
    }

    private boolean seesSky(final Instance instance, final Pos position) {
        final int y = (int) Math.floor(position.y() + getEyeHeight());
        return instance.getSkyLight(position.blockX(), y, position.blockZ()) >= 15;
    }

    private boolean isInWaterOrRain(final Instance instance, final Pos position) {
        if (instance.getBlock(position).compare(Block.WATER)) {
            return true;
        }
        return instance.getWeather().isRaining() && seesSky(instance, position);
    }

    private boolean isExposedToSunlight(final Instance instance, final Pos position) {
        if (instance.getWeather().isRaining()) {
            return false;
        }
        if (instance.getTime() % 24000L >= 12000L) {
            return false;
        }
        return seesSky(instance, position);
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    @Override
    protected float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    /**
     * Adds vanilla {@code collide()} step-up. Minestom's collision does not auto-climb ledges, so when a
     * grounded creature is blocked horizontally, the move is retried lifted by the step-height attribute
     * and adopted if it clears the ledge with more horizontal progress (preserving momentum). Without
     * this, mobs cannot walk up a single block and jump uselessly into the wall.
     */
    @Override
    protected void movementTick() {
        final Pos start = getPosition();
        final Vec velocityBefore = getVelocity();
        final boolean wasOnGround = this.onGround;
        super.movementTick();

        if (!hasPhysics()) return;
        final Instance instance = getInstance();
        if (instance == null || !wasOnGround) return;
        final double stepHeight = getAttribute(Attribute.STEP_HEIGHT).getValue();
        if (stepHeight <= 0.0) return;

        final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        final double wantedX = velocityBefore.x() / tps;
        final double wantedZ = velocityBefore.z() / tps;
        final Pos afterNormal = getPosition();
        final double gotX = afterNormal.x() - start.x();
        final double gotZ = afterNormal.z() - start.z();
        final boolean blocked = (Math.abs(wantedX) > 1.0E-4 && Math.abs(gotX) + 1.0E-4 < Math.abs(wantedX))
                || (Math.abs(wantedZ) > 1.0E-4 && Math.abs(gotZ) + 1.0E-4 < Math.abs(wantedZ));
        if (!blocked) return;

        final Block.Getter getter = new ChunkCache(instance, currentChunk, Block.STONE);
        final BoundingBox bb = getBoundingBox();
        final Vec horizontal = new Vec(wantedX, 0.0, wantedZ);
        final PhysicsResult up = CollisionUtils.handlePhysics(getter, bb, start, new Vec(0.0, stepHeight, 0.0), null, false);
        final PhysicsResult forward = CollisionUtils.handlePhysics(getter, bb, up.newPosition(), horizontal, null, false);
        final PhysicsResult down = CollisionUtils.handlePhysics(getter, bb, forward.newPosition(), new Vec(0.0, -stepHeight, 0.0), null, false);
        final Pos stepped = down.newPosition();
        final double steppedProgress = sqr(stepped.x() - start.x()) + sqr(stepped.z() - start.z());
        final double normalProgress = gotX * gotX + gotZ * gotZ;
        if (stepped.y() > afterNormal.y() + 1.0E-4 && steppedProgress > normalProgress + 1.0E-6) {
            refreshPosition(stepped.withView(afterNormal.yaw(), afterNormal.pitch()), true, true);
            this.onGround = true;
            final Vec current = getVelocity();
            addVelocity(velocityBefore.x() - current.x(), 0.0, velocityBefore.z() - current.z());
        }
    }

    private static double sqr(final double value) {
        return value * value;
    }

    @Override
    public CompletableFuture<Void> setInstance(Instance instance, Pos spawnPosition) {
        this.pathNavigation.stop();
        return super.setInstance(instance, spawnPosition);
    }

    @Override
    public void kill() {
        super.kill();

        if (removalAnimationDelay > 0) {
            // Needed for proper death animation (wait for it to finish before destroying the entity)
            scheduleRemove(Duration.of(removalAnimationDelay, TimeUnit.MILLISECOND));
        } else {
            // Instant removal without animation playback
            remove();
        }
    }

    /**
     * Gets the kill animation delay before vanishing the entity.
     *
     * @return the removal animation delay in milliseconds, 0 if not any
     */
    public int getRemovalAnimationDelay() {
        return removalAnimationDelay;
    }

    /**
     * Changes the removal animation delay of the entity.
     * <p>
     * Testing shows that 1000 is the minimum value to display the death particles.
     *
     * @param removalAnimationDelay the new removal animation delay in milliseconds, 0 to remove it
     */
    public void setRemovalAnimationDelay(int removalAnimationDelay) {
        this.removalAnimationDelay = removalAnimationDelay;
    }

    /**
     * Gets the entity target.
     *
     * @return the entity target, can be null if not any
     */
    @Nullable
    public Entity getTarget() {
        return target;
    }

    /**
     * Changes the entity target.
     *
     * @param target the new entity target, null to remove
     */
    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }

    public MoveControl getMoveControl() {
        return moveControl;
    }

    public LookControl getLookControl() {
        return lookControl;
    }

    public JumpControl getJumpControl() {
        return jumpControl;
    }

    public BodyRotationControl getBodyRotationControl() {
        return bodyRotationControl;
    }

    /**
     * Gets the vanilla-faithful pathfinding navigation for this creature.
     *
     * @return the navigation
     */
    public PathNavigation getNavigation() {
        return pathNavigation;
    }

    public GoalSelector getGoalSelector() {
        return goalSelector;
    }

    public GoalSelector getTargetSelector() {
        return targetSelector;
    }

    public Sensing getSensing() {
        return sensing;
    }

    public Random getRandom() {
        return random;
    }

    /**
     * Whether this creature is being ridden by a controlling passenger for the purposes of AI. Vanilla
     * goals stop self-navigating while a mob {@code isVehicle()}; here only living passengers count, so
     * cosmetic passengers (such as a debug text display) do not disable the creature's own movement.
     *
     * @return true if at least one passenger is a living entity
     */
    public boolean isBeingRidden() {
        for (Entity passenger : getPassengers()) {
            if (passenger instanceof LivingEntity) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Brain<? extends EntityCreature> getBrain() {
        return (Brain<? extends EntityCreature>) this.brain;
    }

    @SuppressWarnings("unchecked")
    private void tickBrain() {
        final Instance instance = getInstance();
        if (instance != null) ((Brain<EntityCreature>) this.brain).tick(instance, this);
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(Entity target, boolean swingHand) {
        if (swingHand)
            swingMainHand();
        EntityAttackEvent attackEvent = new EntityAttackEvent(this, target);
        EventDispatcher.call(attackEvent);
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     * <p>
     * This does not trigger the hand animation.
     *
     * @param target the entity target
     */
    public void attack(Entity target) {
        attack(target, false);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    @Override
    public Acquirable<? extends EntityCreature> acquirable() {
        return (Acquirable<? extends EntityCreature>) super.acquirable();
    }
}

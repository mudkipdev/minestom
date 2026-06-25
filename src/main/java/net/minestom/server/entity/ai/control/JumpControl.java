package net.minestom.server.entity.ai.control;

import net.minestom.server.ServerFlag;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.pathfinding.PathBlocks;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.PotionEffect;

public class JumpControl implements Control {
    private final EntityCreature mob;
    protected boolean jump;
    private int noJumpDelay;

    public JumpControl(final EntityCreature mob) {
        this.mob = mob;
    }

    public void jump() {
        this.jump = true;
    }

    public void tick() {
        final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        if (this.jump) {
            if (this.isInLiquid()) {
                // Vanilla rises in fluids each tick (buoyancy), independent of ground/cooldown.
                this.mob.addVelocity(0.0, 0.04 * tps, 0.0);
            } else if (this.mob.isOnGround() && this.noJumpDelay == 0) {
                final double jumpPower = this.jumpPower();
                if (jumpPower > 1.0E-5) {
                    final double power = jumpPower * tps;
                    final double delta = power - this.mob.getVelocity().y();
                    if (delta > 0.0) {
                        this.mob.addVelocity(0.0, delta, 0.0);
                    }
                }
                this.noJumpDelay = 10;
            }
        } else {
            // Vanilla resets the jump cooldown whenever the mob is not jumping, so a fresh jump is
            // immediate rather than waiting out a stale cooldown.
            this.noJumpDelay = 0;
        }

        if (this.noJumpDelay > 0) {
            this.noJumpDelay--;
        }
        this.jump = false;
    }

    private double jumpPower() {
        return this.mob.getAttribute(Attribute.JUMP_STRENGTH).getValue() * this.blockJumpFactor() + this.jumpBoostPower();
    }

    private double jumpBoostPower() {
        final int amplifier = this.mob.getEffectLevel(PotionEffect.JUMP_BOOST);
        return amplifier >= 0 ? 0.1 * (amplifier + 1.0) : 0.0;
    }

    private double blockJumpFactor() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return 1.0;
        }
        final double jumpFactorHere = instance.getBlock(this.mob.getPosition()).compare(Block.HONEY_BLOCK) ? 0.5 : 1.0;
        if (jumpFactorHere != 1.0) {
            return jumpFactorHere;
        }
        final Block below = instance.getBlock(this.mob.getPosition().sub(0.0, 0.500001, 0.0));
        return below.compare(Block.HONEY_BLOCK) ? 0.5 : 1.0;
    }

    private boolean isInLiquid() {
        final Instance instance = this.mob.getInstance();
        if (instance == null) {
            return false;
        }
        final Block block = instance.getBlock(this.mob.getPosition());
        return PathBlocks.isWater(block) || PathBlocks.isLava(block);
    }
}

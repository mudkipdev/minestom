package net.minestom.server.entity.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minestom.server.entity.ai.goal.FloatGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.SilverfishMergeWithStoneGoal;
import net.minestom.server.entity.ai.goal.target.HurtByTargetGoal;
import net.minestom.server.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;

import java.util.Map;
import java.util.Random;

public class Silverfish extends Monster {
    private SilverfishWakeUpFriendsGoal friendsGoal;

    public Silverfish() {
        super(EntityType.SILVERFISH);
        this.friendsGoal = new SilverfishWakeUpFriendsGoal(this);
        getGoalSelector().addGoal(1, new FloatGoal(this));
        getGoalSelector().addGoal(1, new ClimbOnTopOfPowderSnowGoal(this));
        getGoalSelector().addGoal(3, this.friendsGoal);
        getGoalSelector().addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        getGoalSelector().addGoal(5, new SilverfishMergeWithStoneGoal(this));

        getTargetSelector().addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        getTargetSelector().addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean damage(final Damage damage) {
        if (this.friendsGoal != null && (damage.getAttacker() != null || DamageType.MAGIC.equals(damage.getType()))) {
            this.friendsGoal.notifyHurt();
        }

        return super.damage(damage);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.ENTITY_SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(Damage damage) {
        return SoundEvent.ENTITY_SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.ENTITY_SILVERFISH_DEATH;
    }

    private static class SilverfishWakeUpFriendsGoal extends Goal {
        private static final Map<Block, Block> INFESTED_TO_HOST = Map.of(
                Block.INFESTED_STONE, Block.STONE,
                Block.INFESTED_COBBLESTONE, Block.COBBLESTONE,
                Block.INFESTED_STONE_BRICKS, Block.STONE_BRICKS,
                Block.INFESTED_MOSSY_STONE_BRICKS, Block.MOSSY_STONE_BRICKS,
                Block.INFESTED_CRACKED_STONE_BRICKS, Block.CRACKED_STONE_BRICKS,
                Block.INFESTED_CHISELED_STONE_BRICKS, Block.CHISELED_STONE_BRICKS,
                Block.INFESTED_DEEPSLATE, Block.DEEPSLATE
        );

        private final Silverfish silverfish;
        private int lookForFriends;

        public SilverfishWakeUpFriendsGoal(final Silverfish silverfish) {
            this.silverfish = silverfish;
        }

        public void notifyHurt() {
            if (this.lookForFriends == 0) {
                this.lookForFriends = this.adjustedTickDelay(20);
            }
        }

        @Override
        public boolean canUse() {
            return this.lookForFriends > 0;
        }

        @Override
        public void tick() {
            this.lookForFriends--;
            if (this.lookForFriends <= 0) {
                Instance instance = this.silverfish.getInstance();
                if (instance == null) {
                    return;
                }

                Random random = this.silverfish.getRandom();
                Point basePos = this.silverfish.getPosition();
                int baseX = basePos.blockX();
                int baseY = basePos.blockY();
                int baseZ = basePos.blockZ();

                for (int yOff = 0; yOff <= 5 && yOff >= -5; yOff = (yOff <= 0 ? 1 : 0) - yOff) {
                    for (int xOff = 0; xOff <= 10 && xOff >= -10; xOff = (xOff <= 0 ? 1 : 0) - xOff) {
                        for (int zOff = 0; zOff <= 10 && zOff >= -10; zOff = (zOff <= 0 ? 1 : 0) - zOff) {
                            int x = baseX + xOff;
                            int y = baseY + yOff;
                            int z = baseZ + zOff;
                            if (!instance.isChunkLoaded(x >> 4, z >> 4)) {
                                continue;
                            }

                            Block block = instance.getBlock(x, y, z);
                            Block host = hostStateByInfested(block);
                            if (host != null) {
                                instance.setBlock(x, y, z, host);
                                if (random.nextBoolean()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        private static Block hostStateByInfested(final Block infested) {
            for (Map.Entry<Block, Block> entry : INFESTED_TO_HOST.entrySet()) {
                if (entry.getKey().compare(infested)) {
                    return entry.getValue();
                }
            }

            return null;
        }
    }
}

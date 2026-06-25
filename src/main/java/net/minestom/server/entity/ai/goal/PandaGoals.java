package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.instance.Instance;

final class PandaGoals {
    private PandaGoals() {
    }

    static PandaMeta.Gene getVariant(final EntityCreature mob) {
        if (mob.getEntityMeta() instanceof PandaMeta meta) {
            return getVariantFromGenes(meta.getMainGene(), meta.getHiddenGene());
        }
        return PandaMeta.Gene.NORMAL;
    }

    static boolean isScared(final EntityCreature mob) {
        if (getVariant(mob) != PandaMeta.Gene.WORRIED) {
            return false;
        }
        final Instance instance = mob.getInstance();
        return instance != null && instance.getWeather().thunderLevel() > 0.0F;
    }

    static boolean canPerformAction(final EntityCreature mob) {
        if (!(mob.getEntityMeta() instanceof PandaMeta meta)) {
            return false;
        }
        return !meta.isOnBack() && !isScared(mob) && meta.getEatTimer() <= 0 && !meta.isRolling() && !meta.isSitting();
    }

    private static PandaMeta.Gene getVariantFromGenes(final PandaMeta.Gene mainGene, final PandaMeta.Gene hiddenGene) {
        if (isRecessive(mainGene)) {
            return mainGene == hiddenGene ? mainGene : PandaMeta.Gene.NORMAL;
        }
        return mainGene;
    }

    private static boolean isRecessive(final PandaMeta.Gene gene) {
        return gene == PandaMeta.Gene.BROWN || gene == PandaMeta.Gene.WEAK;
    }
}

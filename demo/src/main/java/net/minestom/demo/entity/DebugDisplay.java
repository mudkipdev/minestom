package net.minestom.demo.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.WrappedGoal;
import net.minestom.server.entity.ai.navigation.PathNavigation;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.pathfinding.Path;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Attaches a floating text display above a creature's head showing live AI/pathfinding debug info. The
 * display rides the creature as a passenger (smooth client interpolation, no gravity) using a meta
 * translation offset, and is removed automatically when the creature is removed. Cosmetic passengers
 * like this do not disable the creature's AI (see {@link EntityCreature#isBeingRidden()}).
 */
public final class DebugDisplay {
    private DebugDisplay() {
    }

    public static void attach(final EntityCreature mob) {
        final Entity display = new Entity(EntityType.TEXT_DISPLAY);

        display.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setUseDefaultBackground(true);
            meta.setViewRange(2.0F);
            meta.setTranslation(new Vec(0.0, mob.getBoundingBox().height() + 0.1, 0.0));
        });

        display.setInstance(mob.getInstance(), mob.getPosition()).thenRun(() -> mob.addPassenger(display));

        final AtomicInteger ticks = new AtomicInteger();
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (mob.isRemoved() || mob.getInstance() == null) {
                display.remove();
                return TaskSchedule.stop();
            }

            display.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(debugText(mob)));
            // Re-assert the mount once a second. addPassenger re-sends the passengers packet to all
            // current viewers, re-syncing any client (e.g. after relogging) that lost the mount and would
            // otherwise see the display detached from the creature.
            if (ticks.incrementAndGet() % 10 == 0 && display.getInstance() != null) {
                mob.addPassenger(display);
            }
            return TaskSchedule.tick(2);
        });
    }

    private static Component debugText(final EntityCreature mob) {
        final PathNavigation navigation = mob.getNavigation();
        final Path path = navigation.getPath();
        final String state = navigation.isDone() ? "idle" : "walking";
        final String pathText = path == null ? "-" : path.getNextNodeIndex() + "/" + path.getNodeCount();
        final Entity target = mob.getTarget();
        final String targetText = target == null ? "-" : target.getEntityType().key().value();
        return Component.text(mob.getEntityType().key().value(), NamedTextColor.AQUA)
                .appendNewline()
                .append(Component.text("goal: " + runningGoal(mob), NamedTextColor.LIGHT_PURPLE))
                .appendNewline()
                .append(Component.text("state: " + state, NamedTextColor.WHITE))
                .appendNewline()
                .append(Component.text("path: " + pathText, NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("target: " + targetText, NamedTextColor.YELLOW));
    }

    private static String runningGoal(final EntityCreature mob) {
        WrappedGoal best = null;
        for (final WrappedGoal goal : mob.getGoalSelector().getAvailableGoals()) {
            if (goal.isRunning() && (best == null || goal.getPriority() < best.getPriority())) {
                best = goal;
            }
        }
        if (best == null) return "-";
        final String name = best.getGoal().getClass().getSimpleName();
        return name.endsWith("Goal") ? name.substring(0, name.length() - 4) : name;
    }
}

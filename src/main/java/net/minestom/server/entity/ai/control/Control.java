package net.minestom.server.entity.ai.control;

public interface Control {
    default float rotateTowards(float fromAngle, float toAngle, float maxRot) {
        float diff = degreesDifference(fromAngle, toAngle);
        float diffClamped = clamp(diff, -maxRot, maxRot);
        return fromAngle + diffClamped;
    }

    static float rotlerp(float fromAngle, float toAngle, float maxRot) {
        float diff = degreesDifference(fromAngle, toAngle);
        float diffClamped = clamp(diff, -maxRot, maxRot);
        return toAngle - diffClamped;
    }

    static float degreesDifference(float fromAngle, float toAngle) {
        return wrapDegrees(toAngle - fromAngle);
    }

    static float degreesDifferenceAbs(float angleA, float angleB) {
        return Math.abs(degreesDifference(angleA, angleB));
    }

    static float wrapDegrees(float angle) {
        float normalizedAngle = angle % 360.0F;
        if (normalizedAngle >= 180.0F) {
            normalizedAngle -= 360.0F;
        }

        if (normalizedAngle < -180.0F) {
            normalizedAngle += 360.0F;
        }

        return normalizedAngle;
    }

    static double wrapDegrees(double angle) {
        double normalizedAngle = angle % 360.0;
        if (normalizedAngle >= 180.0) {
            normalizedAngle -= 360.0;
        }

        if (normalizedAngle < -180.0) {
            normalizedAngle += 360.0;
        }

        return normalizedAngle;
    }

    static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }
}

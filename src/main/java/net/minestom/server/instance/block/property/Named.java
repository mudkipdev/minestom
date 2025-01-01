package net.minestom.server.instance.block.property;

/**
 * Represents something that has a string ID.
 * @see EnumProperty
 */
public interface Named {
    /**
     * Returns the string ID of this value.
     * @return The string ID of this value.
     */
    String getName();
}

package net.minestom.server.instance.block.property;

/**
 * TODO: This will be auto-generated later along with the enums hopefully
 */
interface BlockProperties {
    BlockProperty<Boolean> WATERLOGGED = new BooleanProperty("waterlogged");
    BlockProperty<Direction> FACING = new EnumProperty<>("facing", Direction.class);

    enum Direction {
        NORTH,
        EAST,
        WEST,
        SOUTH,
        UP,
        DOWN
    }
}

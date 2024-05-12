package mapgen.blocks;

import java.awt.*;

public class BlockColor {

    public static Color EMPTY = Color.LIGHT_GRAY;
    public static Color HOOKABLE = Color.DARK_GRAY;
    public static Color FREEZE = Color.GRAY;
    public static Color SPAWN = Color.WHITE;
    public static Color START = Color.GREEN;
    public static Color FINISH = Color.ORANGE;
    public static Color FLOOD = Color.RED;

    public static Color getColor(int blockType) {
        return switch (blockType) {
            case 1 -> HOOKABLE;
            case 9 -> FREEZE;
            case 192 -> SPAWN;
            case 33 -> START;
            case 34 -> FINISH;
            case 999 -> FLOOD;
            default -> EMPTY;
        };
    }
}

package io.github.ozkanceng.ozlauncher.ui;

/** Pure D-pad geometry, separated so every edge can be regression-tested. */
public final class GridNavigator {
    public enum Direction { LEFT, RIGHT, UP, DOWN }
    public static final int EDGE_TOP = -1;
    public static final int EDGE_BOTTOM = -2;

    private GridNavigator() { }

    public static int next(int index, Direction direction, int columns, int size) {
        if (index < 0 || index >= size || columns < 1) return index;
        switch (direction) {
            case LEFT: return index % columns == 0 ? index : index - 1;
            case RIGHT: return index % columns == columns - 1 || index + 1 >= size ? index : index + 1;
            case UP: return index - columns < 0 ? EDGE_TOP : index - columns;
            case DOWN: return index + columns >= size ? EDGE_BOTTOM : index + columns;
            default: return index;
        }
    }
}

package io.github.ozkanceng.ozlauncher.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GridNavigatorTest {
    @Test public void movesWithinGrid() {
        assertEquals(7, GridNavigator.next(1, GridNavigator.Direction.DOWN, 6, 20));
        assertEquals(6, GridNavigator.next(7, GridNavigator.Direction.LEFT, 6, 20));
        assertEquals(8, GridNavigator.next(7, GridNavigator.Direction.RIGHT, 6, 20));
        assertEquals(1, GridNavigator.next(7, GridNavigator.Direction.UP, 6, 20));
    }

    @Test public void horizontalEdgesDoNotWrap() {
        assertEquals(6, GridNavigator.next(6, GridNavigator.Direction.LEFT, 6, 20));
        assertEquals(5, GridNavigator.next(5, GridNavigator.Direction.RIGHT, 6, 20));
        assertEquals(19, GridNavigator.next(19, GridNavigator.Direction.RIGHT, 6, 20));
    }

    @Test public void verticalEdgesAreExplicit() {
        assertEquals(GridNavigator.EDGE_TOP,
                GridNavigator.next(2, GridNavigator.Direction.UP, 6, 20));
        assertEquals(GridNavigator.EDGE_BOTTOM,
                GridNavigator.next(18, GridNavigator.Direction.DOWN, 6, 20));
    }
}

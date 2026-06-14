package com.azukii.diet.gui.screen;

public class FoodStatsRenderState {
    private static boolean open = false;

    public static boolean isOpen() {
        return open;
    }
    public static void toggle() {
        open = !open;
    }
    public static void close() {
        open = false;
    }
}

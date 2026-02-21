package eu.gflash.notifmod.util;

import static net.minecraft.util.ARGB.color;

import net.minecraft.util.CommonColors;

public final class Color extends CommonColors {
    public static final int LIGHT_YELLOW    = color(255, 255, 196);
    public static final int LIGHT_MAGENTA   = color(255, 196, 255);
    public static final int LIGHT_CYAN      = color(196, 255, 255);
    public static final int MAGENTA         = color(255,   0, 255);
    public static final int CYAN            = color(0,   255, 255);

    public static final int CFG_DEF_MESSAGE_INDICATOR   = CYAN;
    public static final int CFG_DEF_MESSAGE_TXT_MATCH   = CYAN;
    public static final int CFG_DEF_MENTION_INDICATOR   = MAGENTA;
    public static final int CFG_DEF_MENTION_TXT_MATCH   = MAGENTA;

    public static final int TEXT_LIGHT                  = WHITE;
    public static final int TEXT_DARK                   = DARK_GRAY;
    public static final int TEXT_BTN_TIMER_REPEATING    = LIGHT_MAGENTA;
    public static final int TEXT_BTN_TIMER_NORMAL       = LIGHT_CYAN;
    public static final int TEXT_BTN_TIMER_ABORTED      = LIGHT_YELLOW;

    public static final int LIST_ENTRY_HOVER_HIGHLIGHT  = color(120, DARK_GRAY);

    private Color() {}
}

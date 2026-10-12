package com.hotel.ui.components;

import java.awt.*;

/**
 * Theme — central colour palette, fonts and sizing constants.
 * Change values here to restyle the entire application.
 */
public class Theme {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(26,  86, 219);   // deep blue
    public static final Color PRIMARY_DARK   = new Color(17,  58, 159);
    public static final Color PRIMARY_LIGHT  = new Color(219, 234, 254);

    public static final Color ACCENT         = new Color(5,  150, 105);   // emerald
    public static final Color ACCENT_LIGHT   = new Color(209, 250, 229);

    public static final Color DANGER         = new Color(220,  38,  38);
    public static final Color DANGER_LIGHT   = new Color(254, 226, 226);

    public static final Color WARNING        = new Color(217, 119,   6);
    public static final Color WARNING_LIGHT  = new Color(254, 243, 199);

    public static final Color BG_MAIN        = new Color(248, 250, 252);
    public static final Color BG_SIDEBAR     = new Color( 15,  23,  42);  // dark navy
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color BG_TABLE_ALT   = new Color(248, 250, 252);

    public static final Color TEXT_PRIMARY   = new Color( 15,  23,  42);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color TEXT_ON_DARK   = new Color(226, 232, 240);
    public static final Color TEXT_SIDEBAR_ACTIVE = Color.WHITE;
    public static final Color SIDEBAR_HOVER  = new Color( 30,  41,  59);
    public static final Color SIDEBAR_ACTIVE = new Color( 37,  99, 235);

    public static final Color BORDER         = new Color(226, 232, 240);
    public static final Color BORDER_FOCUS   = PRIMARY;

    // Status chip colours
    public static final Color STATUS_AVAILABLE   = new Color(  5, 150, 105);
    public static final Color STATUS_BOOKED      = new Color(217, 119,   6);
    public static final Color STATUS_MAINTENANCE = new Color(220,  38,  38);
    public static final Color STATUS_CONFIRMED   = new Color( 37,  99, 235);
    public static final Color STATUS_CHECKED_IN  = ACCENT;
    public static final Color STATUS_CHECKED_OUT = TEXT_SECONDARY;
    public static final Color STATUS_CANCELLED   = DANGER;
    public static final Color STATUS_PAID        = ACCENT;
    public static final Color STATUS_PENDING     = WARNING;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,   22);
    public static final Font FONT_HEADING   = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font FONT_SUBHEAD   = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_NAV       = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_NAV_BOLD  = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_MONO      = new Font("Consolas",  Font.PLAIN,  12);
    public static final Font FONT_TABLE_HDR = new Font("Segoe UI", Font.BOLD,   12);
    public static final Font FONT_TABLE     = new Font("Segoe UI", Font.PLAIN,  12);
    public static final Font FONT_BTN       = new Font("Segoe UI", Font.BOLD,   12);
    public static final Font FONT_INPUT     = new Font("Segoe UI", Font.PLAIN,  13);

    // ── Sizes ─────────────────────────────────────────────────────────────────
    public static final int   SIDEBAR_WIDTH  = 220;
    public static final int   ROW_HEIGHT     = 34;
    public static final int   CORNER_RADIUS  = 8;
    public static final Insets CARD_PAD      = new Insets(20, 20, 20, 20);

    private Theme() {}
}

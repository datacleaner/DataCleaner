import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class FontsAndColorsResourceBundle extends ListResourceBundle{
    public static final float FONT_SIZE_SMALL = 11f;

    private static final Map<String, Font> fonts;

    @Deprecated
    public static final Font FONT_UBUNTU_PLAIN;
    @Deprecated
    public static final Font FONT_UBUNTU_BOLD;
    @Deprecated
    public static final Font FONT_UBUNTU_ITALIC;
    @Deprecated
    public static final Font FONT_UBUNTU_BOLD_ITALIC;

    @Deprecated
    public static final Font FONT_OPENSANS_PLAIN;
    @Deprecated
    public static final Font FONT_OPENSANS_BOLD;
    @Deprecated
    public static final Font FONT_OPENSANS_ITALIC;
    @Deprecated
    public static final Font FONT_OPENSANS_BOLD_ITALIC;

    private static final Font FONT_MULI_PLAIN;
    private static final Font FONT_MULI_BOLD;
    private static final Font FONT_MULI_LIGHT;
    private static final Font FONT_MULI_LIGHT_ITALIC;
    private static final Font FONT_LATO_BOLD;
    private static final Font FONT_LATO_BOLD_ITALIC;

    public static final Font FONT_FONTAWESOME;

    static {
        fonts = new HashMap<>();

        final Font[] fontArray = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (final Font font : fontArray) {
            if (font.isPlain()) {
                fonts.put(font.getName(), font);
            }
        }

        FONT_MULI_PLAIN = createFont("fonts/Muli.ttf");
        FONT_MULI_BOLD = createFont("fonts/Muli-Bold.ttf");
        FONT_MULI_LIGHT = createFont("fonts/Muli-Light.ttf");
        FONT_MULI_LIGHT_ITALIC = createFont("fonts/Muli-LightItalic.ttf");
        FONT_LATO_BOLD = createFont("fonts/Lato-Bold.ttf");
        FONT_LATO_BOLD_ITALIC = createFont("fonts/Lato-BoldItalic.ttf");

        FONT_UBUNTU_PLAIN = FONT_MULI_PLAIN;
        FONT_UBUNTU_ITALIC = FONT_MULI_LIGHT_ITALIC;
        FONT_UBUNTU_BOLD = FONT_LATO_BOLD;
        FONT_UBUNTU_BOLD_ITALIC = FONT_LATO_BOLD_ITALIC;

        FONT_OPENSANS_PLAIN = FONT_MULI_PLAIN;
        FONT_OPENSANS_ITALIC = FONT_MULI_LIGHT_ITALIC;
        FONT_OPENSANS_BOLD = FONT_LATO_BOLD;
        FONT_OPENSANS_BOLD_ITALIC = FONT_LATO_BOLD_ITALIC;

        FONT_FONTAWESOME = createFont("fonts/FontAwesome-4.3.0.ttf").deriveFont(14f);

        fonts.put(FONT_MULI_PLAIN.getName(), FONT_MULI_PLAIN);
    }

    public static final Font FONT_BANNER = FONT_MULI_LIGHT.deriveFont(21f);
    public static final Font FONT_HEADER1 = FONT_MULI_LIGHT.deriveFont(18f);
    public static final Font FONT_HEADER2 = FONT_MULI_LIGHT.deriveFont(16f);
    public static final Font FONT_MONOSPACE = new FontUIResource("Monospaced", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = FONT_MULI_PLAIN.deriveFont(14f);
    public static final Font FONT_NORMAL = FONT_MULI_PLAIN.deriveFont(13f);
    public static final Font FONT_BOLD = FONT_MULI_BOLD.deriveFont(13f);
    public static final Font FONT_SMALL = FONT_MULI_PLAIN.deriveFont(FONT_SIZE_SMALL);
    public static final Font FONT_TABLE_HEADER = FONT_NORMAL.deriveFont(Font.BOLD);

    public static final int SCROLL_UNIT_INCREMENT = 20;

    // blue base color of DC styling (#05b9f0)
    public static final Color BG_COLOR_BLUE_MEDIUM = new ColorUIResource(5, 185, 240);
    public static final Color BG_COLOR_BLUE_BRIGHT = slightlyBrighter(BG_COLOR_BLUE_MEDIUM);
    public static final Color BG_COLOR_BLUE_DARK = slightlyDarker(BG_COLOR_BLUE_MEDIUM);

    // green base color of DC styling (#70be44)
    public static final Color BG_COLOR_GREEN_MEDIUM = new ColorUIResource(122, 190, 68);
    public static final Color BG_COLOR_GREEN_BRIGHT = slightlyBrighter(BG_COLOR_GREEN_MEDIUM);
    public static final Color BG_COLOR_GREEN_DARK = slightlyDarker(BG_COLOR_GREEN_MEDIUM);

    // orange base color of DC styling (#f58132)
    public static final Color BG_COLOR_ORANGE_MEDIUM = new ColorUIResource(245, 129, 50);
    public static final Color BG_COLOR_ORANGE_BRIGHT = slightlyBrighter(BG_COLOR_ORANGE_MEDIUM);
    public static final Color BG_COLOR_ORANGE_DARK = slightlyDarker(BG_COLOR_ORANGE_MEDIUM);

    // white with 10% alpha/opacity
    public static final Color BG_SEMI_TRANSPARENT = new ColorUIResource(new Color(0.0f, 0.0f, 0.0f, 0.05f));

    // pale yellow color which work fine for information/help text fields.
    // #f4f4d3
    @Deprecated
    public static final Color BG_COLOR_PALE_YELLOW = new ColorUIResource(244, 244, 211);

    // white
    public static final Color BG_COLOR_BRIGHTEST = new ColorUIResource(Color.WHITE);

    // #e1e1e1 (silver-ish)
    public static final Color BG_COLOR_BRIGHT = new ColorUIResource(245, 245, 245);

    // slightly darker than BRIGHT
    public static final Color BG_COLOR_LESS_BRIGHT = new ColorUIResource(220, 220, 220);

    // #a0a0a0
    public static final Color BG_COLOR_MEDIUM = new ColorUIResource(130, 140, 150);

    public static final Color BG_COLOR_LESS_DARK = new ColorUIResource(55, 55, 55);

    public static final Color BG_COLOR_DARK = new ColorUIResource(33, 33, 33);

    public static final Color BG_COLOR_DARKEST = new ColorUIResource(Color.BLACK);

    public static final Color COLOR_DEFAULT_BACKGROUND = BG_COLOR_BRIGHTEST;
    public static final Color COLOR_WELL_BACKGROUND = BG_COLOR_BRIGHT;
    public static final Color COLOR_ALTERNATIVE_BACKGROUND = BG_COLOR_DARK;

    // additional colors, only intended for special widget coloring such as
    // charts etc.

    // Green: #70be44
    @Deprecated
    public static final Color ADDITIONAL_COLOR_GREEN_BRIGHT = BG_COLOR_GREEN_MEDIUM;

    // Red: #d32424
    public static final Color ADDITIONAL_COLOR_RED_BRIGHT = new ColorUIResource(211, 36, 36);

    // Purple: #d3249c
    public static final Color ADDITIONAL_COLOR_PURPLE_BRIGHT = new ColorUIResource(211, 36, 156);

    // Cyan: #24d1d3
    public static final Color ADDITIONAL_COLOR_CYAN_BRIGHT = new ColorUIResource(36, 209, 211);


    @Override
    protected Object[][] getContents() {
        return new Object[0][];
    }

    private static Font createFont(final String path) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT,
                    FontsAndColorsResourceBundle.class.getClassLoader().getResourceAsStream(path));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}

package its_meow.quickteleports.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class FTC extends TextComponentStyled {

    public FTC(String msg, TextFormatting color) {
        super(msg, Style.EMPTY.withColor(color));
    }
    
    public FTC(TextFormatting color, String msg) {
        super(msg, Style.EMPTY.withColor(color));
    }
    
    public FTC(TextFormatting color, Form form, String msg) {
        super(msg, form.getStyle(color));
    }
    
    public FTC(String msg, TextFormatting color, Form form) {
        super(msg, form.getStyle(color));
    }
    
    public FTC(String msg, Form form) {
        super(msg, form.getStyle());
    }
    
    public FTC(String msg, TextFormatting color, boolean bold) {
        super(msg, Style.EMPTY.withColor(color).withBold(bold));
    }
    
    public FTC(String msg, TextFormatting color, boolean bold, boolean italic) {
        super(msg, Style.EMPTY.withColor(color).withBold(bold).withItalic(italic));
    }
    
    public FTC(String msg, boolean bold) {
        super(msg, Style.EMPTY.withBold(bold));
    }
    
    public FTC(String msg, boolean bold, boolean italic) {
        super(msg, Style.EMPTY.withBold(bold).withItalic(italic));
    }
    
    public FTC(String msg, TextFormatting color, boolean bold, boolean italic, boolean strikethrough, boolean underline) {
        super(msg, Style.EMPTY.withBold(bold).withItalic(italic).setStrikethrough(strikethrough).setUnderlined(underline));
    }
    
    public static enum Form {
        BOLD(true, false, false, false),
        ITALIC(false, true, false, false),
        STRIKETHROUGH(false, false, true, false),
        UNDERLINE(false, false, false, true),
        BOLD_UNDERLINE(true, false, false, true),
        ITALIC_UNDERLINE(false, true, false, true),
        BOLD_ITALIC_UNDERLINE(true, true, false, true),
        BOLD_STRIKETHROUGH(true, false, true, false),
        ITALIC_STRIKETHROUGH(false, true, true, false),
        BOLD_ITALIC_STRIKETHROUGH(true, true, true, false),
        BOLD_ITALIC_UNDERLINE_STRIKETHROUGH(true, true, true, true);
        
        public final boolean bold;
        public final boolean italic;
        public final boolean strikethrough;
        public final boolean underline;
        
        private Form(boolean bold, boolean italic, boolean strikethrough, boolean underline) {
            this.bold = bold;
            this.italic = italic;
            this.strikethrough = strikethrough;
            this.underline = underline;
        }

        public Style getStyle(TextFormatting color) {
            return getStyle().withColor(color);
        }

        public Style getStyle() {
            return Style.EMPTY.withBold(bold).withItalic(italic).setStrikethrough(strikethrough).setUnderlined(underline);
        }
        
        public Style applyToStyle(Style style) {
            return style.withBold(bold).withItalic(italic).setStrikethrough(strikethrough).setUnderlined(underline);
        }
        
        public String toString() {
            return "--(This is a bug, report this to QuickTeleports with where you find it at)--";
        }
    }

}
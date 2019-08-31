package its_meow.quickteleports.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class FTC extends TextComponentStyled {

    public FTC(String msg, TextFormatting color) {
        super(msg, new Style().setColor(color));
    }
    
    public FTC(TextFormatting color, String msg) {
        super(msg, new Style().setColor(color));
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
        super(msg, new Style().setColor(color).setBold(bold));
    }
    
    public FTC(String msg, TextFormatting color, boolean bold, boolean italic) {
        super(msg, new Style().setColor(color).setBold(bold).setItalic(italic));
    }
    
    public FTC(String msg, boolean bold) {
        super(msg, new Style().setBold(bold));
    }
    
    public FTC(String msg, boolean bold, boolean italic) {
        super(msg, new Style().setBold(bold).setItalic(italic));
    }
    
    public FTC(String msg, TextFormatting color, boolean bold, boolean italic, boolean strikethrough, boolean underline) {
        super(msg, new Style().setBold(bold).setItalic(italic).setStrikethrough(strikethrough).setUnderlined(underline));
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
            return getStyle().setColor(color);
        }

        public Style getStyle() {
            return new Style().setBold(bold).setItalic(italic).setStrikethrough(strikethrough).setUnderlined(underline);
        }
        
        public Style applyToStyle(Style style) {
            return style.setBold(bold).setItalic(italic).setStrikethrough(strikethrough).setUnderlined(underline);
        }
        
        public String toString() {
            return "--(This is a bug, report this to ClaimIt with where you find it at)--";
        }
    }

}
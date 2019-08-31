package its_meow.quickteleports.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

public class TextComponentStyled extends TextComponentString {

    public TextComponentStyled(String msg, Style style) {
        super(msg);
        this.setStyle(style);
    }

}
package its_meow.quickteleports.util;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class TextComponentStyled extends StringTextComponent {

    public TextComponentStyled(String msg, Style style) {
        super(msg);
        this.setStyle(style);
    }

}
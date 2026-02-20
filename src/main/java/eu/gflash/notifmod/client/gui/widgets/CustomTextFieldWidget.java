package eu.gflash.notifmod.client.gui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Text field widget that gets erased when right-clicked.
 * @author Alex811
 */
public class CustomTextFieldWidget extends TextFieldWidget {
    public CustomTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if(super.mouseClicked(click, doubled)) return true;
        return mouseClicked(click.x(), click.y(), click.button());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.isVisible()) return false;
        boolean bl = mouseX >= this.getX() && mouseX < (this.getX() + this.width) && mouseY >= this.getY() && mouseY < (this.getY() + this.height);
        if(!(this.isFocused() && bl && button == 1)) return false;
        this.setText("");
        return true;
    }
}

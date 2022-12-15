package eu.gflash.notifmod.client.gui.widgets;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class CustomButtonWidget extends ButtonWidget {
    public CustomButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, Supplier::get);
    }
}

package eu.gflash.notifmod.client.gui.widgets;

import net.minecraft.client.gui.widget.ButtonWidget;

public class CustomButtonWidget extends ButtonWidget.Text {
    public CustomButtonWidget(int x, int y, int width, int height, net.minecraft.text.Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }
}

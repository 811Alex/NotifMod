package eu.gflash.notifmod.client.gui.widgets;

import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.TranslatableText;

/**
 * Integer slider widget that can take a custom range.
 * @author Alex811
 */
public class CustomIntSliderWidget extends SliderWidget {
    protected final String langKey;
    protected final int min;
    protected final int max;

    /**
     * Slider constructor.
     * @param x slider x coordinate
     * @param y slider y coordinate
     * @param width slider width
     * @param height slider height
     * @param langKey part of the LangKey of the slider's message (the result will be <i>gui.slider.{@code langKey}</i> and it will receive an integer)
     * @param min slider range's min (inclusive)
     * @param max slider range's max (inclusive)
     * @param def slider default value
     */
    public CustomIntSliderWidget(int x, int y, int width, int height, String langKey, int min, int max, int def) {
        super(x, y, width, height, TextUtil.EMPTY, NumUtil.mapValue(def, min, max));
        this.langKey = "gui.slider." + langKey;
        this.min = min;
        this.max = max;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(new TranslatableText(this.langKey, getValue()));
    }

    @Override
    protected void applyValue() {

    }

    public int getValue(){
        return (int) NumUtil.mapValue(value, min, max);
    }

    public void setValue(int value){
        if(value < min || value > max) return;
        this.value = NumUtil.mapValue(value, min, max);
        updateMessage();
    }
}

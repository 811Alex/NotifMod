package eu.gflash.notifmod.client.gui;

import eu.gflash.notifmod.client.gui.widgets.CustomIntSliderWidget;
import eu.gflash.notifmod.client.listeners.ReminderListener;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Reminder time selection GUI.
 * @author Alex811
 */
public class ReminderScreen extends BaseScreen {
    private static final Identifier BACKGROUND = new Identifier("notifmod:textures/gui_reminder.png");
    private static final Text TEXT_TITLE = new TranslatableText("gui.screen.reminder.title");
    private static final Text TEXT_TITLEFIELD = new TranslatableText("gui.screen.reminder.titleField");
    private static final Text TEXT_PRESET_1 = new TranslatableText("gui.screen.reminder.preset1");
    private static final Text TEXT_PRESET_2 = new TranslatableText("gui.screen.reminder.preset2");
    private static final Text TEXT_START = new TranslatableText("gui.screen.reminder.start");
    private CustomIntSliderWidget sliderHours;
    private CustomIntSliderWidget sliderMinutes;
    private CustomIntSliderWidget sliderSeconds;
    private TextFieldWidget titleField;
    private int titleFieldTitleX;
    private int titleFieldTitleY;

    protected ReminderScreen() {
        super(TEXT_TITLE, 250, 128, BACKGROUND);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.draw(matrices, TEXT_TITLEFIELD, titleFieldTitleX, titleFieldTitleY, 4210752);
    }

    @Override
    protected void init() {
        super.init();
        ModConfig.Reminder settings = ModConfig.getInstance().reminder;
        int titleFieldTitleW = textRenderer.getWidth(TEXT_TITLEFIELD);
        titleFieldTitleX = wX(titleFieldTitleW);
        titleFieldTitleY = wY() + 4;
        addChild(titleField = new TextFieldWidget(this.textRenderer, wXr(), wY(16), 240 - titleFieldTitleW - widgetSpacing, 16, Text.of("test")));
        addChild(sliderHours = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "hours", 0, 24, 0));
        addChild(sliderMinutes = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "minutes", 0, 60, 0));
        addChild(sliderSeconds = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "seconds", 0, 60, 0));
        addButton(new ButtonWidget(wX(70), wY(), 70, 20, TEXT_PRESET_1, button -> setTime(settings.pre1Seconds)));
        addButton(new ButtonWidget(wX(96), wY(), 96, 20, TEXT_START, button -> {
            ReminderListener.start(getTime(), titleField.getText());
            onClose();
        }));
        addButton(new ButtonWidget(wX(70), wY(), 70, 20, TEXT_PRESET_2, button -> setTime(settings.pre2Seconds)));
        setTime(settings.defSeconds);
    }

    /**
     * Set all sliders.
     * @param seconds total seconds
     */
    private void setTime(int seconds){
        Triple<Integer, Integer, Integer> hms = NumUtil.secToHMS(seconds);
        sliderHours.setValue(hms.getLeft());
        sliderMinutes.setValue(hms.getMiddle());
        sliderSeconds.setValue(hms.getRight());
    }

    /**
     * Get selected time in seconds.
     * @return seconds selected
     */
    private int getTime(){
        return NumUtil.HMSToSec(sliderHours.getValue(), sliderMinutes.getValue(), sliderSeconds.getValue());
    }

    public static void open(){
        MinecraftClient.getInstance().openScreen(new ReminderScreen());
    }
}

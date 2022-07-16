package eu.gflash.notifmod.client.gui;

import eu.gflash.notifmod.client.gui.widgets.CustomIntSliderWidget;
import eu.gflash.notifmod.client.gui.widgets.CustomTextFieldWidget;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.ReminderTimer;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Reminder time selection GUI.
 * @author Alex811
 */
public class ReminderScreen extends BaseScreen {
    private static final Identifier BACKGROUND = new Identifier("notifmod:textures/gui_reminder.png");
    private static final Text TEXT_TITLE = Text.translatable("gui.screen.reminder.title");
    private static final Text TEXT_TITLEFIELD = Text.translatable("gui.screen.reminder.titleField");
    private static final Text TEXT_PRESET_1 = Text.translatable("gui.screen.reminder.preset1");
    private static final Text TEXT_PRESET_2 = Text.translatable("gui.screen.reminder.preset2");
    private static final Text TEXT_START = TextUtil.getWithFormat(Text.translatable("gui.screen.reminder.start"), Formatting.GREEN);
    private static final Text TEXT_REPEAT_YES = Text.translatable("gui.screen.reminder.repeat.yes");
    private static final Text TEXT_REPEAT_NO = Text.translatable("gui.screen.reminder.repeat.no");
    private static final Text TEXT_LIST = Text.translatable("gui.screen.reminder.list");
    private CustomIntSliderWidget sliderHours;
    private CustomIntSliderWidget sliderMinutes;
    private CustomIntSliderWidget sliderSeconds;
    private CustomTextFieldWidget titleField;
    private int titleFieldTitleX;
    private int titleFieldTitleY;
    private boolean repeat = false;

    protected ReminderScreen() {
        super(TEXT_TITLE, 250, 146, BACKGROUND);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.draw(matrices, TEXT_TITLEFIELD, titleFieldTitleX, titleFieldTitleY, 0x404040);
    }

    @Override
    protected void init() {
        super.init();
        ModConfig.Reminder settings = ModConfig.getInstance().reminder;
        int titleFieldTitleW = textRenderer.getWidth(TEXT_TITLEFIELD);
        titleFieldTitleX = wX(titleFieldTitleW);
        titleFieldTitleY = wY() + 4;
        addDrawableChild(titleField = new CustomTextFieldWidget(this.textRenderer, wXr(), wY(16), 240 - titleFieldTitleW - WIDGET_SPACING, 16, TextUtil.EMPTY));
        addDrawableChild(sliderHours = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "hours", 0, 24, 0));
        addDrawableChild(sliderMinutes = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "minutes", 0, 60, 0));
        addDrawableChild(sliderSeconds = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "seconds", 0, 60, 0));
        addDrawableChild(new ButtonWidget(wX(70), wY(), 70, 20, TEXT_PRESET_1, b -> setTime(settings.pre1Seconds)));
        addDrawableChild(new ButtonWidget(wX(96), wY(), 96, 20, TEXT_START, b -> {
            ReminderTimer.startNew(getTime(), titleField.getText(), this.repeat);
            close();
        }));
        addDrawableChild(new ButtonWidget(wXr(), wY(20), 70, 20, TEXT_PRESET_2, b -> setTime(settings.pre2Seconds)));
        addDrawableChild(new ButtonWidget(wX(70), wY(), 70, 20, TEXT_REPEAT_NO, b -> {
            this.repeat = !this.repeat;
            b.setMessage(this.repeat ? TEXT_REPEAT_YES : TEXT_REPEAT_NO);
        }));
        addDrawableChild(new ButtonWidget(wX(), wY(), 168, 20, TEXT_LIST, b -> ReminderListScreen.open()){{
            this.active = !ReminderTimer.getActive().isEmpty();
        }});
        setTime(settings.defSeconds);
        titleField.setMaxLength(34);
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
        MinecraftClient.getInstance().setScreen(new ReminderScreen());
    }
}

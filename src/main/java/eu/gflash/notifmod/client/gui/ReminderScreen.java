package eu.gflash.notifmod.client.gui;

import eu.gflash.notifmod.util.Color;
import eu.gflash.notifmod.client.gui.widgets.CustomButtonWidget;
import eu.gflash.notifmod.client.gui.widgets.CustomIntSliderWidget;
import eu.gflash.notifmod.client.gui.widgets.CustomTextFieldWidget;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.config.ModConfig;
import eu.gflash.notifmod.util.ReminderTimer;
import eu.gflash.notifmod.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Reminder time selection GUI.
 * @author Alex811
 */
public class ReminderScreen extends BaseScreen {
    private static final Identifier REMINDER_SCREEN_BACKGROUND = Identifier.parse("notifmod:textures/gui_reminder.png");
    private static final Component TEXT_TITLE = Component.translatable("gui.screen.reminder.title");
    private static final Component TEXT_TITLEFIELD = Component.translatable("gui.screen.reminder.titleField");
    private static final Component TEXT_PRESET_1 = Component.translatable("gui.screen.reminder.preset1");
    private static final Component TEXT_PRESET_2 = Component.translatable("gui.screen.reminder.preset2");
    private static final Component TEXT_START = TextUtil.getWithFormat(Component.translatable("gui.screen.reminder.start"), ChatFormatting.GREEN);
    private static final Component TEXT_REPEAT_YES = Component.translatable("gui.screen.reminder.repeat.yes");
    private static final Component TEXT_REPEAT_NO = Component.translatable("gui.screen.reminder.repeat.no");
    private static final Component TEXT_LIST = Component.translatable("gui.screen.reminder.list");
    private CustomIntSliderWidget sliderHours;
    private CustomIntSliderWidget sliderMinutes;
    private CustomIntSliderWidget sliderSeconds;
    private CustomTextFieldWidget titleField;
    private int titleFieldTitleX;
    private int titleFieldTitleY;
    private boolean repeat = false;

    protected ReminderScreen() {
        super(TEXT_TITLE, 250, 146, REMINDER_SCREEN_BACKGROUND);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawText(context, TEXT_TITLEFIELD, titleFieldTitleX, titleFieldTitleY, Color.TEXT_DARK);
    }

    @Override
    protected void init() {
        super.init();
        ModConfig.Reminder settings = ModConfig.getInstance().reminder;
        int titleFieldTitleW = font.width(TEXT_TITLEFIELD);
        titleFieldTitleX = wX(titleFieldTitleW);
        titleFieldTitleY = wY() + 4;
        addRenderableWidget(titleField = new CustomTextFieldWidget(this.font, wXr(), wY(16), 240 - titleFieldTitleW - WIDGET_SPACING, 16, TextUtil.EMPTY));
        addRenderableWidget(sliderHours = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "hours", 0, 24, 0));
        addRenderableWidget(sliderMinutes = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "minutes", 0, 60, 0));
        addRenderableWidget(sliderSeconds = new CustomIntSliderWidget(wX(), wY(20), 240, 20, "seconds", 0, 60, 0));
        addRenderableWidget(new CustomButtonWidget(wX(70), wY(), 70, 20, TEXT_PRESET_1, b -> setTime(settings.pre1Seconds)));
        addRenderableWidget(new CustomButtonWidget(wX(96), wY(), 96, 20, TEXT_START, b -> {
            ReminderTimer.startNew(getTime(), titleField.getValue(), this.repeat);
            onClose();
        }));
        addRenderableWidget(new CustomButtonWidget(wXr(), wY(20), 70, 20, TEXT_PRESET_2, b -> setTime(settings.pre2Seconds)));
        addRenderableWidget(new CustomButtonWidget(wX(70), wY(), 70, 20, TEXT_REPEAT_NO, b -> {
            this.repeat = !this.repeat;
            b.setMessage(this.repeat ? TEXT_REPEAT_YES : TEXT_REPEAT_NO);
        }));
        addRenderableWidget(new CustomButtonWidget(wX(), wY(), 168, 20, TEXT_LIST, b -> ReminderListScreen.open()){{
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
        Minecraft.getInstance().setScreen(new ReminderScreen());
    }
}

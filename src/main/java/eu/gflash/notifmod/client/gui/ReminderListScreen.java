package eu.gflash.notifmod.client.gui;

import eu.gflash.notifmod.util.Color;
import eu.gflash.notifmod.client.gui.widgets.CustomButtonWidget;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.util.ReminderTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Active reminder list GUI.
 * @see ReminderListWidget
 * @author Alex811
 */
public class ReminderListScreen extends BaseScreen {
    private static final Identifier REMINDER_LIST_BACKGROUND = Identifier.of("notifmod:textures/gui_reminder_list.png");
    private static final Identifier MASK = Identifier.of("notifmod:textures/gui_reminder_list_mask.png");
    private static final Text TEXT_TITLE = Text.translatable("gui.screen.reminderList.title");
    private static final Text TEXT_ENTRY_UNTITLED = Text.translatable("gui.screen.reminderList.entry.untitled");
    private static final Text TEXT_ENTRY_STOP = Text.translatable("gui.screen.reminderList.entry.stop");
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_HEIGHT = 146;

    protected ReminderListScreen() {
        super(TEXT_TITLE, PANEL_WIDTH, PANEL_HEIGHT, REMINDER_LIST_BACKGROUND);
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, MASK, panelX, panelY, 0, 0, panelWidth, panelHeight, bgWidth, bgHeight);
    }

    @Override
    protected void init() {
        super.init();
        addDrawableChild(new ReminderListWidget());
    }

    public static void open(){
        MinecraftClient.getInstance().setScreen(new ReminderListScreen());
    }

    /**
     * Reminder list widget. Scrollable, entries contain timer title, time left & abort button.
     * @see ReminderEntry
     */
    public class ReminderListWidget extends ElementListWidget<ReminderListWidget.ReminderEntry> {
        private static final int ROW_ELEMENT_GAP = 6;
        private static final int ITEM_HEIGHT = 20;
        private static final int BUTTON_WIDTH = 50;
        private static final int WIDGET_WIDTH = ReminderListScreen.PANEL_WIDTH - (BaseScreen.PANEL_PADDING << 1);
        private static final int WIDGET_HEIGHT = ReminderListScreen.PANEL_HEIGHT - (ITEM_HEIGHT << 1);
        private static final int EXTRA_TOP_PADDING = 2;
        private static final int EXTRA_BOTTOM_PADDING = 4;
        private final int buttonX;
        private final int timeX;
        private final int maxTitleWidth;
        private final int trimTitleWidth;

        public ReminderListWidget() {
            super(ReminderListScreen.this.client, WIDGET_WIDTH, WIDGET_HEIGHT, ReminderListScreen.this.panelY + ITEM_HEIGHT, ITEM_HEIGHT);
            this.setX(ReminderListScreen.this.wX());

            ReminderTimer.getActive().stream()
                    .sorted(Comparator.comparingInt(ReminderTimer::getRemaining).reversed())    // timers with most time left first
                    .forEach(timer -> addEntry(new ReminderEntry(timer)));

            this.buttonX = (overflows() ? getScrollbarX() : getRowRight()) - BUTTON_WIDTH - Entry.PADDING;
            this.timeX = this.buttonX - ReminderListScreen.this.textRenderer.getWidth("00:00:00") - ROW_ELEMENT_GAP;
            this.maxTitleWidth = this.timeX - getRowLeft() - ROW_ELEMENT_GAP;
            this.trimTitleWidth = maxTitleWidth - ReminderListScreen.this.textRenderer.getWidth("...");
            this.children().forEach(ReminderEntry::init);
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) { /* disable vanilla background */ }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) { /* disable vanilla separators */ }

        @Override
        protected int getScrollbarX() {
            return getRowRight() - SCROLLBAR_WIDTH;
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        protected int getContentsHeightWithPadding() {
            return super.getContentsHeightWithPadding() + EXTRA_BOTTOM_PADDING;    // add bottom margin to list
        }

        /**
         * Reminder list widget entry. Contains timer title, time left & abort button.
         * @see ReminderListWidget
         */
        public class ReminderEntry extends Entry<ReminderEntry>{
            private final ReminderTimer timer;
            private final CustomButtonWidget stopButton;
            private Text title = TEXT_ENTRY_UNTITLED;
            private int relativeTextY;

            /**
             * Entry constructor. Adds elements, sets timer instance.
             * Call {@link #init()} after adding all the entries, to initialize element positions and sizes.
             * @param timer the timer this entry represents
             */
            public ReminderEntry(ReminderTimer timer){
                this.timer = timer;
                this.stopButton = new CustomButtonWidget(0, 0, BUTTON_WIDTH, ITEM_HEIGHT, TEXT_ENTRY_STOP, b -> this.timer.kill());
            }

            /**
             * Initializes positions and sizes of elements. Call after all entries have been added.
             * @see ReminderListWidget#ReminderListWidget()
             */
            public void init(){
                stopButton.setX(ReminderListWidget.this.buttonX);
                relativeTextY = getContentHeight() / 2 - getTextRenderer().fontHeight / 2;
                if(timer.hasName()) title = Text.of(trimTitle(timer.getName()));
            }

            /**
             * Trims title to fit and not overlap, while also adding ellipsis.
             * @param title title to trim
             * @return trimmed title with ellipsis, or original if it fits
             */
            private String trimTitle(String title){
                if(ReminderListScreen.this.textRenderer.getWidth(title) > ReminderListWidget.this.maxTitleWidth)
                    return ReminderListScreen.this.textRenderer.trimToWidth(title, ReminderListWidget.this.trimTitleWidth) + "...";
                return title;
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return Collections.singletonList(stopButton);
            }

            @Override
            public List<? extends Element> children() {
                return Collections.singletonList(stopButton);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                if(overflows() && isInScrollbar(mouseX, mouseY)) return false;
                return ReminderListWidget.this.isMouseOver(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY); // only highlight entry if the mouse is within the widget area
            }

            @Override
            public int getY() {
                return super.getY() + EXTRA_TOP_PADDING;  // add top padding to list
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if(hovered) context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Color.LIST_ENTRY_HOVER_HIGHLIGHT);  // highlight hovered entry
                int textY = getContentY() + relativeTextY;
                drawTextWithShadow(context, title, getContentX(), textY, Color.TEXT_LIGHT);
                stopButton.active = timer.isActive();
                if(stopButton.active)
                    drawTextWithShadow(context, NumUtil.secToHMSString(timer.getRemaining()), ReminderListWidget.this.timeX, textY, timer.isRepeating() ? Color.TEXT_BTN_TIMER_REPEATING : Color.TEXT_BTN_TIMER_NORMAL);
                else
                    drawTextWithShadow(context, "--:--:--", ReminderListWidget.this.timeX, textY, Color.TEXT_BTN_TIMER_ABORTED);
                stopButton.setY(getY());
                stopButton.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}

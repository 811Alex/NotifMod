package eu.gflash.notifmod.client.gui;

import eu.gflash.notifmod.util.Color;
import eu.gflash.notifmod.client.gui.widgets.CustomButtonWidget;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.util.ReminderTimer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Active reminder list GUI.
 * @see ReminderListWidget
 * @author Alex811
 */
public class ReminderListScreen extends BaseScreen {
    private static final Identifier REMINDER_LIST_BACKGROUND = Identifier.parse("notifmod:textures/gui_reminder_list.png");
    private static final Identifier MASK = Identifier.parse("notifmod:textures/gui_reminder_list_mask.png");
    private static final Component TEXT_TITLE = Component.translatable("gui.screen.reminderList.title");
    private static final Component TEXT_ENTRY_UNTITLED = Component.translatable("gui.screen.reminderList.entry.untitled");
    private static final Component TEXT_ENTRY_STOP = Component.translatable("gui.screen.reminderList.entry.stop");
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_HEIGHT = 146;

    protected ReminderListScreen() {
        super(TEXT_TITLE, PANEL_WIDTH, PANEL_HEIGHT, REMINDER_LIST_BACKGROUND);
    }

    @Override
    public void renderForeground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        context.blit(RenderPipelines.GUI_TEXTURED, MASK, panelX, panelY, 0, 0, panelWidth, panelHeight, bgWidth, bgHeight);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ReminderListWidget());
    }

    public static void open(){
        Minecraft.getInstance().setScreen(new ReminderListScreen());
    }

    /**
     * Reminder list widget. Scrollable, entries contain timer title, time left & abort button.
     * @see ReminderEntry
     */
    public class ReminderListWidget extends ContainerObjectSelectionList<ReminderListWidget.ReminderEntry> {
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
            super(ReminderListScreen.this.minecraft, WIDGET_WIDTH, WIDGET_HEIGHT, ReminderListScreen.this.panelY + ITEM_HEIGHT, ITEM_HEIGHT);
            this.setX(ReminderListScreen.this.wX());

            ReminderTimer.getActive().stream()
                    .sorted(Comparator.comparingInt(ReminderTimer::getRemaining).reversed())    // timers with most time left first
                    .forEach(timer -> addEntry(new ReminderEntry(timer)));

            this.buttonX = (scrollbarVisible() ? scrollBarX() : getRowRight()) - BUTTON_WIDTH - Entry.CONTENT_PADDING;
            this.timeX = this.buttonX - ReminderListScreen.this.font.width("00:00:00") - ROW_ELEMENT_GAP;
            this.maxTitleWidth = this.timeX - getRowLeft() - ROW_ELEMENT_GAP;
            this.trimTitleWidth = maxTitleWidth - ReminderListScreen.this.font.width("...");
            this.children().forEach(ReminderEntry::init);
        }

        @Override
        protected void renderListBackground(GuiGraphics context) { /* disable vanilla background */ }

        @Override
        protected void renderListSeparators(GuiGraphics context) { /* disable vanilla separators */ }

        @Override
        protected int scrollBarX() {
            return getRowRight() - SCROLLBAR_WIDTH;
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        protected int contentHeight() {
            return super.contentHeight() + EXTRA_BOTTOM_PADDING;    // add bottom margin to list
        }

        /**
         * Reminder list widget entry. Contains timer title, time left & abort button.
         * @see ReminderListWidget
         */
        public class ReminderEntry extends Entry<ReminderEntry>{
            private final ReminderTimer timer;
            private final CustomButtonWidget stopButton;
            private Component title = TEXT_ENTRY_UNTITLED;
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
                relativeTextY = getContentHeight() / 2 - getFont().lineHeight / 2;
                if(timer.hasName()) title = Component.nullToEmpty(trimTitle(timer.getName()));
            }

            /**
             * Trims title to fit and not overlap, while also adding ellipsis.
             * @param title title to trim
             * @return trimmed title with ellipsis, or original if it fits
             */
            private String trimTitle(String title){
                if(ReminderListScreen.this.font.width(title) > ReminderListWidget.this.maxTitleWidth)
                    return ReminderListScreen.this.font.plainSubstrByWidth(title, ReminderListWidget.this.trimTitleWidth) + "...";
                return title;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return Collections.singletonList(stopButton);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.singletonList(stopButton);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                if(scrollbarVisible() && isOverScrollbar(mouseX, mouseY)) return false;
                return ReminderListWidget.this.isMouseOver(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY); // only highlight entry if the mouse is within the widget area
            }

            @Override
            public int getY() {
                return super.getY() + EXTRA_TOP_PADDING;  // add top padding to list
            }

            @Override
            public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
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

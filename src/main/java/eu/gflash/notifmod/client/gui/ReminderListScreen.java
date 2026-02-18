package eu.gflash.notifmod.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
        private static final int ITEM_HEIGHT = 20;
        private static final int WIDGET_WIDTH = ReminderListScreen.PANEL_WIDTH - (ReminderListScreen.PANEL_PADDING << 1);
        private static final int WIDGET_HEIGHT = ReminderListScreen.PANEL_HEIGHT - (ITEM_HEIGHT << 1);
        private static final int RELATIVE_BOTTOM = ITEM_HEIGHT + WIDGET_HEIGHT;
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

            this.buttonX = this.getRowRight() - (this.overflows() ? 62 : 52);
            this.timeX = this.buttonX - ReminderListScreen.this.textRenderer.getWidth("00:00:00") - 6;
            this.maxTitleWidth = this.timeX - this.getRowLeft() - 6;
            this.trimTitleWidth = maxTitleWidth - ReminderListScreen.this.textRenderer.getWidth("...");
            this.children().forEach(ReminderEntry::init);
        }

        @Override
        protected void drawMenuListBackground(DrawContext context) {}

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) {}

        protected int getScrollbarX() {
            return this.getRowRight() - 6;
        }

        public int getRowWidth() {
            return this.width;
        }

        /**
         * Reminder list widget entry. Contains timer title, time left & abort button.
         * @see ReminderListWidget
         */
        public class ReminderEntry extends Entry<ReminderEntry>{
            private final ReminderTimer timer;
            private final CustomButtonWidget stopButton;
            private Text title = TEXT_ENTRY_UNTITLED;

            /**
             * Entry constructor. Adds elements, sets timer instance.
             * Call {@link #init()} after adding all the entries, to initialize element positions and sizes.
             * @param timer the timer this entry represents
             */
            public ReminderEntry(ReminderTimer timer){
                this.timer = timer;
                this.stopButton = new CustomButtonWidget(0, 0, 50, ITEM_HEIGHT, TEXT_ENTRY_STOP, b -> this.timer.kill());
            }

            /**
             * Initializes positions and sizes of elements. Call after all entries have been added.
             * @see ReminderListWidget#ReminderListWidget()
             */
            public void init(){
                this.stopButton.setX(ReminderListWidget.this.buttonX);
                if(timer.hasName()) this.title = Text.of(trimTitle(timer.getName()));
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
                return ReminderListWidget.this.isMouseOver(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY); // only highlight entry if the mouse is within the widget area
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if(this.isMouseOver(mouseX, mouseY)){   // highlight hovered entry
                    int minX = x - 2;
                    context.fill(minX, y, minX + entryWidth, y + ReminderListWidget.ITEM_HEIGHT, 0x78404040);
                }
                int textY = y + (entryHeight >> 1) - 2;
                drawTextWithShadow(context, title, x, textY, 0xFFFFFF);
                stopButton.active = timer.isActive();
                if(stopButton.active)
                    drawTextWithShadow(context, NumUtil.secToHMSString(timer.getRemaining()), ReminderListWidget.this.timeX, textY, timer.isRepeating() ? 0xFFC4FF : 0xC4FFFF);
                else
                    drawTextWithShadow(context, "--:--:--", ReminderListWidget.this.timeX, textY, 0xFFFFC4);
                stopButton.setY(y);
                stopButton.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}

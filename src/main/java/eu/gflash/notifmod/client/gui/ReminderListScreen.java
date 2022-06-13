package eu.gflash.notifmod.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.gflash.notifmod.util.NumUtil;
import eu.gflash.notifmod.util.ReminderTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Active reminder list GUI.
 * @see ReminderListWidget
 * @author Alex811
 */
public class ReminderListScreen extends BaseScreen {
    private static final Identifier BACKGROUND = new Identifier("notifmod:textures/gui_reminder_list.png");
    private static final Identifier MASK = new Identifier("notifmod:textures/gui_reminder_list_mask.png");
    private static final Text TEXT_TITLE = Text.translatable("gui.screen.reminderList.title");
    private static final Text TEXT_ENTRY_UNTITLED = Text.translatable("gui.screen.reminderList.entry.untitled");
    private static final Text TEXT_ENTRY_STOP = Text.translatable("gui.screen.reminderList.entry.stop");
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_HEIGHT = 146;

    protected ReminderListScreen() {
        super(TEXT_TITLE, PANEL_WIDTH, PANEL_HEIGHT, BACKGROUND);
    }

    @Override
    public void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderForeground(matrices, mouseX, mouseY, delta);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, MASK);
        drawTexture(matrices, panelX, panelY, 0, 0, panelWidth, panelHeight);
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
            super(ReminderListScreen.this.client, WIDGET_WIDTH, WIDGET_HEIGHT, ReminderListScreen.this.panelY + ITEM_HEIGHT, ReminderListScreen.this.panelY + RELATIVE_BOTTOM, ITEM_HEIGHT);
            this.left = ReminderListScreen.this.wX();
            this.right += this.left;
            this.setRenderBackground(false);
            this.setRenderHorizontalShadows(false);

            ReminderTimer.getActive().stream()
                    .sorted(Comparator.comparingInt(ReminderTimer::getRemaining).reversed())    // timers with most time left first
                    .forEach(timer -> addEntry(new ReminderEntry(timer)));

            this.buttonX = this.right - (this.getMaxScroll() > 0 ? 62 : 52);
            this.timeX = this.buttonX - ReminderListScreen.this.textRenderer.getWidth("00:00:00") - 6;
            this.maxTitleWidth = this.timeX - this.left - 6;
            this.trimTitleWidth = maxTitleWidth - ReminderListScreen.this.textRenderer.getWidth("...");
            this.children().forEach(ReminderEntry::init);
        }

        protected int getScrollbarPositionX() {
            return this.right - 6;
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
            private final ButtonWidget stopButton;
            private Text title = TEXT_ENTRY_UNTITLED;

            /**
             * Entry constructor. Adds elements, sets timer instance.
             * Call {@link #init()} after adding all the entries, to initialize element positions and sizes.
             * @param timer the timer this entry represents
             */
            public ReminderEntry(ReminderTimer timer){
                this.timer = timer;
                this.stopButton = new ButtonWidget(0, 0, 50, ITEM_HEIGHT, TEXT_ENTRY_STOP, b -> this.timer.kill()){
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        super.render(matrices, ReminderListWidget.this.isMouseOver(mouseX, mouseY) ? mouseX : -1, mouseY, delta);   // if mouse outside of widget, pretend it's outside the button too
                    }
                };
            }

            /**
             * Initializes positions and sizes of elements. Call after all entries have been added.
             * @see ReminderListWidget#ReminderListWidget()
             */
            public void init(){
                this.stopButton.x = ReminderListWidget.this.buttonX;
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
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if(this.isMouseOver(mouseX, mouseY)){   // highlight hovered entry
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();
                    RenderSystem.enableBlend(); // set up for transparency
                    RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);   // set up for colored quadrilateral
                    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    int minX = x - 2;
                    int maxX = minX + entryWidth;
                    int maxY = y + ReminderListWidget.ITEM_HEIGHT;
                    Arrays.stream(new int[][]{  // vertex coords
                            {minX, maxY},
                            {maxX, maxY},
                            {maxX, y},
                            {minX, y}
                    }).forEach(c -> bufferBuilder.vertex(c[0], c[1], 0D).color(64, 64, 64, 120).next());
                    tessellator.draw();
                    RenderSystem.disableBlend();
                }
                int textY = y + (entryHeight >> 1) - 2;
                textRenderer.drawWithShadow(matrices, title, x, textY, 0xFFFFFF);
                stopButton.active = timer.isActive();
                if(stopButton.active)
                    textRenderer.drawWithShadow(matrices, NumUtil.secToHMSString(timer.getRemaining()), ReminderListWidget.this.timeX, textY, 0xC4FFFF);
                else
                    textRenderer.drawWithShadow(matrices, "--:--:--", ReminderListWidget.this.timeX, textY, 0xFFFFC4);
                stopButton.y = y;
                stopButton.render(matrices, mouseX, mouseY, tickDelta);
            }
        }
    }
}

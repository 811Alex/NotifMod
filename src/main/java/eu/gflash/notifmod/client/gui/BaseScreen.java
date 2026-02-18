package eu.gflash.notifmod.client.gui;

import eu.gflash.notifmod.util.Color;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for the mod's GUIs.
 * @author Alex811
 */
public class BaseScreen extends Screen {
    protected final static int PANEL_BORDER_WIDTH = 3;     // background texture's border thickness, used for wXr() etc
    protected final static int WIDGET_SPACING = 2;        // space to leave between widgets when using wX(int) etc
    protected final static int PANEL_PADDING = PANEL_BORDER_WIDTH + WIDGET_SPACING;
    private final AtomicInteger widgetX = new AtomicInteger(0);
    private final AtomicInteger widgetY = new AtomicInteger(0);
    private int titleX;
    private int titleY;
    protected final Identifier background;      // background texture
    protected final int bgWidth = 256;          // background texture width
    protected final int bgHeight = 256;         // background texture height
    protected int panelWidth;                   // GUI width
    protected int panelHeight;                  // GUI height
    protected int panelX;                       // GUI x coordinate
    protected int panelY;                       // GUI y coordinate

    /**
     * The screen's constructor.
     * @param title the title to set (or null)
     * @param panelWidth the GUI's width (or negative for full screen width)
     * @param panelHeight the GUI's height (or negative for full screen height)
     * @param background the background texture (or null)
     */
    protected BaseScreen(Text title, int panelWidth, int panelHeight, Identifier background){
        super(title);
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.background = background;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta){
        applyBlur(context);
        if(background != null && panelWidth > 0 && panelHeight > 0)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, background, panelX, panelY, 0, 0, panelWidth, panelHeight, bgWidth, bgHeight);
    }

    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta){
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){ // Adds basic background & title drawing.
        renderForeground(context, mouseX, mouseY, delta);
        if(title != null)
            drawText(context, title, titleX, titleY, Color.TEXT_DARK);
    }

    protected void drawText(DrawContext context, Text text, int x, int y, int color, boolean shadow){
        context.drawText(this.textRenderer, text, x, y, color, shadow);
    }

    protected void drawText(DrawContext context, String text, int x, int y, int color, boolean shadow){
        context.drawText(this.textRenderer, text, x, y, color, shadow);
    }

    protected void drawText(DrawContext context, Text text, int x, int y, int color){
        drawText(context, text, x, y, color, false);
    }

    protected void drawText(DrawContext context, String text, int x, int y, int color){
        drawText(context, text, x, y, color, false);
    }

    protected void drawTextWithShadow(DrawContext context, Text text, int x, int y, int color){
        drawText(context, text, x, y, color, true);
    }

    protected void drawTextWithShadow(DrawContext context, String text, int x, int y, int color){
        drawText(context, text, x, y, color, true);
    }

    /**
     * Get current widget x coordinate and reset to initial position.
     * @return current widget x coordinate
     */
    protected int wXr(){
        return widgetX.getAndSet(panelX + PANEL_PADDING);
    }

    /**
     * Get current widget y coordinate and reset to initial position.
     * @return current widget y coordinate
     */
    protected int wYr(){
        return widgetY.getAndSet(panelY + PANEL_PADDING);
    }

    /**
     * Get current widget x coordinate
     * @return current widget x coordinate
     */
    protected int wX(){
        return widgetX.get();
    }

    /**
     * Get current widget y coordinate
     * @return current widget y coordinate
     */
    protected int wY(){
        return widgetY.get();
    }

    /**
     * Get current widget x coordinate and move to the next position.
     * @param delta distance to move, generally same as the current widget's width
     * @return current widget x coordinate
     */
    protected int wX(int delta){
        return widgetX.getAndAdd(delta + WIDGET_SPACING);
    }

    /**
     * Get current widget y coordinate and move to the next position.
     * @param delta distance to move, generally same as the current widget's height
     * @return current widget y coordinate
     */
    protected int wY(int delta){
        return widgetY.getAndAdd(delta + WIDGET_SPACING);
    }

    /**
     * Adds calculation and initialization of component positions.
     * When overriding, you still need to call it, so this class functions properly.
     */
    @Override
    protected void init(){
        super.init();
        if(this.panelWidth < 0) this.panelWidth = this.width;
        if(this.panelHeight < 0) this.panelHeight = this.height;
        this.panelX = width - panelWidth >> 1;
        this.panelY = height - panelHeight >> 1;
        wXr();
        wYr();
        titleX = Math.round(panelX + (panelWidth - (title != null ? textRenderer.getWidth(title) : 0)) / 2F);
        titleY = wY(8);
    }
}

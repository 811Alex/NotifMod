package eu.gflash.notifmod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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
    private float titleX;
    private float titleY;
    protected final Identifier background;      // background texture
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
    protected BaseScreen(Text title, int panelWidth, int panelHeight, Identifier background) {
        super(title);
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.background = background;
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        if(background != null && panelWidth > 0 && panelHeight > 0){
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, background);
            drawTexture(matrices, panelX, panelY, 0, 0, panelWidth, panelHeight);
        }
    }

    public void renderForeground(MatrixStack matrices, int mouseX, int mouseY, float delta){
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) { // Adds basic background & title drawing.
        renderBackground(matrices);
        renderForeground(matrices, mouseX, mouseY, delta);
        if(title != null)
            this.textRenderer.draw(matrices, title, titleX, titleY, 0x404040);
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
    protected void init() {
        super.init();
        if(this.panelWidth < 0) this.panelWidth = this.width;
        if(this.panelHeight < 0) this.panelHeight = this.height;
        this.panelX = width - panelWidth >> 1;
        this.panelY = height - panelHeight >> 1;
        wXr();
        wYr();
        titleX = panelX + (panelWidth - (title != null ? textRenderer.getWidth(title) : 0)) / 2F;
        titleY = wY(8);
    }
}

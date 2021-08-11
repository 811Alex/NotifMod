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
    protected final Identifier background;  // background texture
    protected final int panelWidth;         // GUI width
    protected final int panelHeight;        // GUI height
    protected int panelX;                   // GUI x coordinate
    protected int panelY;                   // GUI y coordinate
    protected int panelBorderWidth = 3;     // background texture's border thickness, used for wXr() etc
    protected int widgetSpacing = 2;        // space to leave between widgets when using wX(int) etc
    private final AtomicInteger widgetX = new AtomicInteger(0);
    private final AtomicInteger widgetY = new AtomicInteger(0);
    private float titleX;
    private float titleY;

    /**
     * The screen's constructor.
     * @param title the title to set
     * @param panelWidth the GUI's width
     * @param panelHeight the GUI's height
     * @param background the background texture
     */
    protected BaseScreen(Text title, int panelWidth, int panelHeight, Identifier background) {
        super(title);
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.background = background;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) { // Adds basic background & title drawing.
        renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, background);
        drawTexture(matrices, panelX, panelY, 0, 0, panelWidth, panelHeight);
        this.textRenderer.draw(matrices, title, titleX, titleY, 4210752);
        super.render(matrices, mouseX, mouseY, delta);
    }

    /**
     * Get current widget x coordinate and reset to initial position.
     * @return current widget x coordinate
     */
    protected int wXr(){
        return widgetX.getAndSet(panelX + panelBorderWidth + widgetSpacing);
    }

    /**
     * Get current widget y coordinate and reset to initial position.
     * @return current widget y coordinate
     */
    protected int wYr(){
        return widgetY.getAndSet(panelY + panelBorderWidth + widgetSpacing);
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
        return widgetX.getAndAdd(delta + widgetSpacing);
    }

    /**
     * Get current widget y coordinate and move to the next position.
     * @param delta distance to move, generally same as the current widget's height
     * @return current widget y coordinate
     */
    protected int wY(int delta){
        return widgetY.getAndAdd(delta + widgetSpacing);
    }

    /**
     * Adds calculation and initialization of component positions.
     * When overriding, you still need to call it, so this class functions properly.
     */
    @Override
    protected void init() {
        super.init();
        this.panelX = (width - panelWidth) / 2;
        this.panelY = (height - panelHeight) / 2;
        wXr();
        wYr();
        titleX = panelX + (panelWidth - textRenderer.getWidth(title)) / 2F;
        titleY = wY(8);
    }
}

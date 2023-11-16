package eu.gflash.notifmod.mixin;

import eu.gflash.notifmod.client.listeners.GameLoadListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to observe the title screen (main menu).
 * @author Alex811
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Unique private static boolean loadedPreFade = false;
    @Unique private static boolean loadedPostFade = false;

    @Shadow private long backgroundFadeStart;

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("RETURN"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        if(loadedPostFade) return;
        float alpha = (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000F - 1F;  // title opacity 0 - 1
        if(!loadedPreFade && alpha <= 0F) {
            loadedPreFade = true;
            GameLoadListener.onTitleScreen(false);
        }else if(alpha >= 1F){
            loadedPostFade = true;
            GameLoadListener.onTitleScreen(true);
        }
    }
}

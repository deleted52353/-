package dev.bedrockfix.mixin;

import dev.bedrockfix.BedrockFixClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Diagnostic: logs which screen swallows clicks, so real fixes can target it.
 * Replace with real fixes once logs show the cause.
 */
@Mixin(Screen.class)
public abstract class DiagnosticMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void bedrockfix$logClick(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        BedrockFixClient.LOG.info("Click on {} at {},{} button={}", this.getClass().getName(), x, y, button);
    }
}

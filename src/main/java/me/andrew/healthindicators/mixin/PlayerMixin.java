package me.andrew.healthindicators.mixin;

import me.andrew.healthindicators.HealthIndicatorsMod;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class PlayerMixin {
	@Inject(at = @At("HEAD"), method = "handleInputEvents")
	private void handleInputs(CallbackInfo info) {
		HealthIndicatorsMod.lh.onTick();
	}
}
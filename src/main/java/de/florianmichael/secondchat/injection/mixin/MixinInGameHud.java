package de.florianmichael.secondchat.injection.mixin;

import de.florianmichael.secondchat.injection.access.IMixinInGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud implements IMixinInGameHud {
  @Final
  @Mutable
  @Unique
  private ChatHud chatHudSecond;

  @Inject(method = "<init>", at = @At(
      value = "FIELD",
      target = "Lnet/minecraft/client/gui/hud/InGameHud;chatHud:Lnet/minecraft/client/gui/hud/ChatHud;",
      shift = At.Shift.AFTER
  ))
  public void injectInit(MinecraftClient client, CallbackInfo ci) {
    this.chatHudSecond = new ChatHud(client);
  }

  @Override
  public ChatHud secondChat$getChatHudSecond() {
    return this.chatHudSecond;
  }
}

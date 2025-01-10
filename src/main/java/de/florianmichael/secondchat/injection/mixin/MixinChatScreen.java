package de.florianmichael.secondchat.injection.mixin;

import de.florianmichael.secondchat.injection.access.IMixinInGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {
  protected MixinChatScreen(Text title) {
    super(title);
  }

  @Inject(method = "render", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V",
      shift = At.Shift.AFTER
  ))
  public void injectChatHudRender(
      DrawContext context,
      int mouseX, int mouseY,
      float delta, CallbackInfo ci
  ) {
    if (this.client == null) return;

    /* if you wanna test :3 ~Luzey
    ((IMixinInGameHud) this.client.inGameHud)
        .secondChat$getChatHudSecond()
        .addMessage(Text.of("meow"));
     */

    var matrices = context.getMatrices();
    matrices.push();
    matrices.translate(200.0F, 0.0F, 0.0F);
    ((IMixinInGameHud) this.client.inGameHud)
        .secondChat$getChatHudSecond()
        .render(context,
            this.client.inGameHud.getTicks(),
            mouseX, mouseY, true);
    matrices.pop();
  }

  @Inject(method = "mouseScrolled", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/hud/ChatHud;scroll(I)V",
      shift = At.Shift.AFTER
  ))
  public void injectChatHudMouseScroll(
      double mouseX, double mouseY,
      double horizontalAmount, double verticalAmount,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (this.client == null) return;
    ((IMixinInGameHud) this.client.inGameHud)
        .secondChat$getChatHudSecond()
        .scroll((int) verticalAmount);
  }

  @Inject(method = "mouseClicked", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/hud/InGameHud;getChatHud()Lnet/minecraft/client/gui/hud/ChatHud;",
      shift = At.Shift.BEFORE
  ), cancellable = true)
  public void injectChatHudMouseClick(
      double mouseX, double mouseY,
      int button, CallbackInfoReturnable<Boolean> cir
  ) {
    if (this.client == null) return;
    if (((IMixinInGameHud) this.client.inGameHud)
        .secondChat$getChatHudSecond()
        .mouseClicked(mouseX, mouseY)) {
      cir.setReturnValue(true);
    }
  }
}

/*
 * This file is part of SecondChat - https://github.com/FlorianMichael/SecondChat
 * Copyright (C) 2025 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.florianmichael.secondchat.injection.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.florianmichael.secondchat.injection.access.IInGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    @Unique
    private boolean secondChat$mainChatFocused;

    protected MixinChatScreen(Text title) {
        super(title);
    }

    @WrapOperation(method = { "keyPressed", "mouseScrolled" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;scroll(I)V"))
    private void scrollSecondChat(ChatHud instance, int scroll, Operation<Void> original) {
        if (secondChat$mainChatFocused) {
            original.call(instance, scroll);
        } else {
            secondChat$getChatHud().scroll(scroll);
        }
    }

    @WrapOperation(method = { "mouseClicked" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;mouseClicked(DD)Z"))
    private boolean clickSecondChat(ChatHud instance, double mouseX, double mouseY, Operation<Boolean> original) {
        if (secondChat$mainChatFocused) {
            return original.call(instance, mouseX, mouseY);
        } else {
            return secondChat$getChatHud().mouseClicked(mouseX, mouseY);
        }
    }
    @WrapOperation(method = { "render" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;getIndicatorAt(DD)Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator indicatorSecondChat(ChatHud instance, double mouseX, double mouseY, Operation<MessageIndicator> original) {
        if (secondChat$mainChatFocused) {
            return original.call(instance, mouseX, mouseY);
        } else {
            return secondChat$getChatHud().getIndicatorAt(mouseX, mouseY);
        }
    }

    @Inject(method = "getTextStyleAt", at = @At("HEAD"), cancellable = true)
    public void textStyleSecondChat(double x, double y, CallbackInfoReturnable<Style> cir) {
        if (!secondChat$mainChatFocused) {
            cir.setReturnValue(secondChat$getChatHud().getTextStyleAt(x, y));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void decideFocusedChat(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        secondChat$mainChatFocused = mouseX <= width / 2;

        final ChatHud secondChat$chatHud = secondChat$getChatHud();
        final int width = MathHelper.ceil((float) secondChat$chatHud.getWidth() / (float) secondChat$chatHud.getChatScale());

        final MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(client.getWindow().getScaledWidth() - width - 10, 0, 0);
        secondChat$chatHud.render(context, client.inGameHud.getTicks(), mouseX, mouseY, true);
        matrices.pop();
    }

    @Unique
    private ChatHud secondChat$getChatHud() {
        final InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
        return ((IInGameHud) inGameHud).secondChat$getChatHud();
    }

}

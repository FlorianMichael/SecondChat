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
import de.florianmichael.secondchat.SecondChat;
import de.florianmichael.secondchat.filter.ConfigScreen;
import de.florianmichael.secondchat.injection.access.IGui;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Matrix3x2fStack;
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

    protected MixinChatScreen(Component title) {
        super(title);
    }

    @WrapOperation(method = { "keyPressed", "mouseScrolled" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void scrollSecondChat(ChatComponent instance, int posInc, Operation<Void> original) {
        if (secondChat$mainChatFocused) {
            original.call(instance, posInc);
        } else {
            secondChat$getChatHud().scrollChat(posInc);
        }
    }

    @WrapOperation(method = { "mouseClicked" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;handleChatQueueClicked(DD)Z"))
    private boolean clickSecondChat(ChatComponent instance, double mouseX, double mouseY, Operation<Boolean> original) {
        if (secondChat$mainChatFocused) {
            return original.call(instance, mouseX, mouseY);
        } else {
            return secondChat$getChatHud().handleChatQueueClicked(secondChat$fixMouseX(mouseX), mouseY);
        }
    }

    @WrapOperation(method = { "render" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;getMessageTagAt(DD)Lnet/minecraft/client/GuiMessageTag;"))
    private GuiMessageTag indicatorSecondChat(ChatComponent instance, double mouseX, double mouseY, Operation<GuiMessageTag> original) {
        if (secondChat$mainChatFocused) {
            return original.call(instance, mouseX, mouseY);
        } else {
            return secondChat$getChatHud().getMessageTagAt(secondChat$fixMouseX(mouseX), mouseY);
        }
    }

    @Inject(method = "getComponentStyleAt", at = @At("HEAD"), cancellable = true)
    public void textStyleSecondChat(double x, double y, CallbackInfoReturnable<Style> cir) {
        if (!secondChat$mainChatFocused) {
            cir.setReturnValue(secondChat$getChatHud().getClickedComponentStyleAt(secondChat$fixMouseX(x), y));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void decideFocusedChat(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        secondChat$mainChatFocused = mouseX <= width / 2;

        final Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        final ChatComponent secondChat = secondChat$getChatHud();
        pose.translate(minecraft.getWindow().getGuiScaledWidth() - SecondChat.instance().getChatWidth(secondChat), 0);
        secondChat.render(guiGraphics, minecraft.gui.getGuiTicks(), mouseX, mouseY, true);
        pose.popMatrix();
    }

    @Unique
    private double secondChat$fixMouseX(final double mouseX) {
        return mouseX - minecraft.getWindow().getGuiScaledWidth() + SecondChat.instance().getChatWidth(secondChat$getChatHud());
    }

    @Unique
    private ChatComponent secondChat$getChatHud() {
        final Gui gui = Minecraft.getInstance().gui;
        return ((IGui) gui).secondChat$getChatComponent();
    }

}

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

import de.florianmichael.secondchat.SecondChat;
import de.florianmichael.secondchat.injection.access.IInGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    public void proxyMessages(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().inGameHud.getChatHud()) {
            final boolean cancel = SecondChat.instance().matches(message.getString());
            if (!cancel) {
                return;
            }

            ci.cancel();
            secondChat$getChatHud().addMessage(message, signatureData, indicator);
        }
    }

    @Inject(method = "clear", at = @At("RETURN"))
    public void clearSecondChat(boolean clearHistory, CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().inGameHud.getChatHud()) {
            secondChat$getChatHud().clear(clearHistory);
        }
    }

    @Inject(method = "reset", at = @At("RETURN"))
    public void resetSecondChat(CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().inGameHud.getChatHud()) {
            secondChat$getChatHud().reset();
        }
    }

    @Inject(method = "resetScroll", at = @At("RETURN"))
    public void resetScrollSecondChat(CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().inGameHud.getChatHud()) {
            secondChat$getChatHud().resetScroll();
        }
    }

    @Unique
    private ChatHud secondChat$getChatHud() {
        final InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
        return ((IInGameHud) inGameHud).secondChat$getChatHud();
    }

}

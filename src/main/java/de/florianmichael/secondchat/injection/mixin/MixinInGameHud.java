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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud implements IInGameHud {

    @Shadow
    @Final
    private ChatHud chatHud;

    @Shadow
    @Final
    private LayeredDrawer layeredDrawer;

    @Unique
    private ChatHud secondChat$chatHud;

    @Unique
    private boolean secondChat$replacingChatHud;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderChat(DrawContext context, RenderTickCounter tickCounter);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftClient client, CallbackInfo ci) {
        secondChat$chatHud = new ChatHud(client);

        layeredDrawer.addLayer(this::secondChat$renderChat);
    }

    @Redirect(method = "renderChat", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;chatHud:Lnet/minecraft/client/gui/hud/ChatHud;"))
    private ChatHud replaceChatHud(InGameHud instance) {
        if (secondChat$replacingChatHud) {
            return secondChat$chatHud;
        } else {
            return chatHud;
        }
    }

    @Unique
    private void secondChat$renderChat(final DrawContext context, final RenderTickCounter tickCounter) {
        secondChat$replacingChatHud = true;

        final MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(client.getWindow().getScaledWidth() - SecondChat.instance().getChatWidth(secondChat$chatHud), 0, 0);
        this.renderChat(context, tickCounter);
        matrices.pop();

        secondChat$replacingChatHud = false;
    }

    @Override
    public ChatHud secondChat$getChatHud() {
        return secondChat$chatHud;
    }

}

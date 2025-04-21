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

import com.mojang.blaze3d.vertex.PoseStack;
import de.florianmichael.secondchat.SecondChat;
import de.florianmichael.secondchat.injection.access.IGui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui implements IGui {

    @Shadow
    @Final
    private LayeredDraw layers;

    @Shadow
    @Final
    private ChatComponent chat;

    @Shadow
    protected abstract void renderChat(final GuiGraphics guiGraphics, final DeltaTracker deltaTracker);

    @Unique
    private ChatComponent secondChat$chatComponent;

    @Unique
    private boolean secondChat$replacingChatHud;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft minecraft, CallbackInfo ci) {
        secondChat$chatComponent = new ChatComponent(minecraft);

        layers.add(this::secondChat$renderChat);
    }

    @Redirect(method = "renderChat", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;chat:Lnet/minecraft/client/gui/components/ChatComponent;"))
    private ChatComponent replaceChatComponent(Gui instance) {
        if (secondChat$replacingChatHud) {
            return secondChat$chatComponent;
        } else {
            return chat;
        }
    }

    @Unique
    private void secondChat$renderChat(final GuiGraphics guiGraphics, final DeltaTracker deltaTracker) {
        secondChat$replacingChatHud = true;

        final PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(guiGraphics.guiWidth() - SecondChat.instance().getChatWidth(secondChat$chatComponent), 0, 0);
        this.renderChat(guiGraphics, deltaTracker);
        pose.popPose();

        secondChat$replacingChatHud = false;
    }

    @Override
    public ChatComponent secondChat$getChatComponent() {
        return secondChat$chatComponent;
    }

}

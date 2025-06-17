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
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @WrapOperation(method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/render/state/GuiRenderState;)V", at = @At(value = "NEW", target = "(I)Lorg/joml/Matrix3x2fStack;"))
    private static Matrix3x2fStack incrementPoseStackSize(int size, Operation<Matrix3x2fStack> original) {
        // Assuming that other mods are also going to increment the size for their own purposes,
        // this should not fail in combination with other mixins.
        return original.call(size + 1);
    }

}

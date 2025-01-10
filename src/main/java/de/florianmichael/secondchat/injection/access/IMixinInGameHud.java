package de.florianmichael.secondchat.injection.access;

import net.minecraft.client.gui.hud.ChatHud;

public interface IMixinInGameHud {
  ChatHud secondChat$getChatHudSecond();
}

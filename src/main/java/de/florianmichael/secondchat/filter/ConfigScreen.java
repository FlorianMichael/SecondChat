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

package de.florianmichael.secondchat.filter;

import de.florianmichael.secondchat.SecondChat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ConfigScreen extends Screen {
    private static final int RED_TRANSPARENT = 0x80FF0000;

    // They sum up to 300
    private static final int TEXT_FIELD_WIDTH = 150;
    private static final int FILTER_BUTTON_WIDTH = 130;
    private static final int ADD_BUTTON_WIDTH = 20;
    private static final int PADDING = 3;

    private final Screen parent;

    private TextFieldWidget textField;
    private ButtonWidget addButton;
    private FilterType filterType = FilterType.EQUALS;

    private FilterRule alreadyAdded;

    public ConfigScreen(final Screen parent) {
        super(Text.translatable("secondchat.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(new SlotList(
                this.client,
                width,
                height,
                PADDING + PADDING + (textRenderer.fontHeight + 2) * PADDING /* title is 2 */,
                30,
                textRenderer.fontHeight + ListEntry.INNER_PADDING * 4
        ));

        final int y = height - ButtonWidget.DEFAULT_HEIGHT - PADDING - 1;
        int x = width / 2 - TEXT_FIELD_WIDTH - PADDING - PADDING;
        textField = addDrawableChild(new TextFieldWidget(this.textRenderer, x, y, TEXT_FIELD_WIDTH, ButtonWidget.DEFAULT_HEIGHT, Text.empty()));

        x += TEXT_FIELD_WIDTH + PADDING;
        addDrawableChild(ButtonWidget
                .builder(getFilterTypeText(filterType), button -> {
                    filterType = FilterType.values()[(filterType.ordinal() + 1) % FilterType.values().length];
                    button.setMessage(getFilterTypeText(filterType));
                })
                .position(x, y)
                .size(FILTER_BUTTON_WIDTH, ButtonWidget.DEFAULT_HEIGHT)
                .build());

        x += FILTER_BUTTON_WIDTH + PADDING;
        addButton = addDrawableChild(ButtonWidget
                .builder(Text.of("+"), button -> {
                    SecondChat.instance().add(new FilterRule(textField.getText(), filterType));
                    client.setScreen(new ConfigScreen(parent));
                })
                .position(x, y)
                .size(ADD_BUTTON_WIDTH, ButtonWidget.DEFAULT_HEIGHT)
                .build());

        addDrawableChild(ButtonWidget
                .builder(Text.of("<-"), button -> client.setScreen(parent))
                .position(PADDING, y)
                .size(ButtonWidget.DEFAULT_HEIGHT, ButtonWidget.DEFAULT_HEIGHT)
                .build());
    }

    private Text getFilterTypeText(final FilterType filterType) {
        return Text.translatable("secondchat.config.filter." + filterType.name().toLowerCase()).formatted(Formatting.GOLD);
    }

    @Override
    public void tick() {
        super.tick();
        if (addButton == null) {
            return;
        }

        addButton.active = !textField.getText().isEmpty();
        if (!addButton.active) {
            return;
        }

        SecondChat.instance().rules().stream()
                .filter(rule -> rule.value().equals(textField.getText()) && rule.type() == filterType)
                .findAny()
                .ifPresentOrElse(filterRule -> this.alreadyAdded = filterRule, () -> this.alreadyAdded = null);
        addButton.active = alreadyAdded == null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        final MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(2.0F, 2.0F, 2.0F);
        context.drawText(client.textRenderer, title, this.width / 4 - client.textRenderer.getWidth(title) / 2, 5, -1, true);
        matrices.pop();
    }

    public class SlotList extends AlwaysSelectedEntryListWidget<ListEntry> {

        public SlotList(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
            super(minecraftClient, width, height - top - bottom, top, entryHeight);

            SecondChat.instance().rules().forEach(rule -> addEntry(new ListEntry(rule)));
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 150;
        }

        @Override
        protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
            // Remove selection box
        }

    }

    public class ListEntry extends AlwaysSelectedEntryListWidget.Entry<ListEntry> {
        public static final int INNER_PADDING = 2;

        private final FilterRule rule;

        public ListEntry(FilterRule rule) {
            this.rule = rule;
        }

        @Override
        public Text getNarration() {
            return getFilterTypeText(rule.type());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            final MatrixStack matrices = context.getMatrices();

            final int color = ConfigScreen.this.alreadyAdded == rule ? RED_TRANSPARENT : Integer.MIN_VALUE;
            matrices.push();
            matrices.translate(x, y, 0);
            context.fill(0, 0, entryWidth - INNER_PADDING * 2, entryHeight, color);
            context.drawTextWithShadow(textRenderer, Text.of(rule.value()), INNER_PADDING, INNER_PADDING, -1);
            context.drawTextWithShadow(textRenderer, getNarration(), entryWidth - textRenderer.getWidth(getNarration()) - INNER_PADDING * 2, INNER_PADDING, Formatting.GOLD.getColorValue());
            matrices.pop();
        }
    }

}

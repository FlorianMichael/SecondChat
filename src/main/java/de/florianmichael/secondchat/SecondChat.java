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

package de.florianmichael.secondchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.florianmichael.secondchat.filter.ConfigScreen;
import de.florianmichael.secondchat.filter.FilterRule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SecondChat implements ClientModInitializer, ModMenuApi {

    private /*final*/ static SecondChat INSTANCE;

    private final Logger logger = LogManager.getLogger("SecondChat");
    private final Path config = FabricLoader.getInstance().getConfigDir().resolve("secondchat.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private List<FilterRule> rules;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        if (Files.exists(config)) {
            try {
                final FilterRule[] rules = gson.fromJson(Files.readString(config), FilterRule[].class);
                this.rules = rules == null ? new ArrayList<>() : Arrays.stream(rules).collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("Failed to read file: {}!", config.toString(), e);
            }
        } else {
            rules = new ArrayList<>(); // Needs to be modifiable
        }
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    public void save() {
        try {
            Files.write(config, gson.toJson(rules).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.error("Failed to create file: {}!", config.toString(), e);
        }
    }

    public void add(final FilterRule rule) {
        rules.add(rule);
        save();
    }

    public void remove(final FilterRule rule) {
        rules.remove(rule);
        save();
    }

    public boolean matches(final String input) {
        return rules.stream().anyMatch(rule -> switch (rule.type()) {
            case EQUALS -> input.equals(rule.value());
            case EQUALS_IGNORE_CASE -> input.equalsIgnoreCase(rule.value());
            case STARTS_WITH -> input.startsWith(rule.value());
            case ENDS_WITH -> input.endsWith(rule.value());
            case CONTAINS -> input.contains(rule.value());
            case REGEX -> input.matches(rule.value());
        });
    }

    public List<FilterRule> rules() {
        return rules;
    }

    public static SecondChat instance() {
        return INSTANCE;
    }

}

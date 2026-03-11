package com.enchantmodal.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PresetManager {
    private static final Path PRESET_FILE = Path.of("config", "enchantmodal_presets.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Map<String, Integer>>>() {}.getType();

    private static LinkedHashMap<String, Map<String, Integer>> presets;

    public static LinkedHashMap<String, Map<String, Integer>> getPresets() {
        if (presets == null) load();
        return presets;
    }

    public static List<String> getPresetNames() {
        return new ArrayList<>(getPresets().keySet());
    }

    public static Map<String, Integer> getPreset(String name) {
        return getPresets().get(name);
    }

    public static void savePreset(String name, Map<Identifier, Integer> enchantments) {
        Map<String, Integer> serialized = new LinkedHashMap<>();
        enchantments.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(e -> serialized.put(e.getKey().toString(), e.getValue()));
        getPresets().put(name, serialized);
        write();
    }

    public static void deletePreset(String name) {
        getPresets().remove(name);
        write();
    }

    private static void load() {
        presets = new LinkedHashMap<>();
        if (Files.exists(PRESET_FILE)) {
            try {
                String json = Files.readString(PRESET_FILE);
                LinkedHashMap<String, Map<String, Integer>> loaded = GSON.fromJson(json, MAP_TYPE);
                if (loaded != null) presets = loaded;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void write() {
        try {
            Files.createDirectories(PRESET_FILE.getParent());
            Files.writeString(PRESET_FILE, GSON.toJson(presets));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

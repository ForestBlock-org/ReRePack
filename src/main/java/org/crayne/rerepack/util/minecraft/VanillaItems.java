package org.crayne.rerepack.util.minecraft;

import org.crayne.rerepack.util.string.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class VanillaItems {

    private VanillaItems() {}

    @NotNull
    private static final Set<String> VANILLA_ITEMS = new HashSet<>();

    public static void loadVanillaItems() {
        try (final InputStream in = VanillaItems.class.getResourceAsStream("/items_vanilla.txt")) {
            assert in != null;
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                reader.lines().forEach(VANILLA_ITEMS::add);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static final String VANILLA_NAMESPACE = "minecraft";

    @NotNull
    public static String withoutMinecraftNamespacedKey(@NotNull final String namespacedKey) {
        return namespacedKey.startsWith(VANILLA_NAMESPACE + ":")
                ? namespacedKey.substring((VANILLA_NAMESPACE + ":").length())
                : namespacedKey;
    }

    @NotNull
    private static final Set<String> ARMOR_ITEMS = Set.of(
            "turtle_helmet", "golden_helmet", "iron_helmet", "diamond_helmet", "chainmail_helmet",
            "netherite_helmet", "leather_helmet", "golden_chestplate", "iron_chestplate", "diamond_chestplate",
            "chainmail_chestplate", "netherite_chestplate", "leather_chestplate", "golden_leggings",
            "iron_leggings", "diamond_leggings", "chainmail_leggings", "netherite_leggings", "leather_leggings",
            "golden_boots", "iron_boots", "diamond_boots", "chainmail_boots", "netherite_boots", "leather_boots"
    );

    public static boolean armor(@NotNull final String namespacedKey) {
        return ARMOR_ITEMS.contains(withoutMinecraftNamespacedKey(namespacedKey));
    }

    public static boolean elytra(@NotNull final String namespacedKey) {
        return withoutMinecraftNamespacedKey(namespacedKey).equals("elytra");
    }

    public static boolean moddedItem(@NotNull final String namespacedKey) {
        return !namespacedKey.startsWith(VANILLA_NAMESPACE + ":") && namespacedKey.contains(":");
    }

    @NotNull
    public static Set<String> allMatching(@NotNull final String pattern) {
        return StringUtil.allMatching(VANILLA_ITEMS, withoutMinecraftNamespacedKey(pattern));
    }

}

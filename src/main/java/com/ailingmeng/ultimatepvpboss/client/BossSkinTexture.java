package com.ailingmeng.ultimatepvpboss.client;

import com.ailingmeng.ultimatepvpboss.UltimatePvpBoss;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BossSkinTexture {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<>();

    private BossSkinTexture() {}

    public static ResourceLocation get(String username) {
        if (username == null || username.isBlank()) {
            username = "Steve";
        }
        String key = username.toLowerCase(Locale.ROOT);
        ResourceLocation cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (LOADING.putIfAbsent(key, Boolean.TRUE) == null) {
            final String name = username;
            Thread t = new Thread(() -> download(key, name), "ultimatepvpboss-skin");
            t.setDaemon(true);
            t.start();
        }
        return new ResourceLocation("textures/entity/steve.png");
    }

    private static void download(String key, String username) {
        ResourceLocation loc = new ResourceLocation(UltimatePvpBoss.MOD_ID, "skins/" + sanitize(key));
        try {
            URI uri;
            if (username.startsWith("http://") || username.startsWith("https://")) {
                uri = URI.create(username);
            } else {
                String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
                uri = URI.create("https://mc-heads.net/skin/" + encoded);
            }
            try (InputStream in = uri.toURL().openStream()) {
                NativeImage image = NativeImage.read(in);
                Minecraft.getInstance().execute(() -> {
                    try {
                        DynamicTexture texture = new DynamicTexture(image);
                        Minecraft.getInstance().getTextureManager().register(loc, texture);
                        CACHE.put(key, loc);
                    } catch (Exception e) {
                        UltimatePvpBoss.LOGGER.warn("Failed to register skin {}", username, e);
                    }
                });
            }
        } catch (Exception e) {
            UltimatePvpBoss.LOGGER.debug("Skin download failed for {}: {}", username, e.toString());
        } finally {
            LOADING.remove(key);
        }
    }

    private static String sanitize(String key) {
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return "url_" + Integer.toHexString(key.hashCode());
        }
        return key.replaceAll("[^a-z0-9_\\-]", "_");
    }
}

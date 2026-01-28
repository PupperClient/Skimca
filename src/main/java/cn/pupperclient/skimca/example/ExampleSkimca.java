package cn.pupperclient.skimca.example;

import cn.pupperclient.skimca.Skimca;
import cn.pupperclient.skimca.SkimcaClient;
import cn.pupperclient.skimca.font.FontHelper;
import net.minecraft.client.MinecraftClient;

import java.awt.*;

public class ExampleSkimca {
    public static boolean example_enabled = true;

    public static void draw() {
        float centerX = MinecraftClient.getInstance().getWindow().getWidth() / 2f;
        float centerY = MinecraftClient.getInstance().getWindow().getHeight() / 2f;

        Skimca.drawText("Skimca Version: " + SkimcaClient.Version,
                centerX,
                centerY,
                Color.WHITE,
                FontHelper.load("Inter-Regular-CJKsc.ttf", 9, "/assets/skimca/fonts/Inter-Regular-CJKsc.ttf")
        );
    }

    public static boolean isExample_enabled() {
        return example_enabled;
    }
}

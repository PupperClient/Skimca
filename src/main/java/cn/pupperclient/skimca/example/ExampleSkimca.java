package cn.pupperclient.skimca.example;

import cn.pupperclient.skimca.Skimca;
import cn.pupperclient.skimca.SkimcaClient;
import cn.pupperclient.skimca.SkimcaLogger;
import cn.pupperclient.skimca.context.SkiaContext;
import cn.pupperclient.skimca.event.EventTarget;
import cn.pupperclient.skimca.event.RenderSkiaEvent;
import cn.pupperclient.skimca.font.FontHelper;
import net.minecraft.client.MinecraftClient;

import java.awt.*;

public class ExampleSkimca {
    @EventTarget(priority = EventTarget.Priority.HIGH)
    public void onInGameRender(RenderSkiaEvent event) {
        // SkimcaLogger.info("ExampleSkimca", "render");
        MinecraftClient client = MinecraftClient.getInstance();
        String versionText = "Skimca v" + SkimcaClient.Version;

        var font = FontHelper.load(
                "Inter-Regular-CJKsc.ttf",
                8,
                "/assets/skimca/fonts/Inter-Regular-CJKsc.ttf"
        );
        var textBounds = Skimca.getTextBounds(versionText, font);
        float x = client.getWindow().getWidth() / 2f;
        float y = client.getWindow().getHeight() / 2f;

        SkiaContext.draw((skiaContext) -> {

            Skimca.save();

            Skimca.drawText(
                    versionText,
                    x,
                    y,
                    new Color(255, 255, 255, 180),
                    font
            );

            Skimca.restore();
        });
        // SkimcaLogger.info("ExampleSkimca", "render/restore");
    }
}

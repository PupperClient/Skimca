package cn.pupperclient.skimca.mixins;

import cn.pupperclient.skimca.event.EventTarget;
import cn.pupperclient.skimca.event.RenderSkiaEvent;
import cn.pupperclient.skimca.example.ExampleSkimca;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique
    @EventTarget
    public void onSkimcaRender(RenderSkiaEvent e) {
        ExampleSkimca.draw();
    }
}

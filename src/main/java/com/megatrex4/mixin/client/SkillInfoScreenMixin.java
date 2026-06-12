package com.megatrex4.mixin.client;

import com.megatrex4.client.LevelZClientSkillVisibility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.levelz.level.Skill;
import net.levelz.screen.LevelScreen;
import net.levelz.screen.SkillInfoScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = SkillInfoScreen.class, remap = false)
public abstract class SkillInfoScreenMixin extends Screen {

    @Shadow
    @Final
    private Skill skill;

    protected SkillInfoScreenMixin() {
        super(null);
    }

    @Inject(
            method = "init",
            at = @At("HEAD"),
            remap = false
    )
    private void levelzinventoryweight$closeHiddenSkillInfo(CallbackInfo ci) {
        if (!LevelZClientSkillVisibility.shouldHideConfiguredSkill()) {
            return;
        }

        String hiddenSkillKey = LevelZClientSkillVisibility.getConfiguredSkillKey();

        if (!hiddenSkillKey.equals(this.skill.getKey())) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null) {
            client.setScreen(new LevelScreen());
        }
    }
}
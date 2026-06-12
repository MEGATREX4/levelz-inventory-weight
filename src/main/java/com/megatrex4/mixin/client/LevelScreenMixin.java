package com.megatrex4.mixin.client;

import com.megatrex4.client.LevelZClientSkillVisibility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.levelz.level.LevelManager;
import net.levelz.level.PlayerSkill;
import net.levelz.level.Skill;
import net.levelz.screen.LevelScreen;
import net.minecraft.client.gui.DrawContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(value = LevelScreen.class, remap = false)
public abstract class LevelScreenMixin {

    @Shadow
    private int skillRow;

    @Shadow
    private LevelManager levelManager;

    @Inject(
            method = "renderBackground",
            at = @At("HEAD"),
            remap = false
    )
    private void levelzinventoryweight$clampSkillRow(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        if (this.levelManager == null) {
            return;
        }

        int visibleSkillCount = LevelZClientSkillVisibility.getVisiblePlayerSkillCount(
                this.levelManager.getPlayerSkills()
        );

        if (visibleSkillCount <= 12) {
            this.skillRow = 0;
            return;
        }

        int maxSkillRow = (visibleSkillCount - 12) / 2;

        if (visibleSkillCount % 2 != 0) {
            maxSkillRow++;
        }

        if (this.skillRow > maxSkillRow) {
            this.skillRow = maxSkillRow;
        }

        if (this.skillRow < 0) {
            this.skillRow = 0;
        }
    }

    @Redirect(
            method = {
                    "init",
                    "renderBackground",
                    "mouseClicked",
                    "mouseScrolled",
                    "updateLevelButtons"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/levelz/level/LevelManager;SKILLS:Ljava/util/Map;",
                    opcode = Opcodes.GETSTATIC
            ),
            remap = false
    )
    private Map<Integer, Skill> levelzinventoryweight$hideSkillFromLevelManagerSkills() {
        return LevelZClientSkillVisibility.getVisibleSkills(LevelManager.SKILLS);
    }

    @Redirect(
            method = {
                    "init",
                    "renderBackground",
                    "mouseClicked",
                    "mouseScrolled",
                    "updateLevelButtons"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/levelz/level/LevelManager;getPlayerSkills()Ljava/util/Map;"
            ),
            remap = false
    )
    private Map<Integer, PlayerSkill> levelzinventoryweight$hideSkillFromPlayerSkills(
            LevelManager levelManager
    ) {
        return LevelZClientSkillVisibility.getVisiblePlayerSkills(levelManager.getPlayerSkills());
    }
}
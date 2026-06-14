package com.megatrex4.mixin.client;

import com.megatrex4.config.LevelZInventoryWeightConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.levelz.screen.SkillInfoScreen", remap = false)
public abstract class SkillInfoScreenMixin {

    private static final int[][] LEVELZ_INVENTORYWEIGHT_SLOT_GROUPS = {
            {4, 5},
            {2, 3},
            {0, 1},
    };

    @Shadow
    @Final
    private String title;

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/levelz/screen/widget/SkillScrollableWidget;<init>"
                            + "(IIIILjava/util/List;Ljava/lang/String;Lnet/minecraft/client/font/TextRenderer;)V"
            ),
            index = 4,
            remap = false
    )
    private List<Text> levelzinventoryweight$addCarryingInfo(List<Text> textList) {
        if (!levelzinventoryweight$shouldInject()) {
            return textList;
        }

        while (textList.size() < 8) {
            textList.add(null);
        }

        Text line1 = Text.translatable("text.levelz_inventoryweight.skill_info_carrying_1");
        Text line2 = Text.translatable("text.levelz_inventoryweight.skill_info_carrying_2");

        int[] target = levelzinventoryweight$findFreeSlots(textList);

        if (target.length == 2) {
            textList.set(target[0], line1);
            textList.set(target[1], line2);
        } else if (target.length == 1) {
            textList.set(target[0], line1.copy().append(" ").append(line2));
        }

        return textList;
    }

    private boolean levelzinventoryweight$shouldInject() {
        LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

        if (!config.enabled || !config.skill.enabled) {
            return false;
        }

        String configuredKey = config.skill.skillKey == null ? "" : config.skill.skillKey.trim();

        return !configuredKey.isEmpty()
                && configuredKey.equalsIgnoreCase(this.title);
    }

    private static int[] levelzinventoryweight$findFreeSlots(List<Text> list) {
        for (int[] group : LEVELZ_INVENTORYWEIGHT_SLOT_GROUPS) {
            if (group.length == 2
                    && levelzinventoryweight$isEmpty(list, group[0])
                    && levelzinventoryweight$isEmpty(list, group[1])) {
                return group;
            }
        }

        for (int slot : new int[]{0, 1, 2, 3, 4, 5}) {
            if (levelzinventoryweight$isEmpty(list, slot)) {
                return new int[]{slot};
            }
        }

        return new int[0];
    }

    private static boolean levelzinventoryweight$isEmpty(List<Text> list, int index) {
        if (index < 0 || index >= list.size()) {
            return false;
        }
        Text text = list.get(index);
        return text == null || text.getString().isBlank();
    }
}

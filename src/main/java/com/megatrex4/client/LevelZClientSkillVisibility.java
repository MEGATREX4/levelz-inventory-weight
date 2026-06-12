package com.megatrex4.client;

import com.megatrex4.LevelZInventoryWeight;
import com.megatrex4.config.LevelZInventoryWeightConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.levelz.level.LevelManager;
import net.levelz.level.PlayerSkill;
import net.levelz.level.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

@Environment(EnvType.CLIENT)
public final class LevelZClientSkillVisibility {

    private static boolean warnedNonTailSkill = false;

    private LevelZClientSkillVisibility() {
    }

    public static boolean shouldHideConfiguredSkill() {
        LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

        if (!config.enabled) {
            return true;
        }

        if (!config.skill.enabled) {
            return true;
        }

        return !config.skill.showInLevelZScreen;
    }

    public static String getConfiguredSkillKey() {
        LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

        String key = config.skill.skillKey;

        if (key == null) {
            return "";
        }

        return key.trim();
    }

    public static OptionalInt getConfiguredSkillId(Map<Integer, Skill> skills) {
        String key = getConfiguredSkillKey();

        if (key.isBlank()) {
            return OptionalInt.empty();
        }

        LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

        if (config.skill.skillIdOverride >= 0) {
            return OptionalInt.of(config.skill.skillIdOverride);
        }

        for (Skill skill : skills.values()) {
            if (key.equals(skill.getKey())) {
                return OptionalInt.of(skill.getId());
            }
        }

        return OptionalInt.empty();
    }

    public static Map<Integer, Skill> getVisibleSkills(Map<Integer, Skill> original) {
        if (!shouldHideConfiguredSkill()) {
            return original;
        }

        OptionalInt hiddenSkillId = getConfiguredSkillId(original);

        if (hiddenSkillId.isEmpty()) {
            return original;
        }

        int skillId = hiddenSkillId.getAsInt();

        if (!canSafelyHideSkill(original, skillId)) {
            return original;
        }

        Map<Integer, Skill> filtered = new HashMap<>(original);
        filtered.remove(skillId);

        return filtered;
    }

    public static Map<Integer, PlayerSkill> getVisiblePlayerSkills(Map<Integer, PlayerSkill> original) {
        if (!shouldHideConfiguredSkill()) {
            return original;
        }

        OptionalInt hiddenSkillId = getConfiguredSkillId(LevelManager.SKILLS);

        if (hiddenSkillId.isEmpty()) {
            return original;
        }

        int skillId = hiddenSkillId.getAsInt();

        if (!canSafelyHideSkill(LevelManager.SKILLS, skillId)) {
            return original;
        }

        Map<Integer, PlayerSkill> filtered = new HashMap<>(original);
        filtered.remove(skillId);

        return filtered;
    }

    public static int getVisiblePlayerSkillCount(Map<Integer, PlayerSkill> original) {
        return getVisiblePlayerSkills(original).size();
    }

    private static boolean canSafelyHideSkill(Map<Integer, Skill> skills, int skillId) {
        /*
         * LevelZ's LevelScreen assumes skill ids are contiguous:
         *
         * 0, 1, 2, 3, ...
         *
         * Your carrying skill is id 12, after default LevelZ ids 0-11.
         * Hiding the final id is safe.
         *
         * Hiding a middle id, for example id 5, would leave a hole and may break
         * LevelZ's screen because it does LevelManager.SKILLS.get(skillId).
         */
        int expectedLastId = skills.size() - 1;

        if (skillId >= expectedLastId) {
            return true;
        }

        if (!warnedNonTailSkill) {
            warnedNonTailSkill = true;

            Skill skill = skills.get(skillId);

            LevelZInventoryWeight.LOGGER.warn(
                    "Cannot safely hide LevelZ skill '{}' with id {} because it is not the last skill id. " +
                            "LevelZ's screen assumes contiguous skill ids. Put the hidden skill at the end, e.g. id 12 after default LevelZ skills.",
                    skill == null ? getConfiguredSkillKey() : skill.getKey(),
                    skillId
            );
        }

        return false;
    }
}
package com.megatrex4.config;

import com.megatrex4.LevelZInventoryWeight;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import net.minecraft.util.Identifier;

public class LevelZInventoryWeightConfig extends Config {

    public boolean enabled = true;

    /**
     * Safety floor. MT Inventory Weight already clamps this too,
     * but keeping it here makes the formula explicit.
     */
    public float minimumMaxWeight = 1.0f;

    public OverallLevelFormula overallLevel = new OverallLevelFormula();

    public SkillFormula skill = new SkillFormula();

    public LevelZInventoryWeightConfig() {
        super(Identifier.of(LevelZInventoryWeight.MOD_ID, "main"));
    }

    public static class OverallLevelFormula extends ConfigSection {

        /**
         * If true, the player's overall LevelZ character level affects max weight.
         */
        public boolean enabled = true;

        /**
         * Flat max-weight bonus per overall LevelZ level.
         *
         * Example:
         * additivePerLevel = 0.5
         * overall level = 20
         * bonus = +10 max weight
         */
        public float additivePerLevel = 0.5f;

        /**
         * Percentage-style multiplier per overall LevelZ level.
         *
         * Example:
         * multiplierPerLevel = 0.01
         * overall level = 20
         * multiplier = 1.20
         */
        public float multiplierPerLevel = 0.0f;
    }

    public static class SkillFormula extends ConfigSection {

        /**
         * If true, one configured LevelZ skill affects max weight.
         */
        public boolean enabled = true;

        /**
         * LevelZ skill key, not translation key.
         *
         * Vanilla/default LevelZ examples:
         * constitution, melee, defense, archery, agility,
         * magic, mining, smithing, farming, cooking, bartering, luck
         *
         * If you later add our own skill, this can become:
         * carrying
         */
        public String skillKey = "carrying";

        /**
         * Optional override.
         *
         * -1 means resolve by skillKey.
         * Use this only if you really need to target a numeric LevelZ skill ID.
         */
        public int skillIdOverride = -1;

        /**
         * Flat max-weight bonus per level of the configured skill.
         */
        public float additivePerLevel = 1.5f;

        /**
         * Percentage-style multiplier per level of the configured skill.
         *
         * 0.01 = +1% per skill level.
         */
        public float multiplierPerLevel = 0.0f;
    }
}
package com.megatrex4.config;

import com.megatrex4.LevelZInventoryWeight;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import net.minecraft.util.Identifier;

public final class LevelZInventoryWeightConfig {

    public static final Identifier SERVER_CONFIG_ID =
            new Identifier(LevelZInventoryWeight.MOD_ID, "server-config");

    private static Server SERVER_INSTANCE;

    private LevelZInventoryWeightConfig() {
    }

    public static Server getServer() {
        if (SERVER_INSTANCE == null) {
            SERVER_INSTANCE = ConfigApiJava.registerAndLoadConfig(Server::new, RegisterType.BOTH);
        }

        return SERVER_INSTANCE;
    }

    @Version(version = 3)
    public static class Server extends Config {

        public Server() {
            super(SERVER_CONFIG_ID);
        }

        @Comment("Master switch for the LevelZ Inventory Weight integration.")
        public boolean enabled = true;

        @Comment("Minimum max weight after all LevelZ formulas are applied.")
        @ValidatedFloat.Restrict(min = 1.0f, max = Float.MAX_VALUE)
        public float minimumMaxWeight = 1.0f;

        @Comment("Formula using the player's total LevelZ character level.")
        public OverallLevelFormula overallLevel = new OverallLevelFormula();

        @Comment("Formula using one configured built-in LevelZ skill.")
        public SkillFormula skill = new SkillFormula();
    }

    public static class OverallLevelFormula extends ConfigSection {

        @Comment("If true, the player's overall LevelZ level affects max inventory weight.")
        public boolean enabled = true;

        @Comment("""
                Flat max-weight bonus per total LevelZ character level.

                Example:
                additivePerLevel = 5.0
                overall level = 10
                bonus = +50 max weight
                """)
        @ValidatedFloat.Restrict(min = 0.0f, max = Float.MAX_VALUE)
        public float additivePerLevel = 0.0f;

        @Comment("""
                Multiplier bonus per total LevelZ character level.

                0.01 = +1% per level
                0.02 = +2% per level

                Example:
                multiplierPerLevel = 0.01
                overall level = 20
                multiplier = 1.20
                """)
        @ValidatedFloat.Restrict(min = 0.0f, max = Float.MAX_VALUE)
        public float multiplierPerLevel = 0.01f;
    }

    public static class SkillFormula extends ConfigSection {

        @Comment("If true, the configured LevelZ skill affects max inventory weight.")
        public boolean enabled = true;

        @Comment("""
                Built-in LevelZ skill key (case-insensitive).

                1.20.1 LevelZ does not support custom skills, so this must be one of:
                health, strength, agility, defense, stamina, luck,
                archery, trade, smithing, mining, farming, alchemy
                """)
        public String skillKey = "strength";

        @Comment("""
                Flat max-weight bonus per configured skill level.

                Example:
                additivePerLevel = 10.0
                strength level = 10
                bonus = +100 max weight
                """)
        @ValidatedFloat.Restrict(min = 0.0f, max = Float.MAX_VALUE)
        public float additivePerLevel = 0.0f;

        @Comment("""
                Multiplier bonus per configured skill level.

                0.05 = +5% per skill level

                Example:
                multiplierPerLevel = 0.05
                strength level = 20
                multiplier = 2.0
                """)
        @ValidatedFloat.Restrict(min = 0.0f, max = Float.MAX_VALUE)
        public float multiplierPerLevel = 0.05f;
    }
}

package com.megatrex4.config;

import com.megatrex4.LevelZInventoryWeight;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

public final class LevelZInventoryWeightConfig {

    public static final Identifier SERVER_CONFIG_ID =
            Identifier.of(LevelZInventoryWeight.MOD_ID, "server-config");

    private static Server SERVER_INSTANCE;

    private LevelZInventoryWeightConfig() {
    }

    public static Server getServer() {
        if (SERVER_INSTANCE == null) {
            SERVER_INSTANCE = ConfigApiJava.registerAndLoadConfig(Server::new, RegisterType.BOTH);
        }

        return SERVER_INSTANCE;
    }

    public enum SkillCapacityMode {
        /**
         * The add-on reads the LevelZ skill level and modifies MT Inventory Weight
         * through InventoryWeightEvents.MODIFY_MAX_WEIGHT.
         *
         * Best option for multiplier-based logic.
         */
        EVENT_MODIFIER,

        /**
         * The LevelZ skill uses inventoryweight:generic.max_weight directly
         * as a LevelZ attribute.
         *
         * Best option for flat ADD_VALUE bonuses.
         */
        LEVELZ_ATTRIBUTE
    }

    @Version(version = 2)
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

        @Comment("Formula using one configured LevelZ skill.")
        public SkillFormula skill = new SkillFormula();

        @Comment("Optional dynamic LevelZ skill creation / attribute configuration.")
        public DynamicSkill dynamicSkill = new DynamicSkill();
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
                If false, the configured skill is hidden from the LevelZ skill screen.

                Also, when skill.enabled is false, the skill should be hidden automatically
                by the client screen mixin.
                """)
        public boolean showInLevelZScreen = true;

        @Comment("""
                How the configured LevelZ skill should affect max inventory weight.

                EVENT_MODIFIER:
                The add-on reads the LevelZ skill level and modifies max weight through
                InventoryWeightEvents.MODIFY_MAX_WEIGHT. This supports additive and multiplier logic.

                LEVELZ_ATTRIBUTE:
                The LevelZ skill uses inventoryweight:generic.max_weight as a LevelZ attribute.
                This is mostly useful for flat ADD_VALUE bonuses.
                """)
        public SkillCapacityMode capacityMode = SkillCapacityMode.EVENT_MODIFIER;

        @Comment("""
                LevelZ skill key.

                For the custom skill from this add-on, use:
                carrying

                Default LevelZ examples:
                constitution, melee, defense, archery, agility,
                magic, mining, smithing, farming, cooking, bartering, luck
                """)
        public String skillKey = "carrying";

        @Comment("""
                Optional numeric skill id override.

                -1 = resolve the skill by skillKey.

                Only use this if you specifically need to target a numeric LevelZ skill id.
                """)
        @ValidatedInt.Restrict(min = -1, max = Integer.MAX_VALUE)
        public int skillIdOverride = -1;

        @Comment("""
                Flat max-weight bonus per configured skill level.

                Used when capacityMode = EVENT_MODIFIER.

                Example:
                additivePerLevel = 10.0
                carrying level = 10
                bonus = +100 max weight
                """)
        @ValidatedFloat.Restrict(min = 0.0f, max = Float.MAX_VALUE)
        public float additivePerLevel = 0.0f;

        @Comment("""
                Multiplier bonus per configured skill level.

                Used when capacityMode = EVENT_MODIFIER.

                0.05 = +5% per skill level

                Example:
                multiplierPerLevel = 0.05
                carrying level = 20
                multiplier = 2.0
                """)
        @ValidatedFloat.Restrict(min = 0.0f, max = Float.MAX_VALUE)
        public float multiplierPerLevel = 0.05f;
    }

    public static class DynamicSkill extends ConfigSection {

        @Comment("""
                If true, this add-on dynamically creates the configured LevelZ skill
                when LevelZ does not already have a skill with skill.skillKey.

                If you keep data/levelz/skill/inventory_weight.json,
                then this normally does nothing because the skill already exists.
                """)
        public boolean createIfMissing = true;

        @Comment("""
                Skill id for the dynamically created skill.

                Default LevelZ skills normally use ids 0 through 11,
                so 12 is the normal next id.

                Set to -1 to automatically use the next available id.
                """)
        @ValidatedInt.Restrict(min = -1, max = Integer.MAX_VALUE)
        public int skillId = 12;

        @Comment("Max level for the dynamically created skill.")
        @ValidatedInt.Restrict(min = 1, max = Integer.MAX_VALUE)
        public int maxLevel = 20;

        @Comment("""
                Attribute display id for LevelZ's attribute side panel.

                -1 means do not display this attribute in the LevelZ attribute panel.
                """)
        @ValidatedInt.Restrict(min = -1, max = Integer.MAX_VALUE)
        public int attributeDisplayId = -1;

        @Comment("""
                Base value used when capacityMode = LEVELZ_ATTRIBUTE.

                -10000.0 means LevelZ should keep the attribute's normal base value.
                """)
        public float attributeBaseValue = -10000.0f;

        @Comment("""
                Attribute value per skill level when capacityMode = LEVELZ_ATTRIBUTE.

                This is applied to:
                inventoryweight:generic.max_weight

                With ADD_VALUE:
                10.0 means +10 max weight per skill level.
                """)
        public float attributeValuePerLevel = 10.0f;

        @Comment("""
                Attribute operation when capacityMode = LEVELZ_ATTRIBUTE.

                Recommended:
                ADD_VALUE

                ADD_MULTIPLIED_BASE and ADD_MULTIPLIED_TOTAL are usually not useful
                for inventoryweight:generic.max_weight because the attribute base is normally 0.
                """)
        public EntityAttributeModifier.Operation attributeOperation =
                EntityAttributeModifier.Operation.ADD_VALUE;
    }
}
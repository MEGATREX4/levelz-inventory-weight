package com.megatrex4;

import com.megatrex4.api.v1.InventoryWeightEvents;
import com.megatrex4.config.LevelZInventoryWeightConfig;
import com.megatrex4.config.ModConfigs;
import com.megatrex4.levelz.DynamicLevelZSkillManager;
import com.megatrex4.levelz.LevelZSkillAccess;
import net.fabricmc.api.ModInitializer;
import net.levelz.access.LevelManagerAccess;
import net.levelz.level.LevelManager;
import net.levelz.level.PlayerSkill;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

public class LevelZInventoryWeight implements ModInitializer {

	public static final String MOD_ID = "levelz_inventoryweight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfigs.init();

		DynamicLevelZSkillManager.register();

		InventoryWeightEvents.MODIFY_MAX_WEIGHT.register(LevelZInventoryWeight::modifyMaxWeight);

		LOGGER.info("LevelZ Inventory Weight initialized");
	}

	private static float modifyMaxWeight(ServerPlayerEntity player, float currentMaxWeight) {
		LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

		if (!config.enabled) {
			return currentMaxWeight;
		}

		LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();

		float additive = 0.0f;
		float multiplier = 1.0f;

		if (config.overallLevel.enabled) {
			int overallLevel = Math.max(0, levelManager.getOverallLevel());

			additive += overallLevel * config.overallLevel.additivePerLevel;

			multiplier *= Math.max(
					0.0f,
					1.0f + overallLevel * config.overallLevel.multiplierPerLevel
			);
		}

		if (
				config.skill.enabled
						&& config.skill.capacityMode == LevelZInventoryWeightConfig.SkillCapacityMode.EVENT_MODIFIER
		) {
			int skillLevel = Math.max(0, getConfiguredSkillLevel(levelManager, config));

			additive += skillLevel * config.skill.additivePerLevel;

			multiplier *= Math.max(
					0.0f,
					1.0f + skillLevel * config.skill.multiplierPerLevel
			);
		}

		float result = (currentMaxWeight + additive) * multiplier;

		return Math.max(config.minimumMaxWeight, result);
	}

	private static int getConfiguredSkillLevel(
			LevelManager levelManager,
			LevelZInventoryWeightConfig.Server config
	) {
		int skillId = config.skill.skillIdOverride;

		if (skillId < 0) {
			OptionalInt resolvedSkillId = LevelZSkillAccess.getSkillIdByKey(config.skill.skillKey);

			if (resolvedSkillId.isEmpty()) {
				return 0;
			}

			skillId = resolvedSkillId.getAsInt();
		}

		PlayerSkill playerSkill = levelManager.getPlayerSkills().get(skillId);

		return playerSkill == null ? 0 : playerSkill.getLevel();
	}
}
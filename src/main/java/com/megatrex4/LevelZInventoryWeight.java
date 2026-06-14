package com.megatrex4;

import com.megatrex4.api.v1.InventoryWeightEvents;
import com.megatrex4.config.LevelZInventoryWeightConfig;
import com.megatrex4.config.ModConfigs;
import com.megatrex4.levelz.LevelZSkillAccess;
import net.fabricmc.api.ModInitializer;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LevelZInventoryWeight implements ModInitializer {

	public static final String MOD_ID = "levelz_inventoryweight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfigs.init();

		InventoryWeightEvents.MODIFY_MAX_WEIGHT.register(LevelZInventoryWeight::modifyMaxWeight);

		LOGGER.info("LevelZ Inventory Weight initialized");
	}

	private static float modifyMaxWeight(ServerPlayerEntity player, float currentMaxWeight) {
		LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

		if (!config.enabled) {
			return currentMaxWeight;
		}

		PlayerStatsManager statsManager =
				((PlayerStatsManagerAccess) player).getPlayerStatsManager();

		float additive = 0.0f;
		float multiplier = 1.0f;

		if (config.overallLevel.enabled) {
			int overallLevel = Math.max(0, statsManager.getOverallLevel());

			additive += overallLevel * config.overallLevel.additivePerLevel;

			multiplier *= Math.max(
					0.0f,
					1.0f + overallLevel * config.overallLevel.multiplierPerLevel
			);
		}

		if (config.skill.enabled) {
			int skillLevel = Math.max(0, getConfiguredSkillLevel(statsManager, config));

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
			PlayerStatsManager statsManager,
			LevelZInventoryWeightConfig.Server config
	) {
		Optional<Skill> skill = LevelZSkillAccess.getSkillByKey(config.skill.skillKey);

		if (skill.isEmpty()) {
			return 0;
		}

		return statsManager.getSkillLevel(skill.get());
	}
}

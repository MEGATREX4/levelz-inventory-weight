package com.megatrex4.levelz;

import com.megatrex4.LevelZInventoryWeight;
import com.megatrex4.api.v1.InventoryWeightApi;
import com.megatrex4.config.LevelZInventoryWeightConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.levelz.access.LevelManagerAccess;
import net.levelz.level.LevelManager;
import net.levelz.level.PlayerSkill;
import net.levelz.level.Skill;
import net.levelz.level.SkillAttribute;
import net.levelz.util.LevelHelper;
import net.levelz.util.PacketHelper;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public final class DynamicLevelZSkillManager {

    private DynamicLevelZSkillManager() {
    }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new DynamicLevelZSkillReloader());

        ServerLifecycleEvents.SERVER_STARTED.register(DynamicLevelZSkillManager::syncPlayers);

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                syncPlayers(server);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ensurePlayerHasRegisteredSkills(handler.getPlayer())
        );
    }

    public static void applyConfiguredDynamicSkill() {
        LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

        if (!config.enabled) {
            return;
        }

        if (!config.skill.enabled) {
            return;
        }

        if (!config.dynamicSkill.createIfMissing) {
            return;
        }

        String skillKey = normalizeSkillKey(config.skill.skillKey);

        if (skillKey == null) {
            LevelZInventoryWeight.LOGGER.warn(
                    "Could not create dynamic LevelZ skill because skillKey is empty"
            );
            return;
        }

        if (LevelZSkillAccess.hasSkillKey(skillKey)) {
            LevelZInventoryWeight.LOGGER.info(
                    "LevelZ skill '{}' already exists; dynamic skill creation skipped",
                    skillKey
            );
            return;
        }

        int skillId = config.dynamicSkill.skillId;

        if (skillId < 0) {
            skillId = LevelZSkillAccess.getNextAvailableSkillId();
        } else if (LevelManager.SKILLS.containsKey(skillId)) {
            LevelZInventoryWeight.LOGGER.warn(
                    "Could not create dynamic LevelZ skill '{}' with id {} because that id is already used by '{}'. " +
                            "Set dynamicSkill.skillId to -1 to choose the next available id automatically.",
                    skillKey,
                    skillId,
                    LevelManager.SKILLS.get(skillId).getKey()
            );
            return;
        }

        List<SkillAttribute> attributes = createDynamicSkillAttributes(config);

        Skill skill = new Skill(
                skillId,
                skillKey,
                config.dynamicSkill.maxLevel,
                attributes
        );

        LevelManager.SKILLS.put(skillId, skill);

        TreeMap<Integer, Skill> sortedSkills = new TreeMap<>(LevelManager.SKILLS);
        LevelManager.SKILLS.clear();
        LevelManager.SKILLS.putAll(sortedSkills);

        LevelZInventoryWeight.LOGGER.info(
                "Created dynamic LevelZ skill '{}' with id {} and mode {}",
                skillKey,
                skillId,
                config.skill.capacityMode
        );
    }

    public static void syncPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ensurePlayerHasRegisteredSkills(player);
        }
    }

    public static void ensurePlayerHasRegisteredSkills(ServerPlayerEntity player) {
        LevelZInventoryWeightConfig.Server config = LevelZInventoryWeightConfig.getServer();

        LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();

        Set<Integer> registeredSkillIds = new HashSet<>(LevelManager.SKILLS.keySet());

        levelManager.getPlayerSkills()
                .keySet()
                .removeIf(skillId -> !registeredSkillIds.contains(skillId));

        for (Skill skill : LevelZSkillAccess.getRegisteredSkills()) {
            PlayerSkill playerSkill = levelManager.getPlayerSkills()
                    .computeIfAbsent(skill.getId(), id -> new PlayerSkill(id, 0));

            if (playerSkill.getLevel() > skill.getMaxLevel()) {
                playerSkill.setLevel(skill.getMaxLevel());
            }
        }

        handleInventoryWeightAttributeMode(player, config);

        PacketHelper.updateSkills(player);
        PacketHelper.updatePlayerSkills(player, null);
        PacketHelper.updateLevels(player);
    }

    private static List<SkillAttribute> createDynamicSkillAttributes(
            LevelZInventoryWeightConfig.Server config
    ) {
        List<SkillAttribute> attributes = new ArrayList<>();

        if (config.skill.capacityMode != LevelZInventoryWeightConfig.SkillCapacityMode.LEVELZ_ATTRIBUTE) {
            return attributes;
        }

        attributes.add(new SkillAttribute(
                config.dynamicSkill.attributeDisplayId,
                InventoryWeightApi.getMaxWeightAttribute(),
                config.dynamicSkill.attributeBaseValue,
                config.dynamicSkill.attributeValuePerLevel,
                config.dynamicSkill.attributeOperation
        ));

        return attributes;
    }

    private static void handleInventoryWeightAttributeMode(
            ServerPlayerEntity player,
            LevelZInventoryWeightConfig.Server config
    ) {
        String skillKey = normalizeSkillKey(config.skill.skillKey);

        if (skillKey == null) {
            return;
        }

        boolean shouldUseAttributeMode =
                config.enabled
                        && config.skill.enabled
                        && config.skill.capacityMode == LevelZInventoryWeightConfig.SkillCapacityMode.LEVELZ_ATTRIBUTE
                        && LevelZSkillAccess.hasSkillKey(skillKey);

        if (shouldUseAttributeMode) {
            LevelZSkillAccess.getSkillByKey(skillKey)
                    .ifPresent(skill -> LevelHelper.updateSkill(player, skill));
        } else {
            removeLevelZInventoryWeightAttributeModifier(player, skillKey);
        }
    }

    private static void removeLevelZInventoryWeightAttributeModifier(
            ServerPlayerEntity player,
            String skillKey
    ) {
        EntityAttributeInstance instance = InventoryWeightApi.getMaxWeightAttributeInstance(player);

        if (instance == null) {
            return;
        }

        Identifier modifierId = Identifier.of("levelz", skillKey);

        if (instance.hasModifier(modifierId)) {
            instance.removeModifier(modifierId);
        }
    }

    private static String normalizeSkillKey(String skillKey) {
        if (skillKey == null) {
            return null;
        }

        String normalized = skillKey.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}
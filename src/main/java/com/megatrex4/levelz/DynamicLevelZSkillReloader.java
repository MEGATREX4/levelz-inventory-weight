package com.megatrex4.levelz;

import com.megatrex4.LevelZInventoryWeight;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Set;

public final class DynamicLevelZSkillReloader implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID =
            Identifier.of(LevelZInventoryWeight.MOD_ID, "dynamic_levelz_skill");

    private static final Identifier LEVELZ_SKILL_LOADER_ID =
            Identifier.of("levelz", "skill");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Set.of(LEVELZ_SKILL_LOADER_ID);
    }

    @Override
    public void reload(ResourceManager manager) {
        DynamicLevelZSkillManager.applyConfiguredDynamicSkill();
    }
}
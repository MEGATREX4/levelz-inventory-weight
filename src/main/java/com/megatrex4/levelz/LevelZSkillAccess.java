package com.megatrex4.levelz;

import net.levelz.level.LevelManager;
import net.levelz.level.Skill;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;

public final class LevelZSkillAccess {

    private LevelZSkillAccess() {
    }

    public static Collection<Skill> getRegisteredSkills() {
        return Collections.unmodifiableCollection(LevelManager.SKILLS.values());
    }

    public static Optional<Skill> getSkillByKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        return LevelManager.SKILLS.values()
                .stream()
                .filter(skill -> key.equals(skill.getKey()))
                .findFirst();
    }

    public static OptionalInt getSkillIdByKey(String key) {
        Optional<Skill> skill = getSkillByKey(key);

        return skill.map(value -> OptionalInt.of(value.getId()))
                .orElseGet(OptionalInt::empty);
    }

    public static boolean hasSkillKey(String key) {
        return getSkillByKey(key).isPresent();
    }

    public static int getNextAvailableSkillId() {
        return LevelManager.SKILLS.keySet()
                .stream()
                .max(Comparator.naturalOrder())
                .map(id -> id + 1)
                .orElse(0);
    }
}
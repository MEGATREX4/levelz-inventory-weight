package com.megatrex4.levelz;

import net.levelz.stats.Skill;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

public final class LevelZSkillAccess {

    private LevelZSkillAccess() {
    }

    public static Optional<Skill> getSkillByKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        String normalized = key.trim().toUpperCase();

        return Arrays.stream(Skill.values())
                .filter(skill -> skill.name().equals(normalized))
                .findFirst();
    }

    public static OptionalInt getSkillIdByKey(String key) {
        return getSkillByKey(key)
                .map(skill -> OptionalInt.of(skill.getId()))
                .orElseGet(OptionalInt::empty);
    }

    public static boolean hasSkillKey(String key) {
        return getSkillByKey(key).isPresent();
    }
}

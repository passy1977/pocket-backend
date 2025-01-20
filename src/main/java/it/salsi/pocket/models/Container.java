package it.salsi.pocket.models;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Container(
        @NotNull String token,
        @NotNull User user,
        @NotNull Device device,
        @NotNull Iterable<Group> groups,
        @NotNull Iterable<GroupField> groupsFields,
        @NotNull Iterable<Field> fields
) {}

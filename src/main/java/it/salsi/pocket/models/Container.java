package it.salsi.pocket.models;

import org.jetbrains.annotations.NotNull;

public record Container(
        @NotNull Long timestampLastUpdate,
        @NotNull User user,
        @NotNull Device device,
        @NotNull Iterable<Group> groups,
        @NotNull Iterable<GroupField> groupsFields,
        @NotNull Iterable<Field> fields
) {}

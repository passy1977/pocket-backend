package it.salsi.pocket.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Container(
        @NotNull Long timestampLastUpdate,
        @Nullable User user,
        @Nullable Device device,
        @NotNull Iterable<Group> groups,
        @NotNull Iterable<GroupField> groupsFields,
        @NotNull Iterable<Field> fields
) {}

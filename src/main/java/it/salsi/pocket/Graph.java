package it.salsi.pocket;

import it.salsi.pocket.core.BaseModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Graph<T extends BaseModel> {

    private record Node<T extends BaseModel> (
        @NotNull T t,
        @Nullable Node<T> before
    ) {

        boolean isBeforeNull() {
            return before == null;
        }

        @Override
        public String toString() {
            return String.valueOf(t.getId());
        }
    }


    private final @NotNull Map<Long, T> models = new HashMap<>();


    public boolean add(@NotNull final T t) {
        if(models.containsKey(t.getId())) {
            return false;
        }

        return models.put(t.getId(), t) == null;
    }

    @NotNull Map<Long, T> getHierarchy() {
        return models;
    }

    @NotNull Optional<List<T>> getHierarchy(@NotNull final Long id) {
        return getHierarchy(id, true);
    }

    @NotNull Optional<List<T>> getHierarchy(@NotNull final Long id, boolean reversed) {
        if(!models.containsKey(id)) {
            return Optional.empty();
        }

        List<Long> ret = new ArrayList<>();

        final var t = models.get(id);

        ret.add(t.getId());

        var serverId = t.getServerId();;
        do {
            if(serverId > 0) {
                ret.add(serverId);
                serverId = models.get(serverId).getServerId();
            }
        } while (serverId > 0);

        if(reversed) {
            return Optional.of(
                    ret.stream()
                    .map(models::get)
                    .toList()
                    .reversed()
            );
        }
        else {
            return Optional.of(
                    ret.stream()
                    .map(models::get)
                    .toList()
            );
        }


    }

}

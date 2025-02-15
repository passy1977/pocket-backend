/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/


package it.salsi.pocket.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.salsi.pocket.core.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SQLRestriction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "groups")
@SuppressWarnings("JpaDataSourceORMInspection")
public final class Group extends BaseModel<Group> {

    @EqualsAndHashCode.Include
    @Size(max = 256, message = "max size exceeded; maximum 256 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Column(nullable = false)
    private String title = "";

    @Size(max = 256, message = "max size exceeded; maximum 256 char")
    @NotNull(message = "field null")
    @Column(nullable = false)
    private String icon = "";

    @EqualsAndHashCode.Include
    @Column
    private String note = "";

    @JsonInclude
    @Transient
    private boolean shared = false;


    @Transient
    @JsonProperty("groupId")
    private @NotNull Long groupId = 0L;

    @Transient
    @JsonProperty("serverGroupId")
    private @NotNull Long serverGroupId = 0L;


    @JsonIgnore
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "group", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    private List<GroupField> groupFields;

    @Nullable
    @JsonIgnore
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "group", fetch = FetchType.EAGER)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    private List<Group> groups;

    @Nullable
    @JsonIgnore
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    private Group group;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "group", fetch = FetchType.LAZY)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    @ToString.Exclude
    private List<Field> fields;

    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User user;

    @Override
    public void switchId() {
        Long tmp = serverId;
        serverId = id;
        id = tmp;

        if(group != null) {
            if(group.getId() == 0 && group.getServerId() > 0) {
                serverGroupId = group.getServerId();
            } else if(group.getId() > 0 && group.getServerId() == 0) {
                serverGroupId = group.getId();
            } else {
                serverGroupId = 0L;
            }
        }
    }

    @Override
    public void postStore(final @org.jetbrains.annotations.NotNull Group group) {
        serverId = id;
        id = group.getServerId();

        if(this.group != null) {
            serverGroupId = this.group.getId();
        }
        groupId = group.getServerGroupId();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Group group = (Group) o;
        return id != null && Objects.equals(id, group.id);
    }

    @Contract(pure = true)
    @Override
    public @org.jetbrains.annotations.NotNull String toString() {
        return String.valueOf(id);
    }
}
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
import it.salsi.pocket.core.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "fields")
public final class Field extends BaseModel<Field> {

    @EqualsAndHashCode.Include
    @Size(max = 256, message = "max size exceeded; maximum 256 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Column(nullable = false)
    private String title = "";

    @EqualsAndHashCode.Include
    @Size(max = 256, message = "max size exceeded; maximum 256 char")
    @NotNull(message = "field null")
    @Column(nullable = false)
    private String value = "";

    @Column(nullable = false)
    private Boolean isHidden = false;

    @Column(nullable = false)
    @NotNull
    private Long groupFieldId = 0L;

    @Transient
    @NotNull
    private Long serverGroupFieldId = 0L;

    @Transient
    private @NotNull Long groupId = 0L;

    @Transient
    private @NotNull Long serverGroupId = 0L;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User user;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    @ManyToOne
    @SQLRestriction("deleted = 0")
    private Group group;

    @Override
    public void switchId() {
        Long tmp = serverId;
        serverId = id;
        id = tmp;

        tmp = serverGroupFieldId;
        serverGroupFieldId = groupFieldId;
        groupFieldId = tmp;

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
    public void postStore(final @org.jetbrains.annotations.NotNull Field field) {
        serverId = id;
        id = field.getServerId();

        if(this.group != null) {
            serverGroupId = this.group.getId();
        }
        groupId = field.getServerGroupId();

        serverGroupFieldId = groupFieldId;
        groupFieldId = field.getServerGroupFieldId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Field field = (Field) o;
        return id != null && Objects.equals(id, field.id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}

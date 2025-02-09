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


package it.salsi.pocket.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.salsi.pocket.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
@ToString
@RequiredArgsConstructor
abstract public class BaseModel implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id = 0L;

    @JsonInclude
    @Transient
    protected Long serverId = 0L;

    @Column(nullable = false)
    protected Boolean deleted = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Long timestampCreation = Instant.now(Clock.systemUTC()).getEpochSecond();

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Long timestampLastUpdate = 0L;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    abstract public void setUser(@NotNull User user);

    @NotNull
    abstract public User getUser();

    public abstract void switchId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final var baseModel = (BaseModel) o;
        return id != null && Objects.equals(id, baseModel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

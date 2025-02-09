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
import it.salsi.pocket.Constant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.Objects;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "properties")
@SuppressWarnings("JpaDataSourceORMInspection")
public final class Property {

    @SuppressWarnings("unused")
    public enum Type {
        REAL, INTEGER, STRING, BOOLEAN
    }

    @ToString.Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @Size(max = 128, message = "max size exceeded, maximum 128 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Column(name = "_key", nullable = false)
    private String key = "";

    @Size(max = 256, message = "max size exceeded, maximum 256 char")
    private String value = "";

    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User user;

    @Column(nullable = false)
    private Type type = Type.STRING;

    public void setKey(@org.jetbrains.annotations.NotNull final Constant key) {
        this.key = key.value;
    }

    public void setValue(@org.jetbrains.annotations.Nullable final Object value) {
        if (value == null) {
            return;
        }
        this.value = value.toString();
    }


    @JsonIgnore
    @org.jetbrains.annotations.NotNull
    public Constant.MetaProperty getMetaProperty() {
        return Constant.getMetaProperty(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Property property = (Property) o;
        return id != null && Objects.equals(id, property.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
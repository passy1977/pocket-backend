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
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static it.salsi.pocket.models.User.Status.ACTIVE;
import static it.salsi.pocket.models.User.Status.NOT_ACTIVE;

@Getter
@Setter
@Entity(name = "users")
@SuppressWarnings({"JpaDataSourceORMInspection", "unused"})
public final class User implements Cloneable {

    public enum Status {
        NOT_ACTIVE, ACTIVE, DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @Size(max = 256, message = "max size exceeded, maximum 256 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Email(message = "email wrong")
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 256, message = "max size exceeded, maximum 256 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Column(nullable = false)
    private String name;

    @Size(max = 256, message = "max size exceeded, maximum 256 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Column(nullable = false)
    @JsonIgnore
    private String passwd;

    @Column(nullable = false)
    private Status status = ACTIVE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Long timestampCreation = 0L;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Device> devices;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Field> fields;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Group> groups;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<GroupField> groupFields;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Property> properties;

    @org.jetbrains.annotations.NotNull
    public static User build(@org.jetbrains.annotations.NotNull final String email) {
        final var ret = new User();
        ret.setEmail(email);
        ret.setStatus(NOT_ACTIVE);
        return ret;
    }

    public User() {}

    public User(
            @NotNull final String name,
            @NotNull final String email,
            @NotNull final String passwd
    ) {
        this.email = email;
        this.name = name;
        this.passwd = passwd;
        setTimestampCreation(Instant.now(Clock.systemUTC()).getEpochSecond());
    }

    @Override
    @NotNull
    public User clone() throws CloneNotSupportedException {
        return (User) super.clone();
    }

    @Override
    @NotNull
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}

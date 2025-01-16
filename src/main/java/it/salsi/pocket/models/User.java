/*
The MIT License (MIT)

Original Work Copyright (c) 2018-2025 Antonio Salsi

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package it.salsi.pocket.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static it.salsi.pocket.Constant.DATE_TIME_FORMAT;
import static it.salsi.pocket.models.User.Status.ACTIVE;
import static it.salsi.pocket.models.User.Status.UNACTIVE;

@Getter
@Setter
@Entity(name = "users")
@SuppressWarnings({"JpaDataSourceORMInspection", "unused"})
public final class User implements Cloneable {

    public enum Status {
        UNACTIVE, ACTIVE, DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @JsonInclude
    @Transient
    private Long serverId = 0L;

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
    private String passwd;

    @Transient
    @NotNull
    private String hostAuthUser = "";

    @Transient
    @NotNull
    private String hostAuthPasswd = "";

    @Column(nullable = false)
    private Status status = ACTIVE;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
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
        ret.setStatus(UNACTIVE);
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
    }

    @Override
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

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "groups")
@SuppressWarnings("JpaDataSourceORMInspection")
public final class Group extends BaseModel {

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

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "group", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    private List<GroupField> groupFields;

    @Nullable
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "group", fetch = FetchType.EAGER)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    private List<Group> groups;

    @Nullable
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    private Group group;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "group", fetch = FetchType.LAZY)
//    @Where(clause = "deleted = 0")
    @SQLRestriction("deleted = 0")
    @ToString.Exclude
    private List<Field> fields;

    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Group group = (Group) o;
        return id != null && Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
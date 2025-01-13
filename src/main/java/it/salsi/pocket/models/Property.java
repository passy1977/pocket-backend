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
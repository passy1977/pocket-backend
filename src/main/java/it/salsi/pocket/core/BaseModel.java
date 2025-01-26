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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
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
        BaseModel baseModel = (BaseModel) o;
        return id != null && Objects.equals(id, baseModel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

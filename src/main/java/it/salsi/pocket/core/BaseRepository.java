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

import it.salsi.pocket.models.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Date;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T> extends CrudRepository<T, Long> {

    Optional<T> findByUserAndId(@NotNull final User userId, @NotNull final Long id);

    Iterable<T> findByUser(@NotNull final User userId);

    Iterable<T> findByUserAndDateTimeLastUpdateGreaterThan(@NotNull final User userId, @NotNull final Date dateTimeLastUpdate);

    Iterable<T> findByUserAndDeletedAndDateTimeLastUpdateLessThan(@NotNull final User userId, boolean deleted, @NotNull final Date dateTimeLastUpdate);
}

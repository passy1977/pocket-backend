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


package it.salsi.pocket.repositories;

import it.salsi.pocket.core.BaseRepository;
import it.salsi.pocket.models.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupRepository extends BaseRepository<Group> {
    
    @EntityGraph(attributePaths = {"fields"})
    @Query("SELECT g FROM groups g WHERE g.id = :id")
    Optional<Group> findByIdWithFields(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"groupFields"})
    @Query("SELECT g FROM groups g WHERE g.id = :id")
    Optional<Group> findByIdWithGroupFields(@Param("id") Long id);
}
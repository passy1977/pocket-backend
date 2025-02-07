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

package it.salsi.pocket.rests;

import it.salsi.pocket.core.BaseRest;
import it.salsi.pocket.models.Field;
import it.salsi.pocket.repositories.DeviceRepository;
import it.salsi.pocket.repositories.FieldRepository;
import it.salsi.pocket.repositories.GroupRepository;
import it.salsi.pocket.repositories.UserRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@Log
@RestController
@RequestMapping("${server.api-version}/field/")
public final class FieldRest extends BaseRest<Field, FieldRepository> {

    public FieldRest(@Autowired @NotNull final FieldRepository repository,
                     @Autowired @NotNull final GroupRepository groupRepository,
                     @Autowired @NotNull final DeviceRepository deviceRepository,
                     @Autowired @NotNull final UserRepository userRepository) {
        super(repository, deviceRepository, userRepository);

        setOnSave(field -> {
            if (field.getGroup() == null) {
                return field;
            }

            Field original;
            try {
                original = (Field) field.clone();
            } catch (CloneNotSupportedException e) {
                log.severe(e.getMessage());
                return field;
            }

            field.setGroupFieldId(original.getServerGroupFieldId());

            groupRepository.findById(field.getGroup().getServerId()).ifPresent(field::setGroup);

            return original;
        });

        setOnReturn((original, ret) -> {
            ret.setGroup(original.getGroup());
            ret.setGroupFieldId(original.getGroupFieldId());
        });
    }

}

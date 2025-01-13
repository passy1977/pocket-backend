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

package it.salsi.pocket.services;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.Constant;
import it.salsi.pocket.models.Property;
import it.salsi.pocket.repositories.PropertyRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log
@Service
public final class PropertiesManagerImpl implements PropertiesManager {

//
//    @Value("${basic.auth.passwd}")
//    @Nullable
//

    @NotNull
    private final PropertyRepository propertyRepository;

    public PropertiesManagerImpl(@Autowired @NotNull PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void checkAll() throws CommonsException {

        log.info("start checks");

        for (final var constant : Constant.values()) {
            final var metaProperty = constant.getMetaProperty();

            if (metaProperty.mandatory()) {
                final var opt = propertyRepository.getByKey(constant);
                if (opt.isEmpty()) {
                    var property = new Property();
                    property.setKey(metaProperty.constant());

                    Optional.ofNullable(
                            metaProperty.defaultValue()
                    ).ifPresentOrElse(
                            property::setValue,
                            () -> property.setValue("")
                    );

                    property.setType(metaProperty.type());
                    propertyRepository.save(property);
                    log.info("add property: " + property);
                }
            }
        }

//        if (authPasswd != null && authPasswd.length() != 32) {
//            throw new CommonsException("Crypto key must be 32 char");
//        }

        log.info("end checks");
    }

}

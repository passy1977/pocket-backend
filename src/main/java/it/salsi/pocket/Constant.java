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

package it.salsi.pocket;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static it.salsi.pocket.models.Property.Type.*;

public enum Constant {

    CRYPTO_IV("h7QWwZ2eVGow03X9"),
    EMAIL_ALREADY_USED("Email already used"),
    EMAIL_HOST_WRONG_CREDENTIAL("Wrong baseAuth credential for this server"),
    PROPERTY_DB_VERSION("dbVersion"),
    PROPERTY_INVALIDATOR_ENABLE("invalidatorEnable"),
    PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS("invalidatorMaxLoginDays"),
    PROPERTY_CLEAN_DATA_ENABLE("cleanDataEnable"),
    DIVISOR("|"),
    FOO("");


    @NotNull
    public static Constant getEnum(@NotNull final String str) {
        return switch (str) {
            case "dbVersion" -> PROPERTY_DB_VERSION;
            case "invalidatorEnable" -> PROPERTY_INVALIDATOR_ENABLE;
            case "invalidatorMaxLoginDays" -> PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS;
            case "cleanDataEnable" -> PROPERTY_CLEAN_DATA_ENABLE;
            default -> FOO;
        };
    }

    @Builder
    public record MetaProperty(@NotNull Constant constant,
                               @NotNull it.salsi.pocket.models.Property.Type type,
                               @NotNull String description,
                               @Nullable Object defaultValue,
                               boolean mandatory,
                               boolean noShow,
                               boolean toApp) {}

    @NotNull
    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public final static int SERVER_CONNECTOR_PORT_DEFAULT = 8090;


    @NotNull
    public final String value;

    Constant(@NotNull final String value) {
        this.value = value;
    }

    @NotNull
    public MetaProperty getMetaProperty() {
        int DATABASE_VERSION = 5;

        return switch (this) {
            case PROPERTY_DB_VERSION -> new MetaProperty(this, INTEGER, "", DATABASE_VERSION, true, true, true);
            case PROPERTY_CLEAN_DATA_ENABLE -> new MetaProperty(this, BOOLEAN, "Enable old data deleting", false, true, true, false);
            case PROPERTY_INVALIDATOR_ENABLE -> new MetaProperty(this, BOOLEAN, "Enable invalidate unused account", false, true, true, false);
            case PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS -> new MetaProperty(this, INTEGER, "Day after that a client will be disable", 30, true, true, false);
            default -> new MetaProperty(FOO, STRING, "", "", false, true, false);
        };
    }

    @NotNull
    public static MetaProperty getMetaProperty(@NotNull final String key) {
        return switch (key) {
            case "dbVersion" -> PROPERTY_DB_VERSION.getMetaProperty();
            case "invalidatorEnable" -> PROPERTY_INVALIDATOR_ENABLE.getMetaProperty();
            case "invalidatorMaxLoginDays" -> PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS.getMetaProperty();
            case "cleanDataEnable" -> PROPERTY_CLEAN_DATA_ENABLE.getMetaProperty();
            default -> FOO.getMetaProperty();
        };
    }

}

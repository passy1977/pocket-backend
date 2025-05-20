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
    PROPERTY_INVALIDATOR_CACHE_MAX_MINUTES("invalidatorCacheMaxMinutes"),
    PROPERTY_CLEAN_DATA_ENABLE("cleanDataEnable"),
    DIVISOR("|"),
    FOO("");


    @NotNull
    public static Constant getEnum(@NotNull final String str) {
        return switch (str) {
            case "dbVersion" -> PROPERTY_DB_VERSION;
            case "invalidatorEnable" -> PROPERTY_INVALIDATOR_ENABLE;
            case "invalidatorMaxLoginDays" -> PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS;
            case "invalidatorCacheMaxMinutes" -> PROPERTY_INVALIDATOR_CACHE_MAX_MINUTES;
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
            case PROPERTY_INVALIDATOR_ENABLE -> new MetaProperty(this, BOOLEAN, "Enable invalidate unused account", true, true, true, false);
            case PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS -> new MetaProperty(this, INTEGER, "Day after that a client will be disable", 30, true, true, false);
            case PROPERTY_INVALIDATOR_CACHE_MAX_MINUTES -> new MetaProperty(this, INTEGER, "Amount of minutes after that a login cache will be delete", 10, true, true, false);
            default -> new MetaProperty(FOO, STRING, "", "", false, true, false);
        };
    }

    @NotNull
    public static MetaProperty getMetaProperty(@NotNull final String key) {
        return switch (key) {
            case "dbVersion" -> PROPERTY_DB_VERSION.getMetaProperty();
            case "invalidatorEnable" -> PROPERTY_INVALIDATOR_ENABLE.getMetaProperty();
            case "invalidatorMaxLoginDays" -> PROPERTY_INVALIDATOR_MAX_LOGIN_DAYS.getMetaProperty();
            case "invalidatorCacheMaxMinutes" -> PROPERTY_INVALIDATOR_CACHE_MAX_MINUTES.getMetaProperty();
            case "cleanDataEnable" -> PROPERTY_CLEAN_DATA_ENABLE.getMetaProperty();
            default -> FOO.getMetaProperty();
        };
    }

}

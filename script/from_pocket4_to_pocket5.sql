use pocket4;

SET foreign_key_checks = 0;

ALTER TABLE devices 
    DROP COLUMN date_time_last_login, 
    DROP COLUMN date_time_last_update,
    CHANGE device_serial `uuid` varchar(256) NOT NULL,
    ADD COLUMN `note` tinytext DEFAULT NULL,
    ADD COLUMN `public_key` longtext NOT NULL,
    ADD COLUMN `private_key` longtext NOT NULL,
    ADD COLUMN `timestamp_last_login` bigint(20) NOT NULL DEFAULT 0,
    ADD COLUMN `timestamp_last_update` bigint(20) NOT NULL DEFAULT 0,
    ADD COLUMN `timestamp_creation` bigint(20) NOT NULL DEFAULT 0,
    DROP COLUMN token;
DELETE FROM devices;

ALTER TABLE fields 
    ADD COLUMN timestamp_last_update bigint(20) DEFAULT 0,
    ADD COLUMN timestamp_creation bigint(20) DEFAULT 0;
UPDATE fields SET timestamp_creation = UNIX_TIMESTAMP(date_time_last_update);
ALTER TABLE fields DROP COLUMN  date_time_last_update;

ALTER TABLE group_fields 
    ADD COLUMN timestamp_last_update bigint(20) DEFAULT 0,
    ADD COLUMN timestamp_creation bigint(20) DEFAULT 0;
UPDATE group_fields SET timestamp_creation = UNIX_TIMESTAMP(date_time_last_update);
ALTER TABLE group_fields DROP COLUMN  date_time_last_update;

ALTER TABLE `groups` 
    ADD COLUMN timestamp_last_update bigint(20) DEFAULT 0,
    ADD COLUMN timestamp_creation bigint(20) DEFAULT 0;
UPDATE groups SET timestamp_creation = UNIX_TIMESTAMP(date_time_last_update);
ALTER TABLE groups DROP COLUMN  date_time_last_update;

ALTER TABLE users ADD COLUMN timestamp_creation bigint(20) DEFAULT 0;

DROP TABLE users_roles;
DROP TABLE roles;

SET foreign_key_checks = 1;


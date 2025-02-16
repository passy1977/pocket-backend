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


package it.salsi.pocket.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.salsi.commons.CommonsException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Clock;
import java.time.Instant;
import java.util.StringTokenizer;
import java.util.UUID;

import static it.salsi.pocket.models.Device.Status.ACTIVE;

@ToString
//@Data no perchè crash OneToMany
@Entity(name = "devices")
@Table(
        indexes = {@Index(name = "idx_devices_device_serial", columnList = "deviceSerial"), @Index(name = "idx_devices_token", columnList = "token")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"token"})}
)
@Getter
@Setter
@SuppressWarnings("JpaDataSourceORMInspection")
public class Device {


    @SuppressWarnings("unused")
    public enum Status {
        NOT_ACTIVE, ACTIVE, DELETED, INVALIDATED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

    @Size(max = 256, message = "max size exceeded; maximum 256 char")
    @NotEmpty(message = "field empty")
    @NotNull(message = "field null")
    @Column(nullable = false)
    private String uuid = "";

    @JsonIgnore
    private String version;

    @JsonIgnore
    private String address;

    @Column(nullable = false)
    private Status status = ACTIVE;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Long timestampLastUpdate = 0L;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Long timestampLastLogin = 0L;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Long timestampCreation = Instant.now(Clock.systemUTC()).getEpochSecond();

    @ToString.Exclude
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @JsonIgnore
    @Lob
    private String note;

    @ToString.Exclude
    @JsonIgnore
    @Lob
    private String publicKey;

    @ToString.Exclude
    @JsonIgnore
    @Lob
    private String privateKey;


    public @NotNull String getPublicKey() throws CommonsException {
        if(publicKey.isEmpty()) {
            return "";
        }

        final var tokenizer = new StringTokenizer(publicKey, "[\n]");
        if(tokenizer.countTokens() != 3) {
            throw new CommonsException("Token number != 3");
        }
        tokenizer.nextToken();
        return tokenizer.nextToken();
    }

    public @NotNull String getPrivateKey() throws CommonsException {
        if(privateKey.isEmpty()) {
            return "";
        }
        final var tokenizer = new StringTokenizer(privateKey, "[\n]");
        if(tokenizer.countTokens() != 3) {
            throw new CommonsException("Token number != 3");
        }
        tokenizer.nextToken();
        return tokenizer.nextToken();
    }

    public void updateTimestampLastLogin() {
        timestampLastLogin = Instant.now(Clock.systemUTC()).getEpochSecond();
    }

    public void updateTimestampLastUpdate() {
        timestampLastUpdate = Instant.now(Clock.systemUTC()).getEpochSecond();
    }

    public Device() {}

    public Device(@org.jetbrains.annotations.NotNull final User user) {
        this();
        setUser(user);
        setUuid(UUID.randomUUID().toString());
        setStatus(Device.Status.ACTIVE);
        updateTimestampLastLogin();
        updateTimestampLastUpdate();
    }
}

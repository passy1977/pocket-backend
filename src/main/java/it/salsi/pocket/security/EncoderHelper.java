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

package it.salsi.pocket.security;

import it.salsi.commons.CommonsException;
import it.salsi.commons.utils.Crypto;
import it.salsi.commons.utils.CryptoBuilder;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Log
public class EncoderHelper {

    @Nullable
    private MessageDigest md;

    @Value("${server.aes.cbc.iv}")
    @Nullable
    private String aesCrbIv;

    public static final int KEY_SIZE = 32;
    private static final char PADDING = '$';

    @NotNull
    public String encode(@NotNull final CharSequence rawPassword) {

        if (md == null) {
            try {
                md = MessageDigest.getInstance("SHA-512");
            } catch (NoSuchAlgorithmException e) {
                log.severe(e.getLocalizedMessage());
                return "";
            }
        }

        return bytesToHex(md.digest(rawPassword.toString().getBytes()));
    }

    @NotNull
    private static String bytesToHex(final byte @NotNull [] hashInBytes) {
        final var sb = new StringBuilder();
        for (final var hashInByte : hashInBytes) {
            sb.append(Integer.toString((hashInByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public @NotNull Crypto getCrypto(@NotNull final String authPasswd) throws CommonsException {
        if (aesCrbIv == null) {
            throw new CommonsException("AES CRC IV not set");
        }
        if (aesCrbIv.length() != 16) {
            throw new CommonsException("AES CRC IV must be 16 byte");
        }

        var localAuthPasswd = new char[KEY_SIZE];
        byte i = 0;
        for (; i < authPasswd.length() && i < KEY_SIZE; i++) {
            localAuthPasswd[i] = authPasswd.charAt(i);
        }
        for (; i < KEY_SIZE; i++) {
            localAuthPasswd[i] = PADDING;
        }

        return new CryptoBuilder()
                .setDecodeBase64Callback(Base64.getDecoder()::decode)
                .setEncodeBase64Callback(Base64.getEncoder()::encode)
                .setKey(String.valueOf(localAuthPasswd))
                .setIV(aesCrbIv)
                .setCipher("AES/CBC/PKCS5Padding")
                .build();
    }

}

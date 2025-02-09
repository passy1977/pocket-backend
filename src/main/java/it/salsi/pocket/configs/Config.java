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


package it.salsi.pocket.configs;

import it.salsi.commons.CommonsException;
import it.salsi.commons.utils.Crypto;
import it.salsi.commons.utils.CryptoBuilder;
import it.salsi.pocket.security.PasswordEncoder;
import it.salsi.pocket.security.RSAHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

import static it.salsi.pocket.Constant.CRYPTO_IV;

@Configuration
public class Config {

    @Value("${server.auth.passwd}")
    @Nullable
    private String authPasswd;

    @NotNull
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }

    @NotNull
    @Bean
    public Crypto getCrypto() throws CommonsException {
        if (authPasswd == null) {
            throw new CommonsException("Key null");
        }
        return new CryptoBuilder()
                .setDecodeBase64Callback(Base64.getUrlDecoder()::decode)
                .setEncodeBase64Callback(Base64.getUrlEncoder()::encode)
                .setKey(authPasswd)
                .setIV(CRYPTO_IV.value)
                .setCipher("AES/CBC/PKCS5Padding")
                .build();
    }

    @NotNull
    @Bean
    public RSAHelper getRSAHelper() throws CommonsException {
        return new RSAHelper(RSAHelper.ALGORITHM, RSAHelper.KEY_SIZE);
    }

}

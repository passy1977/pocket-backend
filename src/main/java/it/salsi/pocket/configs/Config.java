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

    @Value("${basic.auth.passwd}")
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
        return new RSAHelper("RSA", 2048);
    }
}

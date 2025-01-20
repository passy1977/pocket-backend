package it.salsi.pocket.security;

import it.salsi.commons.CommonsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final public class RSAHelper  {

    private final @NotNull String algorithm;

    @NotNull
    private final KeyPair pair;

    @Nullable
    private PrivateKey privateKey = null;

    @Nullable
    private PublicKey publicKey = null;

    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 2048;

    public RSAHelper(@NotNull String algorithm, int keySize) throws CommonsException {
        this.algorithm = algorithm;
        try {
            final var generator = KeyPairGenerator.getInstance(algorithm);
            generator.initialize(keySize);
            pair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new CommonsException(e);
        }
    }

    public byte @Nullable [] getPrivateKey() {
        if(privateKey == null) {
            return null;
        }
        return privateKey.getEncoded();
    }

    public byte @Nullable [] getPublicKey() {
        if(publicKey == null) {
            return null;
        }
        return publicKey.getEncoded();
    }

    public void enroll() {
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }


    public void loadPrivateKey(byte[] keyBytes) throws CommonsException {
        try {
            final var keyFactory = KeyFactory.getInstance(algorithm);
            final var privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CommonsException(e);
        }
    }

    public void loadPublicKey(byte[] keyBytes) throws CommonsException {
        try {
            final var keyFactory = KeyFactory.getInstance(algorithm);
            final var publicKeySpec = new X509EncodedKeySpec(keyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CommonsException(e);
        }
    }

    public byte @NotNull [] encrypt(final byte @NotNull[] buffer) throws CommonsException {
        try {
            final var rsa = Cipher.getInstance(algorithm);
            rsa.init(Cipher.ENCRYPT_MODE, publicKey);
            return rsa.doFinal(buffer);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new CommonsException(e);
        }
    }

    public @NotNull String encryptToString(final byte @NotNull[] buffer) throws CommonsException {
        final var result = new StringBuilder();

        for (final var b : encrypt(buffer)) {
            result.append(String.format("%02X", b));
        }

        return result.toString();
    }

    public @NotNull String decrypt(byte[] buffer) throws CommonsException {
        try {
            final var rsa = Cipher.getInstance(algorithm);
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] utf8 = rsa.doFinal(buffer);
            return new String(utf8, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CommonsException(e);
        }
    }

    public @NotNull String decryptFromString(@NotNull final String buffer) throws CommonsException {
        return decrypt(buffer.getBytes(StandardCharsets.UTF_8));
    }

}

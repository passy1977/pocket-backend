package it.salsi.pocket.security;

import it.salsi.commons.CommonsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

final public class RSAHelper  {

    private @NotNull String algorithm;

    @NotNull
    private final KeyPair pair;

    @Nullable
    private PrivateKey privateKey = null;

    @Nullable
    private PublicKey publicKey = null;

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

    public void enroll() {
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }


    public void loadPrivateKey(byte[] keyBytes) throws CommonsException {
        try {
            final var keyFactory = KeyFactory.getInstance(algorithm);
            final var publicKeySpec = new X509EncodedKeySpec(keyBytes);
            privateKey = keyFactory.generatePrivate(publicKeySpec);
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


    public byte @NotNull [] encrypt(@NotNull final String Buffer) throws CommonsException {
        try {
            Cipher rsa;
            rsa = Cipher.getInstance(algorithm);
            rsa.init(Cipher.ENCRYPT_MODE, privateKey);
            return rsa.doFinal(Buffer.getBytes());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new CommonsException(e);
        }
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

}

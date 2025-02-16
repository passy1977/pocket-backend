package it.salsi.pocket.security;


import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Log
public class PasswordEncoder {

    @Nullable
    private MessageDigest md;

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

}

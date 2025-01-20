package it.salsi.pocket.security;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.models.Device;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public final class TokenHelper {

    private @NotNull final RSAHelper rsaHelper;

    private @NotNull final String token;



    /**
     *
     */
    public TokenHelper(
            @NotNull final Device device,
            @NotNull final String token
    ) throws CommonsException {
        this.token = token;

        rsaHelper = new RSAHelper(RSAHelper.ALGORITHM, RSAHelper.KEY_SIZE);
        rsaHelper.loadPrivateKey(device.getPrivateKey().getBytes(StandardCharsets.UTF_8));
        rsaHelper.loadPublicKey(device.getPublicKey().getBytes(StandardCharsets.UTF_8));


    }


}

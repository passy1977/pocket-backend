package it.salsi.pocket.services;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.Constant;
import it.salsi.pocket.security.RSAHelper;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.security.*;
import java.util.Arrays;
import java.util.stream.Stream;

@Setter
@Log
@Service
public class IpcSocketManagerImpl implements IpcSocketManager {

    private boolean loop = true;

    private @NotNull RSAHelper rsaHelper;

    public IpcSocketManagerImpl(@Autowired @NotNull RSAHelper rsaHelper) throws NoSuchAlgorithmException {
        this.rsaHelper = rsaHelper;

        rsaHelper.enroll();
    }




    @Async
    @Override
    public void start() {

        log.info("start socket");

        try (final var serverSocket = new ServerSocket(Constant.SOCKET_PORT)) {

            while (loop) {
                final var client = serverSocket.accept();

                final var out = new PrintWriter(client.getOutputStream(), true);
                final var in = new BufferedReader(new InputStreamReader(client.getInputStream()));



                String line;
                while ((line = in.readLine()) != null) {

                    final var result = new StringBuilder();

                    for (byte b : rsaHelper.encrypt(line)) {
                        result.append(String.format("%02X", b));
                    }

                    out.println(result);

                }

            }


        } catch (IOException | CommonsException e) {
            log.severe(e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info("end socket");
    }
}

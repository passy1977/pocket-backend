package it.salsi.pocket.services;

import org.springframework.scheduling.annotation.Async;

public interface IpcSocketManager {
    @Async
    void start();
}

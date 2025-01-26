package it.salsi.pocket.rests;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.controllers.SessionController;
import it.salsi.pocket.models.Container;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
@RequestMapping("${server.api-version}/session/")
public class SessionRest {

    private @NotNull final SessionController sessionController;

    public SessionRest(@Autowired @NotNull SessionController sessionController) {
        this.sessionController = sessionController;
    }

    @GetMapping("/{uuid}/{timestampLastUpdate}/{email}/{passwd}")
    public @NotNull ResponseEntity<Container> getFullData(@PathVariable @NotNull final String uuid,
                                                    @PathVariable @NotNull final Long timestampLastUpdate,
                                                    @PathVariable @NotNull final String email,
                                                    @PathVariable @NotNull final String passwd,
                                                    @NotNull final HttpServletRequest request) throws CommonsException
    {
        return sessionController.getFullData(uuid, timestampLastUpdate, email, passwd);
    }

}

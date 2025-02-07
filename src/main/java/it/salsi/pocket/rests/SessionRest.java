package it.salsi.pocket.rests;

import it.salsi.commons.CommonsException;
import it.salsi.pocket.controllers.SessionController;
import it.salsi.pocket.models.Container;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log
@RestController
@RequestMapping("${server.api-version}/session/")
public class SessionRest {

    private @NotNull final SessionController sessionController;

    public SessionRest(@Autowired @NotNull SessionController sessionController) {
        this.sessionController = sessionController;
    }

    @GetMapping("/{uuid}/{crypt}/{email}/{passwd}")
    public @NotNull ResponseEntity<Container> getData(@PathVariable @NotNull final String uuid,
                                                          @PathVariable @NotNull final String crypt,
                                                          @PathVariable @NotNull final String email,
                                                          @PathVariable @NotNull final String passwd) throws CommonsException
    {
        return sessionController.getData(uuid, crypt, email, passwd);
    }

    @PostMapping("/{uuid}/{crypt}/{timestampLastUpdate}")
    public @NotNull ResponseEntity<Container> setData(@PathVariable @NotNull final String uuid,
                                                          @PathVariable @NotNull final String crypt,
                                                          @Valid @NotNull @RequestBody final List<Container> list) throws CommonsException
    {
        return sessionController.setData(uuid, crypt, list);
    }


}

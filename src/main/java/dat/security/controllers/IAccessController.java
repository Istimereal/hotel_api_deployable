package dat.security.controllers;

import dat.security.exceptions.NotAuthorizedException;
import io.javalin.http.Context;

public interface IAccessController {
    void accessHandler(Context ctx) throws NotAuthorizedException;
}

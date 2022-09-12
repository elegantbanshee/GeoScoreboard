package org.rolando.util;

import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Session;

public class SessionHandler {
    public static final String LOGGED_IN = "logged_in";
    public static final String EMAIL = "email";

    public static void createSession(Request request, String email) {
        Session session = request.session(true);
        session.attribute(LOGGED_IN, "true");
        session.attribute(EMAIL, email);
    }

    @Nullable
    public static String getEmail(Request request) {
        if (!isLoggedIn(request))
            return null;
        return request.session(false).attribute(EMAIL);
    }

    private static boolean isLoggedIn(Request request) {
        return request.session(false) != null;
    }
}

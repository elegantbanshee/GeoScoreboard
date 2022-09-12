package org.rolando;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rolando.data.Constants;
import org.rolando.util.Logger;
import org.rolando.util.PostgresUtil;

import java.io.*;
import java.util.logging.Level;

import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class GeoScoreboard {
    public static void main(String[] args) {
        // Logging initialization
        Logger.setLevel(Level.ALL);
        Logger.info("Starting GeoScoreboard %s", Constants.VERSION);

        // Parse port
        int port = 5000;
        String portString = System.getenv("PORT");
        try {
            if (portString != null && !portString.isEmpty())
                port = Integer.parseInt(portString);
        }
        catch (NumberFormatException e) {
            Logger.warn("Failed to parse PORT env var: %s", portString);
        }

        // Set values
        port(port);
        staticFiles.location("/static/");
        staticFiles.expireTime(1);

        // Web
        GeoScoreboardServer.getGeneric("/", "index.hbs");
        GeoScoreboardServer.getGeneric("/login", "login.hbs");
        GeoScoreboardServer.getGeneric("/signup", "signup.hbs");
        GeoScoreboardServer.getGeneric("/demo", "demo.hbs");
        GeoScoreboardServer.getScoreboard("/get");

        GeoScoreboardServer.postSignup("/signup");
        GeoScoreboardServer.postLogin("/login");
        GeoScoreboardServer.postResetApiKey("/reset_api_key");
        GeoScoreboardServer.postPublish("/publish");

        // Init
        PostgresUtil.init();
    }
}



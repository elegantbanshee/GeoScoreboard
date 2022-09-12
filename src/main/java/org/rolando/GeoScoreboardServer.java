package org.rolando;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rolando.data.ScoreBoard;
import org.rolando.util.CityUtil;
import org.rolando.util.KeyUtil;
import org.rolando.util.PostgresUtil;
import org.rolando.util.SessionHandler;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.*;

public class GeoScoreboardServer {
    public static void getGeneric(String path, String templatePath) {
        spark.Spark.get(path, (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String email = SessionHandler.getEmail(request);
            model.put("email", Objects.requireNonNullElse(email, ""));
            if (email != null)
                model.put("api_key", PostgresUtil.getApiKey(email));
            return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
        });
    }

    public static void postSignup(String path) {
        spark.Spark.post(path, (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String email = request.queryParams("email");
            String password = request.queryParams("password");
            if (email == null || email.length() <= 3 || password == null || password.length() < 3) {
                model.put("status", "invalid_data");
                return new HandlebarsTemplateEngine().render(new ModelAndView(model,
                        "signup.hbs"));
            }
            if (PostgresUtil.doesEmailExist(email)) {
                model.put("status", "email_exists");
                return new HandlebarsTemplateEngine().render(new ModelAndView(model,
                        "signup.hbs"));
            }
            PostgresUtil.createUser(email, password);
            model.put("status", "success");
            SessionHandler.createSession(request, email);
            return new HandlebarsTemplateEngine().render(new ModelAndView(model, "signup.hbs"));
        });
    }

    public static void postLogin(String path) {
        spark.Spark.post(path, (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String email = request.queryParams("email");
            String password = request.queryParams("password");
            model.put("status", "false");
            if (KeyUtil.checkPassword(email, password)) {
                SessionHandler.createSession(request, email);
                model.put("status", "true");
            }
            return new HandlebarsTemplateEngine().render(new ModelAndView(model, "login.hbs"));
        });
    }

    public static void postResetApiKey(String path) {
        spark.Spark.post(path, (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String email = SessionHandler.getEmail(request);
            PostgresUtil.resetApiKey(email);
            return new HandlebarsTemplateEngine().render(new ModelAndView(model, "reset_api_key.hbs"));
        });
    }

    public static void postPublish(String path) {
        spark.Spark.post(path, (request, response) -> {
            String[] queryParams = request.body().split("&");
            String scoreboard = queryParams[0].split("=")[1];
            float longitude = Float.parseFloat(queryParams[1].split("=")[1]);
            float latitude = Float.parseFloat(queryParams[2].split("=")[1]);
            int score = Integer.parseInt(queryParams[3].split("=")[1]);
            String apiKey = queryParams[4].split("=")[1];

            PostgresUtil.publishScore(apiKey, scoreboard,
                    longitude, latitude, score);
            return "200";
        });
    }

    public static void getScoreboard(String path) {
        spark.Spark.get(path, (request, response) -> {
            String apiKey = request.queryParams("api_key");
            String scoreboardName = request.queryParams("scoreboard");
            float longitude = Float.parseFloat(request.queryParams("longitude"));
            float latitude = Float.parseFloat(request.queryParams("latitude"));
            String cityCountry = CityUtil.getCityCountryName(longitude, latitude);
            String cityCountryName = String.format("%s:%s", scoreboardName, cityCountry);

            List<ScoreBoard> scoreboards = PostgresUtil.getScoreboards(apiKey, cityCountryName);
            if (scoreboards != null) {
                JSONArray jsonArray = new JSONArray();
                for (ScoreBoard scoreboard : scoreboards) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("score", scoreboard.score);
                    jsonArray.put(jsonObject);
                }
                return jsonArray.toString();
            }

            return "[]";
        });
    }
}

package org.rolando.util;

import org.jetbrains.annotations.Nullable;
import org.rolando.data.PasswordHash;
import org.rolando.data.ScoreBoard;
import org.rolando.data.User;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgresUtil {
    private static final int MAX_SCORE_BOARD_SIZE = 100;
    private static Sql2o sql2o;
    private static int connections;
    private static int MAX_CONNECTIONS =
            (int) StringUtil.parseLong(System.getenv()
                    .getOrDefault("SQL_CONNECTIONS", "1"));
    private static String schema;
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static void init() {
        String sqlUrlEnv = System.getenv("DATABASE_URL_ENV");
        String sqlUrlEnvName = sqlUrlEnv == null || sqlUrlEnv.isEmpty() ? "DATABASE_URL" : sqlUrlEnv;
        String sqlServer = System.getenv(sqlUrlEnvName);
        if (sqlServer == null || sqlServer.isEmpty()) {
            Logger.warn("Missing env: %s", sqlUrlEnvName);
            System.exit(1);
        }
        setServer(sqlServer);
    }

    /**
     * Set the server url an initialize a new sql2o instance
     * @param serverUrl url of sql server
     */
    public static void setServer(String serverUrl) {
        sql2o = getSql2oInstance(serverUrl);
    }

    /**
     * Get new sql instance with a parsed connection url
     * Use sql2o instance instead of calling this multiple times
     * @param serverUrl connection url
     * @return new instance
     */
    private static Sql2o getSql2oInstance(String serverUrl) {
        Pattern mysqlPattern = Pattern.compile("(.*://)(.*):(.*)@(.*)"); // (scheme)(user):(pass)@(url)
        Matcher mysqlMatches = mysqlPattern.matcher(serverUrl);
        if (!mysqlMatches.find()) {
            Logger.warn("Could not parse mysql database connection string.");
            System.exit(1);
        }
        String mysqlUrl = mysqlMatches.group(1).replace("postgres", "postgresql") +
                mysqlMatches.group(4);
        if (Boolean.parseBoolean(System.getenv().getOrDefault("SQL_SSL", "true")))
            mysqlUrl += "?sslmode=require";
        String mysqlUsername = mysqlMatches.group(2);
        String mysqlPassword = mysqlMatches.group(3);
        schema = System.getenv().getOrDefault("SQL_SCHEMA", "geo_scoreboard");
        return new Sql2o(mysqlUrl, mysqlUsername, mysqlPassword, new PostgresQuirks());
    }

    /**
     * Release a connection
     * @param connection sql2o connection
     */
    private static void releaseConnection(@Nullable Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (Exception e) {
                Logger.exception(e);
            }
        }
        LOCK.lock();
        if (connections > 0)
            connections--;
        LOCK.unlock();
    }

    /**
     * Try to fetch a connection
     * @return sql2o connection
     */
    private static Connection getConnection() {
        while (connections >= MAX_CONNECTIONS)
            try {
                Thread.sleep(1);
            }
            catch (Exception e) {
                Logger.exception(e);
            }
        Connection connection = sql2o.open();
        LOCK.lock();
        connections++;
        LOCK.unlock();
        return connection;
    }

    public static boolean doesEmailExist(String email) {
        return getUser(email) != null;
    }

    @Nullable
    public static User getUser(String email) {
        String sql = "select * from %s.users where email = :email;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            List<User> users = connection.createQuery(sql, false)
                    .addParameter("email", email)
                    .addColumnMapping("uid", "uid")
                    .addColumnMapping("api_key", "apiKey")
                    .addColumnMapping("password_hash", "passwordHash")
                    .addColumnMapping("password_salt", "passwordSalt")
                    .addColumnMapping("email", "email")
                    .addColumnMapping("account_type", "accountType")
                    .executeAndFetch(User.class);
            releaseConnection(connection);
            if (users.size() == 0)
                return null;
            return users.get(0);
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
            return null;
        }
    }

    @Nullable
    public static User getUserByApiKey(String apiKey) {
        String sql = "select * from %s.users where api_key = :api_key;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            List<User> users = connection.createQuery(sql, false)
                    .addParameter("api_key", apiKey)
                    .addColumnMapping("uid", "uid")
                    .addColumnMapping("api_key", "apiKey")
                    .addColumnMapping("password_hash", "passwordHash")
                    .addColumnMapping("password_salt", "passwordSalt")
                    .addColumnMapping("email", "email")
                    .addColumnMapping("account_type", "accountType")
                    .executeAndFetch(User.class);
            releaseConnection(connection);
            if (users.size() == 0)
                return null;
            return users.get(0);
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
            return null;
        }
    }

    public static void createUser(String email, String password) {
        String sql = "insert into %s.users (api_key, password_hash, password_salt, email) " +
                "values (:api_key, :password_hash," +
                ":password_salt, :email);";
        sql = String.format(sql, schema);
        Connection connection = null;
        PasswordHash passwordHash = KeyUtil.generatePasswordHash(password);
        try {
            connection = getConnection();
            connection.createQuery(sql, false)
                    .addParameter("api_key", KeyUtil.generateApiKey())
                    .addParameter("password_hash", passwordHash.hash)
                    .addParameter("password_salt", passwordHash.salt)
                    .addParameter("email", email)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
        }
    }

    @Nullable
    public static PasswordHash getPasswordHash(String email) {
        User user = getUser(email);
        if (user != null) {
            return new PasswordHash(user.passwordHash, user.passwordSalt);
        }
        else {
            return null;
        }
    }

    @Nullable
    public static String getApiKey(String email) {
        User user = getUser(email);
        if (user != null) {
            return user.apiKey;
        }
        return null;
    }

    public static void resetApiKey(String email) {
        String sql = "update %s.users set api_key = :api_key where email = :email;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(sql, false)
                    .addParameter("api_key", KeyUtil.generateApiKey())
                    .addParameter("email", email)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
        }
        dropAllScoreboards(email);
    }

    private static void dropAllScoreboards(String email) {
        String apiKey = getApiKey(email);
        if (apiKey == null)
            return;

        String sql = "delete from %s.scoreboards where api_key = :api_key;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(sql, false)
                    .addParameter("api_key", apiKey)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
        }
    }

    public static void publishScore(String apiKey, String scoreboard, float longitude,
                                    float latitude, int score) {
        if (scoreboard == null || scoreboard.length() > 25 || scoreboard.isEmpty())
            return;

        String cityCountryName = CityUtil.getCityCountryName(longitude, latitude);
        cityCountryName = String.format("%s:%s", scoreboard, cityCountryName);
        if (apiKey.equals("demo"))
            cityCountryName = String.format("%s%s", "DEMO", cityCountryName);
        if (isValidApiKey(apiKey) &&
                getAmountOfRows(apiKey) < getMaxAllowedRows(apiKey) &&
                shouldPublish(apiKey, cityCountryName, score)) {
            String sql = "insert into %s.scoreboards (api_key, score, city_country) " +
                    "values (:api_key, :score, :city_country);";
            sql = String.format(sql, schema);
            Connection connection = null;
            try {
                connection = getConnection();
                connection.createQuery(sql, false)
                        .addParameter("api_key", apiKey)
                        .addParameter("score", score)
                        .addParameter("city_country", cityCountryName)
                        .executeUpdate();
                releaseConnection(connection);
            }
            catch (Exception e) {
                Logger.exception(e);
                releaseConnection(connection);
            }
        }
    }

    private static boolean isValidApiKey(String apiKey) {
        if (apiKey.equals("demo"))
            return true;
        User user = getUserByApiKey(apiKey);
        if (user == null)
            return false;
        return true;
    }

    private static boolean shouldPublish(String apiKey, String cityCountryName, int score) {
        List<ScoreBoard> scoreBoards = getScoreboards(apiKey, cityCountryName, 200);

        boolean isGreater = false;
        if (scoreBoards.size() == 0)
            isGreater = true;
        for (ScoreBoard scoreBoard : scoreBoards) {
            if (scoreBoard.score <= score) {
                isGreater = true;
                break;
            }
        }

        for (int index = 0; index <= scoreBoards.size() - PostgresUtil.MAX_SCORE_BOARD_SIZE; index++)
            truncateIfNecessary(apiKey, cityCountryName);

        return isGreater;
    }

    private static void truncateIfNecessary(String apiKey, String cityCountryName) {
        if (apiKey == null)
            return;

        List<ScoreBoard> scoreBoards = getScoreboards(apiKey, cityCountryName, 200);

        if (scoreBoards == null || scoreBoards.size() < MAX_SCORE_BOARD_SIZE)
            return;

        long uid = scoreBoards.get(scoreBoards.size() - 1).uid;

        String sql = "delete from %s.scoreboards where uid = :uid;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            connection.createQuery(sql, false)
                    .addParameter("uid", uid)
                    .executeUpdate();
            releaseConnection(connection);
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
        }
    }

    public static List<ScoreBoard> getScoreboards(String apiKey, String cityCountryName, int limit) {
        String sql = "select * from %s.scoreboards where api_key = :api_key and " +
                "city_country = :city_country order by score desc limit :limit;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            List<ScoreBoard> scoreBoards = connection.createQuery(sql, false)
                    .addParameter("api_key", apiKey)
                    .addParameter("city_country", cityCountryName)
                    .addParameter("limit", limit)
                    .addColumnMapping("api_key", "apiKey")
                    .addColumnMapping("score", "score")
                    .addColumnMapping("uid", "uid")
                    .addColumnMapping("city_country", "cityCountryName")
                    .executeAndFetch(ScoreBoard.class);
            releaseConnection(connection);

            return scoreBoards;
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
            return null;
        }
    }

    private static long getAmountOfRows(String apiKey) {
        if (apiKey == null)
            return Integer.MAX_VALUE;

        String sql = "select count(api_key) from %s.scoreboards where api_key = :api_key;";
        sql = String.format(sql, schema);
        Connection connection = null;
        try {
            connection = getConnection();
            Object count = connection.createQuery(sql, false)
                    .addParameter("api_key", apiKey)
                    .executeScalar();
            releaseConnection(connection);
            return (long) count;
        }
        catch (Exception e) {
            Logger.exception(e);
            releaseConnection(connection);
        }

        return Integer.MAX_VALUE;
    }

    private static long getMaxAllowedRows(String apiKey) {
        int accountType = getAccountType(apiKey);
        switch (accountType) {
            case 0:
                return 10000;
            case 1:
                return 1000000;
            default:
                return 10000;
        }
    }

    private static int getAccountType(String apiKey) {
        User user = getUserByApiKey(apiKey);
        if (user == null)
            return 0;
        return user.accountType;
    }
}

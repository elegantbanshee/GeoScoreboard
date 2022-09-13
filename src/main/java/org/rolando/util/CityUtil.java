package org.rolando.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class CityUtil {
    private static JSONArray cities = new JSONArray();

    static {
        populateCities();
    }

    private static void populateCities() {
        InputStream citiesIn = CityUtil.class.getResourceAsStream("/worldcities.csv");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(citiesIn));
        try {
            String line = bufferedReader.readLine();
            boolean firstLine = true;
            while (line != null) {
                if (firstLine) {
                    firstLine = false;
                    line = bufferedReader.readLine();
                    continue;
                }
                String[] lineSplit = line
                        .replaceAll("\"", "")
                        .replaceAll("Islamorada,", "Islamorada -")
                        .split(",");
                JSONObject city = new JSONObject();
                city.put("city", lineSplit[0]);
                city.put("city_ascii", lineSplit[1]);
                city.put("lat", lineSplit[2]);
                city.put("lng", lineSplit[3]);
                city.put("country", lineSplit[4]);
                city.put("iso2", lineSplit[5]);
                city.put("iso3", lineSplit[6]);
                city.put("admin_name", lineSplit[7]);
                city.put("capital", lineSplit[8]);
                city.put("population", lineSplit[9]);
                city.put("id", lineSplit[10]);
                cities.put(city);
                line = bufferedReader.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCityCountryName(float longitude, float latitude) {
        String cityCountryName = "";
        float distance = Float.MAX_VALUE;
        for (int index = 0; index < cities.length(); index++) {
            JSONObject city = cities.getJSONObject(index);
            float newLongitude = city.getFloat("lng");
            float newLatitude = city.getFloat("lat");
            float newDistance = (float) Math.sqrt(Math.pow(newLongitude - longitude, 2) +
                    Math.pow(newLatitude - latitude, 2));
            if (newDistance < distance) {
                cityCountryName = String.format("%s:%s", city.getString("city"),
                        city.getString("country"));
                distance = newDistance;
            }
        }
        return cityCountryName.toLowerCase(Locale.US).replaceAll(" ", "");
    }
}

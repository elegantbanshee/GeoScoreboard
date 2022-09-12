# GeoScoreboard

# Building

`./gradlew shadow`

# Env Vars

## PORT

Numerical port

## SQL_SCHEMA

Schema to use for SQL connections

Default: geo_scoreboard

## DATABASE_URL

Postgres database url string

postgres://username:password@localhost:5432/geo_scoreboard

## SQL_SSL

Boolean

Should SQL use SSL

## SQL_CONNECTIONS

Amount of connections

Default: 1
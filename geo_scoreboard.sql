--
-- PostgreSQL database dump
--

-- Dumped from database version 14.5
-- Dumped by pg_dump version 14.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: geo_scoreboard; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA geo_scoreboard;


ALTER SCHEMA geo_scoreboard OWNER TO postgres;

--
-- Name: users; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA users;


ALTER SCHEMA users OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: scoreboards; Type: TABLE; Schema: geo_scoreboard; Owner: postgres
--

CREATE TABLE geo_scoreboard.scoreboards (
    api_key character varying NOT NULL,
    score integer NOT NULL,
    uid integer NOT NULL,
    city_country character varying
);


ALTER TABLE geo_scoreboard.scoreboards OWNER TO postgres;

--
-- Name: scoreboards_uid_seq; Type: SEQUENCE; Schema: geo_scoreboard; Owner: postgres
--

CREATE SEQUENCE geo_scoreboard.scoreboards_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE geo_scoreboard.scoreboards_uid_seq OWNER TO postgres;

--
-- Name: scoreboards_uid_seq; Type: SEQUENCE OWNED BY; Schema: geo_scoreboard; Owner: postgres
--

ALTER SEQUENCE geo_scoreboard.scoreboards_uid_seq OWNED BY geo_scoreboard.scoreboards.uid;


--
-- Name: users; Type: TABLE; Schema: geo_scoreboard; Owner: postgres
--

CREATE TABLE geo_scoreboard.users (
    uid bigint NOT NULL,
    api_key character varying,
    password_hash character varying,
    password_salt character varying,
    email character varying,
    account_type integer DEFAULT 0 NOT NULL
);


ALTER TABLE geo_scoreboard.users OWNER TO postgres;

--
-- Name: users_uid_seq; Type: SEQUENCE; Schema: geo_scoreboard; Owner: postgres
--

CREATE SEQUENCE geo_scoreboard.users_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE geo_scoreboard.users_uid_seq OWNER TO postgres;

--
-- Name: users_uid_seq; Type: SEQUENCE OWNED BY; Schema: geo_scoreboard; Owner: postgres
--

ALTER SEQUENCE geo_scoreboard.users_uid_seq OWNED BY geo_scoreboard.users.uid;


--
-- Name: table_name; Type: TABLE; Schema: users; Owner: postgres
--

CREATE TABLE users.table_name (
    uid bigint NOT NULL,
    api_key character varying NOT NULL,
    password_hash character varying NOT NULL,
    password_salt character varying NOT NULL,
    email character varying NOT NULL
);


ALTER TABLE users.table_name OWNER TO postgres;

--
-- Name: table_name_uid_seq; Type: SEQUENCE; Schema: users; Owner: postgres
--

CREATE SEQUENCE users.table_name_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE users.table_name_uid_seq OWNER TO postgres;

--
-- Name: table_name_uid_seq; Type: SEQUENCE OWNED BY; Schema: users; Owner: postgres
--

ALTER SEQUENCE users.table_name_uid_seq OWNED BY users.table_name.uid;


--
-- Name: scoreboards uid; Type: DEFAULT; Schema: geo_scoreboard; Owner: postgres
--

ALTER TABLE ONLY geo_scoreboard.scoreboards ALTER COLUMN uid SET DEFAULT nextval('geo_scoreboard.scoreboards_uid_seq'::regclass);


--
-- Name: users uid; Type: DEFAULT; Schema: geo_scoreboard; Owner: postgres
--

ALTER TABLE ONLY geo_scoreboard.users ALTER COLUMN uid SET DEFAULT nextval('geo_scoreboard.users_uid_seq'::regclass);


--
-- Name: table_name uid; Type: DEFAULT; Schema: users; Owner: postgres
--

ALTER TABLE ONLY users.table_name ALTER COLUMN uid SET DEFAULT nextval('users.table_name_uid_seq'::regclass);


--
-- Data for Name: scoreboards; Type: TABLE DATA; Schema: geo_scoreboard; Owner: postgres
--

COPY geo_scoreboard.scoreboards (api_key, score, uid, city_country) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: geo_scoreboard; Owner: postgres
--

COPY geo_scoreboard.users (uid, api_key, password_hash, password_salt, email, account_type) FROM stdin;
\.


--
-- Data for Name: table_name; Type: TABLE DATA; Schema: users; Owner: postgres
--

COPY users.table_name (uid, api_key, password_hash, password_salt, email) FROM stdin;
\.


--
-- Name: scoreboards_uid_seq; Type: SEQUENCE SET; Schema: geo_scoreboard; Owner: postgres
--

SELECT pg_catalog.setval('geo_scoreboard.scoreboards_uid_seq', 113, true);


--
-- Name: users_uid_seq; Type: SEQUENCE SET; Schema: geo_scoreboard; Owner: postgres
--

SELECT pg_catalog.setval('geo_scoreboard.users_uid_seq', 7, true);


--
-- Name: table_name_uid_seq; Type: SEQUENCE SET; Schema: users; Owner: postgres
--

SELECT pg_catalog.setval('users.table_name_uid_seq', 1, false);


--
-- Name: scoreboards scoreboards_uid_key; Type: CONSTRAINT; Schema: geo_scoreboard; Owner: postgres
--

ALTER TABLE ONLY geo_scoreboard.scoreboards
    ADD CONSTRAINT scoreboards_uid_key UNIQUE (uid);


--
-- Name: table_name table_name_api_key_key; Type: CONSTRAINT; Schema: users; Owner: postgres
--

ALTER TABLE ONLY users.table_name
    ADD CONSTRAINT table_name_api_key_key UNIQUE (api_key);


--
-- Name: table_name table_name_email_key; Type: CONSTRAINT; Schema: users; Owner: postgres
--

ALTER TABLE ONLY users.table_name
    ADD CONSTRAINT table_name_email_key UNIQUE (email);


--
-- Name: table_name table_name_pkey; Type: CONSTRAINT; Schema: users; Owner: postgres
--

ALTER TABLE ONLY users.table_name
    ADD CONSTRAINT table_name_pkey PRIMARY KEY (uid);


--
-- PostgreSQL database dump complete
--


CREATE DATABASE hackers
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       LC_COLLATE = 'English, United States'
       LC_CTYPE = 'English, United States'
       CONNECTION LIMIT = -1;



-- Table: person

-- DROP TABLE person;

CREATE TABLE person
(
  id serial NOT NULL,
  "name" character varying NOT NULL,
  email character varying,
  "comment" character varying,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT person_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE person OWNER TO postgres;


-- Table: "key"

-- DROP TABLE "key";

CREATE TABLE "key"
(
  id serial NOT NULL,
  "value" character varying NOT NULL,
  revocated boolean NOT NULL DEFAULT false,
  person integer NOT NULL,
  "comment" character varying,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT key_pkey PRIMARY KEY (id),
  CONSTRAINT key_person_fkey FOREIGN KEY (person)
      REFERENCES person (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT key_value_key UNIQUE (value)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "key" OWNER TO postgres;


-- Table: hacker

-- DROP TABLE hacker;

CREATE TABLE hacker
(
  id serial NOT NULL,
  "name" character varying,
  gateway integer,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT hacker_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE hacker OWNER TO postgres;

-- Index: idx_hacker_gateway

-- DROP INDEX idx_hacker_gateway;

CREATE INDEX idx_hacker_gateway
  ON hacker
  USING btree
  (gateway);

-- Index: idx_hacker_name

-- DROP INDEX idx_hacker_name;

CREATE INDEX idx_hacker_name
  ON hacker
  USING btree
  (name);


-- Table: report

-- DROP TABLE report;

CREATE TABLE report
(
  id serial NOT NULL,
  hacker integer,
  game_engine integer,
  map_name character varying,
  agent_version character varying,
  "key" integer,
  ip character varying,
  "version" timestamp without time zone DEFAULT now(),
  "comment" character varying,
  CONSTRAINT report_pkey PRIMARY KEY (id),
  CONSTRAINT report_hacker_fkey FOREIGN KEY (hacker)
      REFERENCES hacker (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT report_key_fkey FOREIGN KEY ("key")
      REFERENCES "key" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE report OWNER TO postgres;

-- Index: idx_report_version

-- DROP INDEX idx_report_version;

CREATE INDEX idx_report_version
  ON report
  USING btree
  (version);



-- Table: download_log

-- DROP TABLE download_log;

CREATE TABLE download_log
(
  id serial NOT NULL,
  ip character varying,
  success boolean,
  exec_time_ms integer,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT download_log_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE download_log OWNER TO postgres;


-- Table: aka_group

-- DROP TABLE aka_group;

CREATE TABLE aka_group
(
  id serial NOT NULL,
  "comment" character varying,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT aka_group_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE aka_group OWNER TO postgres;


-- Table: game

-- DROP TABLE game;

CREATE TABLE game
(
  id serial NOT NULL,
  engine integer,
  frames integer,
  save_time timestamp without time zone,
  "name" character varying,
  map_width integer,
  map_height integer,
  speed integer,
  "type" integer,
  sub_type integer,
  creator_name character varying,
  map_name character varying,
  replay_md5 character varying,
  agent_version character varying,
  gateway integer,
  ip character varying,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT game_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE game OWNER TO postgres;

-- Index: idx_game__replay_md5

-- DROP INDEX idx_game__replay_md5;

CREATE INDEX idx_game__replay_md5
  ON game
  USING btree
  (replay_md5);




-- Table: player

-- DROP TABLE player;

CREATE TABLE player
(
  id serial NOT NULL,
  "name" character varying,
  aka_group integer,
  games_count integer DEFAULT 0,
  first_game timestamp without time zone,
  last_game timestamp without time zone,
  total_frames integer DEFAULT 0,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT player_pkey PRIMARY KEY (id),
  CONSTRAINT player_aka_group_fkey FOREIGN KEY (aka_group)
      REFERENCES aka_group (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE player OWNER TO postgres;

-- Index: idx_player__id_name

-- DROP INDEX idx_player__id_name;

CREATE INDEX idx_player__id_name
  ON player
  USING btree
  (id, name);

-- Index: idx_player__name

-- DROP INDEX idx_player__name;

CREATE INDEX idx_player__name
  ON player
  USING btree
  (name);



-- Table: game_player

-- DROP TABLE game_player;

CREATE TABLE game_player
(
  id serial NOT NULL,
  game integer,
  player integer,
  race integer,
  actions_count integer,
  color integer,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT game_player_pkey PRIMARY KEY (id),
  CONSTRAINT game_player_game_fkey FOREIGN KEY (game)
      REFERENCES game (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT game_player_player_fkey FOREIGN KEY (player)
      REFERENCES player (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE game_player OWNER TO postgres;

-- Index: idx_game_player__game_player

-- DROP INDEX idx_game_player__game_player;

CREATE INDEX idx_game_player__game_player
  ON game_player
  USING btree
  (game, player);



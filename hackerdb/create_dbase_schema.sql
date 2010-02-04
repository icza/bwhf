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
  password character varying,
  page_access integer,
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

-- Index: idx_key__value_revocated

-- DROP INDEX idx_key__value_revocated;

CREATE INDEX idx_key__value_revocated
  ON "key"
  USING btree
  (value, revocated);


-- Table: hacker

-- DROP TABLE hacker;

CREATE TABLE hacker
(
  id serial NOT NULL,
  "name" character varying,
  gateway integer,
  guarded boolean NOT NULL DEFAULT false,
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

-- Index: idx_hacker_guarded

-- DROP INDEX idx_hacker_guarded;

CREATE INDEX idx_hacker_guarded
  ON hacker
  USING btree
  (guarded);


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
  replay_md5 character varying,
  save_time timestamp without time zone,
  revocated boolean NOT NULL DEFAULT false,
  changed_by integer,
  used_hacks character varying,
  CONSTRAINT report_pkey PRIMARY KEY (id),
  CONSTRAINT report_hacker_fkey FOREIGN KEY (hacker)
      REFERENCES hacker (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT report_key_fkey FOREIGN KEY ("key")
      REFERENCES "key" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT report_person_fkey FOREIGN KEY (changed_by)
      REFERENCES person (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE report OWNER TO postgres;

-- Index: idx_report__replay_md5

-- DROP INDEX idx_report__replay_md5;

CREATE INDEX idx_report__replay_md5
  ON report
  USING btree
  (replay_md5);

-- Index: idx_report_version

-- DROP INDEX idx_report_version;

CREATE INDEX idx_report_version
  ON report
  USING btree
  (version);

-- Index: idx_report_revocated

-- DROP INDEX idx_report_revocated;

CREATE INDEX idx_report_revocated
  ON report
  USING btree
  (revocated);


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
  CONSTRAINT aka_group_pkey PRIMARY KEY (id),
  CONSTRAINT aka_group_comment_key UNIQUE (comment)
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

-- Index: idx_game_version

-- DROP INDEX idx_game_version;

CREATE INDEX idx_game_version
  ON game
  USING btree
  (version);



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
  total_frames bigint DEFAULT 0,
  "version" timestamp without time zone DEFAULT now(),
  is_computer boolean NOT NULL DEFAULT false,
  CONSTRAINT player_pkey PRIMARY KEY (id),
  CONSTRAINT player_aka_group_fkey FOREIGN KEY (aka_group)
      REFERENCES aka_group (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE player OWNER TO postgres;

-- Index: idx_player__aka_group

-- DROP INDEX idx_player__aka_group;

CREATE INDEX idx_player__aka_group
  ON player
  USING btree
  (aka_group);

-- Index: idx_player__first_game

-- DROP INDEX idx_player__first_game;

CREATE INDEX idx_player__first_game
  ON player
  USING btree
  (first_game);

-- Index: idx_player__games_count

-- DROP INDEX idx_player__games_count;

CREATE INDEX idx_player__games_count
  ON player
  USING btree
  (games_count);

-- Index: idx_player__id_name

-- DROP INDEX idx_player__id_name;

CREATE INDEX idx_player__id_name
  ON player
  USING btree
  (id, name);

-- Index: idx_player__is_computer

-- DROP INDEX idx_player__is_computer;

CREATE INDEX idx_player__is_computer
  ON player
  USING btree
  (is_computer);

-- Index: idx_player__last_game

-- DROP INDEX idx_player__last_game;

CREATE INDEX idx_player__last_game
  ON player
  USING btree
  (last_game);

-- Index: idx_player__name

-- DROP INDEX idx_player__name;

CREATE INDEX idx_player__name
  ON player
  USING btree
  (name);

-- Index: idx_player__total_frames

-- DROP INDEX idx_player__total_frames;

CREATE INDEX idx_player__total_frames
  ON player
  USING btree
  (total_frames);




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

-- Index: idx_game_player__player

-- DROP INDEX idx_game_player__player;

CREATE INDEX idx_game_player__player
  ON game_player
  USING btree
  (player);

-- Index: idx_game_player__game

-- DROP INDEX idx_game_player__game;

CREATE INDEX idx_game_player__game
  ON game_player
  USING btree
  (game);


-- Table: login_log

-- DROP TABLE login_log;

CREATE TABLE login_log
(
  id serial NOT NULL,
  ip character varying,
  "name" character varying,
  success boolean,
  "version" timestamp without time zone DEFAULT now(),
  CONSTRAINT login_log_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE login_log OWNER TO postgres;


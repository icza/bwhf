CREATE DATABASE hackers
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       LC_COLLATE = 'English, United States'
       LC_CTYPE = 'English, United States'
       CONNECTION LIMIT = -1;




----------------------------------------
---- Tables of the hacker registry -----
----------------------------------------

DROP TABLE IF EXISTS download_log;
DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS hacker;
DROP TABLE IF EXISTS key;
DROP TABLE IF EXISTS person;


-- Key owners
CREATE TABLE person (
    id      SERIAL PRIMARY KEY,
    name    VARCHAR NOT NULL,
    email   VARCHAR,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW()
);


-- Keys
CREATE TABLE key (
    id        SERIAL PRIMARY KEY,
    value     VARCHAR NOT NULL,
    revocated BOOLEAN DEFAULT FALSE NOT NULL,
    person    INT NOT NULL,
    comment   VARCHAR,
    version   TIMESTAMP DEFAULT NOW(),
    UNIQUE (value),
    FOREIGN KEY (person) REFERENCES person(id) ON DELETE CASCADE
);


-- Hackers
CREATE TABLE hacker (
    id      SERIAL PRIMARY KEY,
    name    VARCHAR,
    gateway INT,
    version TIMESTAMP DEFAULT NOW()
);


-- Hacker reports
CREATE TABLE report (
    id            SERIAL PRIMARY KEY,
    hacker        INT,
    game_engine   INT,
    map_name      VARCHAR,
    agent_version VARCHAR,
    key           INT,
    ip            VARCHAR,               --IP of the reporter's computer.
    version       TIMESTAMP DEFAULT NOW(),
    comment       VARCHAR,
    FOREIGN KEY (key) REFERENCES key(id) ON DELETE CASCADE,
    FOREIGN KEY (hacker) REFERENCES hacker(id) ON DELETE CASCADE
);


-- Hacker list download log
CREATE TABLE download_log (
    id           SERIAL PRIMARY KEY,
    ip           VARCHAR,                --IP of the downloader's computer.
    success      BOOLEAN,
    exec_time_ms INT,                    --Execution time in ms.
    version      TIMESTAMP DEFAULT NOW()
);


DROP INDEX IF EXISTS idx_hacker_name;
DROP INDEX IF EXISTS idx_hacker_gateway;
DROP INDEX IF EXISTS idx_report_version;

CREATE INDEX idx_hacker_name ON hacker (name);
CREATE INDEX idx_hacker_gateway ON hacker (gateway);
CREATE INDEX idx_report_version ON report (version);


-----------------------------------------
---- Tables of the Players' Network -----
-----------------------------------------

DROP TABLE IF EXISTS game_player;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS aka_group;
DROP TABLE IF EXISTS game;

-- Games
CREATE TABLE game (
    id            SERIAL PRIMARY KEY,
    engine        INT,
    frames        INT,
    save_time     TIMESTAMP,
    name          VARCHAR,
    map_width     INT,
    map_height    INT,
    speed         INT,
    type          INT,
    sub_type      INT,
    creator_name  VARCHAR,
    map_name      VARCHAR,
    replay_md5    VARCHAR,
    agent_version VARCHAR,
    gateway       INT,                   --optional
    ip            VARCHAR,               --IP of the reporter's computer.
    version       TIMESTAMP DEFAULT NOW()
);


-- Groups of players' akas
CREATE TABLE aka_group (
    id      SERIAL PRIMARY KEY,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW()
);


-- Players of games
CREATE TABLE player (
    id           SERIAL PRIMARY KEY,
    name         VARCHAR,
    aka_group    INT,
    games_count  INT DEFAULT 0, -- redundant data to speed up player listing
    first_game   TIMESTAMP,     -- time of first game, redundant data to speed up player listing
    last_game    TIMESTAMP,     -- time of last game, redundant data to speed up player listing
    total_frames INT DEFAULT 0, -- total frames in games, redundant data to speed up player listing
    version      TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (aka_group) REFERENCES aka_group(id) ON DELETE CASCADE
);


-- Connections between players and games
CREATE TABLE game_player (
    id            SERIAL PRIMARY KEY,
    game          INT,
    player        INT,
    race          INT,
    actions_count INT,
    color         INT,
    version       TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (game) REFERENCES game(id) ON DELETE CASCADE,
    FOREIGN KEY (player) REFERENCES player(id) ON DELETE CASCADE
);


DROP INDEX IF EXISTS idx_player__name;
DROP INDEX IF EXISTS idx_player__id_name;
DROP INDEX IF EXISTS idx_game_player__game_player;
CREATE INDEX idx_player__name ON player (name);
CREATE INDEX idx_player__id_name ON player (id,name);
CREATE INDEX idx_game_player__game_player ON game_player (game,player);


----------------------------------------
---- Tables of the hacker registry -----
----------------------------------------

DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS hacker;
DROP TABLE IF EXISTS key;
DROP TABLE IF EXISTS person;


-- Key owners
CREATE CACHED TABLE person (
    id      IDENTITY,
    name    VARCHAR NOT NULL,
    email   VARCHAR,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW
);


-- Keys
CREATE CACHED TABLE key (
    id        IDENTITY,
    value     VARCHAR NOT NULL,
    revocated BOOLEAN DEFAULT FALSE NOT NULL,
    person    INT NOT NULL,
    comment   VARCHAR,
    version   TIMESTAMP DEFAULT NOW,
	UNIQUE (value),
    FOREIGN KEY (person) REFERENCES person(id) ON DELETE CASCADE
);


-- Hackers
CREATE CACHED TABLE hacker (
    id      IDENTITY,
    name    VARCHAR,
    gateway INT,
    version TIMESTAMP DEFAULT NOW
);


-- Hacker reports
CREATE CACHED TABLE report (
    id            IDENTITY,
    hacker        INT,
    game_engine   INT,
    map_name      VARCHAR,
    agent_version VARCHAR,
    key           INT,
    ip            VARCHAR,               --IP of the reporter's computer.
    version       TIMESTAMP DEFAULT NOW,
    comment       VARCHAR,
    FOREIGN KEY (key) REFERENCES key(id) ON DELETE CASCADE,
    FOREIGN KEY (hacker) REFERENCES hacker(id) ON DELETE CASCADE
);


-- Hacker list download log
CREATE CACHED TABLE download_log (
    id           IDENTITY,
    ip           VARCHAR,                --IP of the downloader's computer.
    success      BOOLEAN,
    exec_time_ms INT,                    --Execution time in ms.
    version      TIMESTAMP DEFAULT NOW
);


DROP INDEX idx_hacker_name IF EXISTS;
DROP INDEX idx_hacker_gateway IF EXISTS;
DROP INDEX idx_report_version IF EXISTS;

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
CREATE CACHED TABLE game (
    id            IDENTITY,
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
    version       TIMESTAMP DEFAULT NOW
);


-- Groups of players' akas
CREATE CACHED TABLE aka_group (
    id      IDENTITY,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW
);


-- Players of games
CREATE CACHED TABLE player (
    id        IDENTITY,
    name      VARCHAR,
    aka_group INT,
    version   TIMESTAMP DEFAULT NOW,
    FOREIGN KEY (aka_group) REFERENCES aka_group(id) ON DELETE CASCADE
);


-- Connections between players and games
CREATE CACHED TABLE game_player (
    id            IDENTITY,
    game          INT,
    player        INT,
    race          INT,
    actions_count INT,
    color         INT,
    version       TIMESTAMP DEFAULT NOW,
    FOREIGN KEY (game) REFERENCES game(id) ON DELETE CASCADE,
    FOREIGN KEY (player) REFERENCES player(id) ON DELETE CASCADE
);


DROP INDEX idx_player_name IF EXISTS;
DROP INDEX idx_game_frames IF EXISTS;
DROP INDEX idx_game_map IF EXISTS;
DROP INDEX idx_game_type IF EXISTS;
DROP INDEX idx_game_save_time IF EXISTS;

CREATE INDEX idx_player_name ON player (name);
CREATE INDEX idx_game_frames ON game (frames);
CREATE INDEX idx_game_map ON game (map_name);
CREATE INDEX idx_game_type ON game (type);
CREATE INDEX idx_game_save_time ON game (save_time);
--
DROP INDEX idx_game_player_game_player IF EXISTS;
CREATE INDEX idx_game_player_game_player ON game_player (game,player);
CREATE INDEX idx_player_id_name ON player (id,name);

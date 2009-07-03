
----------------------------------------
---- Tables of the hacker registry -----
----------------------------------------

DROP TABLE download_log IF EXISTS;
DROP TABLE report IF EXISTS;
DROP TABLE hacker IF EXISTS;
DROP TABLE key IF EXISTS;
DROP TABLE person IF EXISTS;


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

DROP TABLE game_player IF EXISTS;
DROP TABLE player IF EXISTS;
DROP TABLE aka_group IF EXISTS;
DROP TABLE game IF EXISTS;

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
    id           IDENTITY,
    name         VARCHAR,
    aka_group    INT,
    games_count  INT DEFAULT 0, -- redundant data to speed up player listing
    first_game   TIMESTAMP,     -- time of first game, redundant data to speed up player listing
    last_game    TIMESTAMP,     -- time of last game, redundant data to speed up player listing
    total_frames INT DEFAULT 0, -- total frames in games, redundant data to speed up player listing
    version      TIMESTAMP DEFAULT NOW,
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


DROP INDEX idx_player__name IF EXISTS;
DROP INDEX idx_player__id_name IF EXISTS;
DROP INDEX idx_game_player__game_player IF EXISTS;
CREATE INDEX idx_player__name ON player (name);
CREATE INDEX idx_player__id_name ON player (id,name);
CREATE INDEX idx_game_player__game_player ON game_player (game,player);

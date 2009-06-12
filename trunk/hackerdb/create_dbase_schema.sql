
----------------------------------------
---- Tables of the hacker registry -----
----------------------------------------

DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS hacker;
DROP TABLE IF EXISTS key;
DROP TABLE IF EXISTS person;


CREATE CACHED TABLE person (
    id      IDENTITY,
    name    VARCHAR NOT NULL,
    email   VARCHAR,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW
);


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


CREATE CACHED TABLE hacker (
    id      IDENTITY,
    name    VARCHAR,
    gateway INT,
    version TIMESTAMP DEFAULT NOW
);


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


CREATE CACHED TABLE download_log (
    id           IDENTITY,
    ip           VARCHAR,                --IP of the downloader's computer.
    success      BOOLEAN,
    exec_time_ms INT,                    --Execution time in ms.
    version      TIMESTAMP DEFAULT NOW
);


-----------------------------------------
---- Tables of the Players' Network -----
-----------------------------------------

DROP TABLE IF EXISTS player_aka;
DROP TABLE IF EXISTS game_player;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS game;

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
    ip            VARCHAR,               --IP of the reporter's computer.
    version       TIMESTAMP DEFAULT NOW
);


CREATE CACHED TABLE player (
    id      IDENTITY,
    name    VARCHAR,
    version TIMESTAMP DEFAULT NOW
);


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


CREATE CACHED TABLE player_aka (
    id         IDENTITY,
    player     INT,
    player_aka INT,
    version    TIMESTAMP DEFAULT NOW,
    FOREIGN KEY (player) REFERENCES player(id) ON DELETE CASCADE,
    FOREIGN KEY (player_aka) REFERENCES player(id) ON DELETE CASCADE
);


DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS hacker;
DROP TABLE IF EXISTS key;
DROP TABLE IF EXISTS person;


CREATE CACHED TABLE person (
    id IDENTITY,
    name VARCHAR NOT NULL,
    email VARCHAR,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW
);


CREATE CACHED TABLE key (
    id IDENTITY,
    value VARCHAR NOT NULL,
    revocated BOOLEAN DEFAULT FALSE NOT NULL,
    person INT NOT NULL,
    comment VARCHAR,
    version TIMESTAMP DEFAULT NOW,
    FOREIGN KEY (person) REFERENCES person(id) ON DELETE CASCADE
);


CREATE CACHED TABLE hacker (
    id IDENTITY,
    name VARCHAR,
    gateway INT,
    version TIMESTAMP DEFAULT NOW
);


CREATE CACHED TABLE report (
    id IDENTITY,
    hacker INT,
    key INT,
    ip VARCHAR,                 --IP of the reporter's computer.
    version TIMESTAMP DEFAULT NOW,
    FOREIGN KEY (key) REFERENCES key(id) ON DELETE CASCADE,
    FOREIGN KEY (hacker) REFERENCES hacker(id) ON DELETE CASCADE
);


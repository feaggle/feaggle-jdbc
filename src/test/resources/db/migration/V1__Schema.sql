CREATE TABLE RELEASES(
    ID VARCHAR(256) PRIMARY KEY,
    STATUS BIT
);

CREATE TABLE EXPERIMENTS(
    ID VARCHAR(256) PRIMARY KEY,
    STATUS BIT
);

CREATE TABLE SEGMENTS(
    ID VARCHAR(256),
    KIND VARCHAR(256) NOT NULL,
    ROLLOUT INT,
    PREMIUM BIT
);

CREATE INDEX SEGMENTS_BY_EXPERIMENT ON SEGMENTS (ID);
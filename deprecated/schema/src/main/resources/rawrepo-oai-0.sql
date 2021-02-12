CREATE TABLE version (
       version NUMERIC(6) NOT NULL PRIMARY KEY,
       warning TEXT DEFAULT NULL
);
-- Compatible versions
INSERT INTO version VALUES (0);


\set ON_ERROR_STOP
CREATE TABLE version (
    version NUMERIC(6) NOT NULL PRIMARY KEY,
    warning TEXT DEFAULT NULL
);
-- Compatible versions
INSERT INTO version VALUES (1);


CREATE TABLE oaisets (
    setSpec VARCHAR(64) NOT NULL,
    setName VARCHAR(64) NOT NULL,
    description TEXT,
    CONSTRAINT oaisets_pk PRIMARY KEY (setSpec)
);

INSERT INTO oaisets (setSpec, setName, description) VALUES ('bkm', 'BiblioteksKatalogiseret Materiale', 'Noget on hvad det er');
INSERT INTO oaisets (setSpec, setName, description) VALUES ('nat', 'National Bibliografi', 'Noget mere om hvad det er');

CREATE TABLE oairecords (
    pid VARCHAR(128) NOT NULL,
    changed TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT timeofday()::timestamp,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT oairecords_pk PRIMARY KEY (pid)
);
CREATE INDEX oairecords_time ON oairecords(changed);

CREATE TABLE oairecordsets (
    pid VARCHAR(128) NOT NULL,
    setSpec VARCHAR(64) NOT NULL,
    CONSTRAINT oairecordsets_pk PRIMARY KEY (pid, setSpec),
    CONSTRAINT oairecordsets_pid_fk FOREIGN KEY (pid) REFERENCES oairecords (pid),
    CONSTRAINT oairecordsets_setspec_fk FOREIGN KEY (setSpec) REFERENCES oaisets (setSpec)
);


CREATE TABLE oaiformats (
    prefix VARCHAR(64) NOT NULL,
    schema TEXT NOT NULL,
    namespace TEXT NOT NULL,
    CONSTRAINT oaiformats_pk PRIMARY KEY (prefix)
);

INSERT INTO oaiformats (prefix, schema, namespace) VALUES('oai_dc', 'http://www.openarchives.org/OAI/2.0/oai_dc.xsd', 'http://www.openarchives.org/OAI/2.0/oai_dc/');
INSERT INTO oaiformats (prefix, schema, namespace) VALUES('marcx', 'https://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd', 'info:lc/xmlns/marcxchange-v1');
 
CREATE TABLE keyvalue (
    key VARCHAR(64) NOT NULL,
    value TEXT NOT NULL,
    CONSTRAINT keyvalue_pk PRIMARY KEY (key)
);

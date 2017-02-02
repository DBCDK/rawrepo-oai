--
-- ENSURE ONLY UPGRADING PREVIOUS VERSION
--
\set ON_ERROR_STOP

BEGIN TRANSACTION;

DO
$$
DECLARE
  currentversion INTEGER = 1;
  brokenversion INTEGER = 0;
  oldversion INTEGER;
BEGIN
  SELECT MAX(version) INTO oldversion FROM version;
  IF (oldversion <> (currentversion-1)) THEN
    RAISE EXCEPTION 'Expected schema version % found %', (currentversion-1), oldversion;
  END IF;
  INSERT INTO version VALUES(currentversion);
  DELETE FROM version WHERE version <= brokenversion;
END
$$;


--
--
--

CREATE TABLE oaisets (
    setSpec VARCHAR(64) NOT NULL,
    setName VARCHAR(64) NOT NULL,
    description TEXT,
    CONSTRAINT oaisets_pk PRIMARY KEY (setSpec)
);


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


--
--
--
COMMIT;

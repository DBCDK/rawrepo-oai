--
-- ENSURE ONLY UPGRADING PREVIOUS VERSION
--
\set ON_ERROR_STOP

BEGIN TRANSACTION;

DO
$$
DECLARE
  currentversion INTEGER = 2;
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

UPDATE  oaisets SET setName = 'Bibliotekskatalogiserede Materialer', description = 'Betalingsprodukt - kræver adgangskode.' WHERE setSpec = 'bkm';
UPDATE  oaisets SET setName = 'Nationalbibliografi', description = 'Materialer udgivet i Danmark. Offentligt tilgængeligt.' WHERE setSpec = 'nat';
INSERT INTO oaisets (setSpec, setName, description) VALUES ('art', 'Artikler', 'Artikler fra Artikelbasen - Offentligt tilgængeligt.');
INSERT INTO oaisets (setSpec, setName, description) VALUES ('onl', 'Onlinematerialer', 'Betalingsprodukt - kræver adgangskode');

--
--
--
COMMIT;

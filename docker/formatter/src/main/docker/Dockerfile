FROM docker.dbc.dk/dbc-dropwizard
MAINTAINER os-team

ENV KAFKA_LOG_APPNAME=rawrepo-oai-formatter \
    FETCH_RECORDS_TIMEOUT=30
ADD res/rawrepo-oai-formatter-jar/ $DROPWIZARD_HOME

LABEL RAWREPO_RECORD_SERVICE_URL="Rawrepo record service  url. (required). Format: http://..." \
      POOL_SIZE="Size of JavaScript worker pool, and rawrepo jdbc pool. Default is 2 (optional)" \
      FETCH_RECORDS_TIMEOUT="Fetch record (from record-service) timeout in seconds (default 30)"

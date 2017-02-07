FROM docker.dbc.dk/dbc-dropwizard
MAINTAINER os-team

ENV KAFKA_LOG_APPNAME rawrepo-oai-formatter
ADD res/rawrepo-oai-formatter-jar/ $DROPWIZARD_HOME

LABEL RAWREPO_URL="Rawrepo jdbc url. (required). Format: user:pass@host:port/base" \
      POOL_SIZE="Size of JavaScript worker pool, and rawrepo jdbc pool. Default is 2 (optional)"


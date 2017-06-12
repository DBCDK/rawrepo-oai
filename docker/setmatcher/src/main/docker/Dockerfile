FROM docker.dbc.dk/dbc-dropwizard
MAINTAINER os-team

ENV KAFKA_LOG_APPNAME rawrepo-oai-setmatcher
ADD res/rawrepo-oai-setmatcher-jar/ $DROPWIZARD_HOME

LABEL RAWREPO_URL="Rawrepo jdbc url. (required). Format: user:pass@host:port/base" \
      RAWREPO_OAI_URL="Rawrepo OAI jdbc url. (required). Format: user:pass@host:port/base" \
      WORKER="Name of rawrepo queue worker (required)" \
      COMMIT_INTERVAL="Number of jobs to process between commits (optional). Default is 50." \
      POLL_INTERVAL_MS="The interval in ms to poll the rawrepo queue (optional). Default is 5000" \
      POOL_SIZE="Size of job processor pool. Default is 2 (optional)"


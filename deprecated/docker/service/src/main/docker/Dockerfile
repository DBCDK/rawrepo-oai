FROM docker.dbc.dk/dbc-dropwizard
MAINTAINER os-team

ENV KAFKA_LOG_APPNAME rawrepo-oai-service-dw
ADD res/rawrepo-oai-service-jar/ $DROPWIZARD_HOME

LABEL RAWREPO_OAI_URL="Rawrepo-oai jdbc url. (required). Format: user:pass@host:port/base" \
      REPOSITORY_NAME="The name of the OAI service (required)" \
      BASE_URL="External url for this service (required)" \
      RECORDS_PR_REQUEST="Number of Records to deliver in one request (default: 10)" \
      IDENTIFIERS_PR_REQUEST="Number if Identifiers to deliver in one request (default: 50)" \
      TOKEN_MAX_AGE="How long a resumption token should live (default: 8 {hours})" \
      FETCH_RECORD_TIMEOUT="How long to wait for fetching records (default: 30 {sec})" \
      NO_THROTTLE="Disable throttling (default: false)" \
      NO_AUTHENTICATION="Disable authentication (default: false)" \
      X_FORWARDED_FOR="ipv4 nets to use X-Forwarded-For header from (default 127.0.0.1 172.16.0.0/12)" \
      FORS_RIGHTS_URL="Url to openagency (required)" \
      FORS_RIGHTS_RULES="Rules for access control [name,rule=set[,set]* ]*  (default: netpunkt.dk,500=bkm,onl)" \
      FORMAT_SERVICE="url to format service: http://.../..?id=%(id)&format=%(format)&sets=%(sets) (required)"


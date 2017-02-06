FROM docker.dbc.dk/dbc-payara
MAINTAINER os-team

ENV KAFKA_LOG_APPNAME rawrepo-oai-formatter

ADD pre/* $PAYARA_CFG/pre/
ADD post/* $PAYARA_CFG/post/
ADD res/rawrepo-oai-formatter.war $PAYARA_CFG/pre/
RUN payara-setup $PAYARA_CFG/pre/

LABEL RAWREPO_URL="Rawrepo jdbc url. (required). Ex.: 'user:pass@host:1234/dbname'" \

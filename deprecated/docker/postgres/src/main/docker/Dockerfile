FROM docker.dbc.dk/dbc-postgres:9.5
MAINTAINER os-team

ENV POSTGRES_DB=rawrepooai
ENV POSTGRES_USER=rawrepooai
ENV POSTGRES_PASSWORD=rawrepooai

USER root
ADD res/rawrepo-oai-schema-jar/rawrepo-oai.sql /docker-entrypoint.d/10-rawrepo-oai.sql
RUN chown -R $POSTGRES_LINUX_USER:$POSTGRES_LINUX_USER /docker-entrypoint.d/
USER $POSTGRES_LINUX_USER

LABEL POSTGRES_DB="Name of db (optional). Default is 'rawrepooai'" \
      POSTGRES_USER="(optional). Default is 'rawrepooai'" \
      POSTGRES_PASSWORD="(optional). Default is 'rawrepooai'"


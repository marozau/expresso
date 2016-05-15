FROM java:8-jre

ENV APP_DIR "/opt/esearch-rss"
ENV LOG_DIR "/var/log/esearch-rss"

COPY ${PWD}/targer/esearch-rss ${APP_DIR}

WORKDIR ${APP_DIR}
RUN chmod +x rss-fetcher.sh &&\
    mkdir ${LOG_DIR} &&\
    chmod 600 /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/management/jmxremote.password

VOLUME ["${LOG_DIR}"]

CMD ["./rss-fetcher.sh"]

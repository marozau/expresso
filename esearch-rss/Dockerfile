FROM java:8-jre

ENV APP_DIR "/opt/esearch-rss"

COPY ${PWD}/target/esearch-rss ${APP_DIR}

WORKDIR ${APP_DIR}
RUN chmod 600 /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/management/jmxremote.password

CMD ["java", "-cp", "esearch-rss-1.0-SNAPSHOT.jar", "today.expresso.esearch.rss.RssFetcher"]
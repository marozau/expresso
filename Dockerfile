FROM openjdk:8-jre
MAINTAINER im.expresso.today@gmail.com

RUN apt-get -q update && apt-get -y -q install iproute2 iputils-ping less wget

ENV VERSION 1.0
ENV APP_DIR /opt/expresso-${VERSION}
ENV LOG_DIR ${APP_DIR}/logs

ENV PORT 9000

COPY ./target/universal/expresso-${VERSION}.zip /opt/expresso.zip
RUN unzip /opt/expresso.zip -d /opt && rm /opt/expresso.zip

WORKDIR ${APP_DIR}

EXPOSE ${PORT}
VOLUME ${LOG_DIR}

ENTRYPOINT ["./bin/expresso"]


# build stage
FROM maven:3-jdk-11 as builder
RUN mkdir -p /usr/src/app
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean package -DskipTests=true


#Create Image Stage:
FROM openjdk:11-jre-slim

VOLUME /tmp


ARG PIPELINE_NAME
ARG GROUP_NAME
ARG OUTGOING_TOPIC_NAME
ARG ERROR_TOPIC_NAME
ARG KAFKA_BROKERS
ARG POLL_INTERVAL_MILLIS
ARG S3_BUCKET_NAME
ARG S3_ACCESS_KEY
ARG S3_SECRET
ARG SQS_URL

ENV PIPELINE_NAME ${PROFILE_NAME}
ENV GROUP_NAME ${GROUP_NAME}
ENV OUTGOING_TOPIC_NAME ${OUTGOING_TOPIC_NAME}
ENV ERROR_TOPIC_NAME ${ERROR_TOPIC_NAME}
ENV KAFKA_BROKERS ${KAFKA_BROKERS}
ENV POLL_INTERVAL_MILLIS ${POLL_INTERVAL_MILLIS}
ENV S3_BUCKET_NAME ${S3_BUCKET_NAME}
ENV S3_ACCESS_KEY ${S3_ACCESS_KEY}
ENV S3_SECRET ${S3_SECRET}
ENV SQS_URL ${SQS_URL}

COPY --from=builder  /usr/src/app/target/*with-dependencies.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "./app.jar"]
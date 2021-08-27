FROM gradle:7.1-jdk16 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME
COPY build.gradle settings.gradle $APP_HOME

COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src

RUN gradle shadowJar || return 0
COPY . .

FROM openjdk:16-jdk-slim
ENV ARTIFACT_NAME=EasyPoll-3.0-all.jar
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

ENTRYPOINT exec java -jar ${ARTIFACT_NAME}

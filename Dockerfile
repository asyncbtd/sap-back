# syntax=docker/dockerfile:1.19
FROM eclipse-temurin:25-alpine AS build

COPY ./mvnw ./mvnw
COPY ./.mvn ./.mvn
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw

COPY ./pom.xml ./pom.xml
COPY ./src/ ./src/

RUN --mount=type=cache,target=/root/.m2/repository \
    ./mvnw package -DskipTests

RUN java -Djarmode=tools -jar ./target/sap-back.jar extract --destination /extract

FROM bellsoft/liberica-openjdk-alpine-musl:25
WORKDIR /opt/sap
ENV TZ=Europe/Minsk

ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:InitialRAMPercentage=25.0 \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/urandom"

COPY --from=build /extract/lib/ ./lib/
COPY --from=build /extract/sap-back.jar ./sap-back.jar

ENTRYPOINT [ "java", "-jar", "sap-back.jar" ]

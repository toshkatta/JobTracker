# syntax=docker/dockerfile:1

# ---- Stage 1: build ----
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /build

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

# The cache mount keeps downloaded Maven dependencies between builds
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B --no-transfer-progress -DskipTests package \
    && mv target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---- Stage 2: runtime ----
FROM eclipse-temurin:25-jre-noble
WORKDIR /application

RUN useradd --system --no-create-home appuser

# Least-changing layers first, so Docker reuses them between builds
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "application.jar"]

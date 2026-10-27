FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q package -DskipTests

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 spring
COPY --from=build /build/target/twitterclone-*.jar /app/app.jar
USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn compile -q
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.ynov.solid.SOLIDDemo", "-q"]

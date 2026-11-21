
# Étape 1 : compiler CAISSEG avec Maven et Java 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY . .

RUN mvn -B clean package -DskipTests

# Étape 2 : installer l'application dans WildFly 41
FROM quay.io/wildfly/wildfly:41.0.1.Final-jdk21

COPY --from=build /build/target/CAISSEG-1.0-SNAPSHOT.war \
    /opt/jboss/wildfly/standalone/deployments/CAISSEG-1.0-SNAPSHOT.war

EXPOSE 10000

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-Djboss.http.port=10000"]

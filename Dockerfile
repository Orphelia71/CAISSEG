# Étape 1 : compilation de CAISSEG
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY . .

RUN mvn -B clean package -DskipTests

# Étape 2 : serveur WildFly
FROM quay.io/wildfly/wildfly:41.0.1.Final-jdk21

# Options transmises directement à Java
ENV JAVA_OPTS="-Xms32m -Xmx160m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=32m -Xss512k -XX:+UseSerialGC"

# Installation du pilote PostgreSQL
COPY docker/postgresql/ /opt/jboss/wildfly/modules/system/layers/base/com/postgresql/main/

# Installation de la configuration WildFly
COPY docker/standalone-full-render.xml /opt/jboss/wildfly/standalone/configuration/standalone-full-render.xml

# Déploiement de CAISSEG
COPY --from=build /build/target/CAISSEG-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/CAISSEG-1.0-SNAPSHOT.war

EXPOSE 10000

# Démarrage de WildFly
CMD ["/bin/sh", "-c", "exec /opt/jboss/wildfly/bin/standalone.sh --server-config=standalone-full-render.xml -b 0.0.0.0 -Djboss.http.port=${PORT:-10000}"]

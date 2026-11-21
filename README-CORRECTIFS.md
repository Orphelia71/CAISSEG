# CAISSEG - correctifs JasperReports et securite WildFly

## 1. JasperReports / recus PDF

Le service de generation est `src/main/java/reports/ReceiptService.java`.
Le modele du recu est `src/main/resources/reports/recu.jrxml`.
Les proprietes JasperReports sont dans `src/main/resources/jasperreports.properties`.

L'URL du recu est :

`http://localhost:8080/CAISSEG-1.0-SNAPSHOT/reports/recu?id=1`

Remplace `1` par un identifiant de vente existant dans PostgreSQL.
Le modele JRXML est compile une fois et reutilise ensuite.

## 2. JAAS / Elytron sur WildFly 41

Le projet declare deja l'authentification FORM et les roles dans
`src/main/webapp/WEB-INF/web.xml`. Le fichier `jboss-web.xml` associe
l'application au domaine Undertow `caisseg`.

Il faut aussi creer le realm JDBC Elytron sur le serveur. Le script
`wildfly-caisseg-elytron.cli` effectue cette configuration.

Avant de l'executer :
1. Demarrer WildFly 41 avec la configuration qui contient le datasource PostgreSQL.
2. Verifier que le datasource porte le nom `GestionCaisseDS` et que son JNDI est
   `java:/jdbc/GestionCaisseDS`.
3. Verifier les colonnes `utilisateurs.login`, `utilisateurs.mot_de_passe`,
   `utilisateurs.role` et `utilisateurs.actif`.
4. Ouvrir un terminal dans `WILDFLY_HOME/bin` et lancer :

   `jboss-cli.bat --connect --file="CHEMIN_COMPLET\wildfly-caisseg-elytron.cli"`

5. Redemarrer/recharger WildFly si le script ne l'a pas deja fait.
6. Reconstruire et redeployer le WAR.

Le formulaire `login.xhtml` envoie les champs `j_username` et `j_password`
vers `j_security_check`, comme attendu par l'authentification FORM.

## 3. Format des mots de passe

Le script Elytron utilise `clear-password-mapper` car les mots de passe de
la base ont ete indiques comme etant stockes en clair. `PasswordUtil` ne
transforme donc plus les mots de passe lors de la creation ou modification.

ATTENTION : le stockage en clair est moins securise. Ce choix est fait pour
rester compatible avec les donnees actuelles. Les anciens comptes dont le
mot de passe a deja ete transforme en SHA-256 doivent etre remis au format
attendu (mot de passe en clair) avant de pouvoir se connecter avec ce realm.

## 4. Roles attendus

Les valeurs de la colonne `role` doivent correspondre exactement aux roles
declares dans `web.xml` :
- `RESPONSABLE`
- `CAISSIER`

Les comptes dont `actif` vaut `FALSE` sont exclus de la requete du realm.

## 5. Compilation

Dans le dossier du projet contenant `pom.xml` :

`mvn clean package`

Le WAR genere se trouve dans `target/CAISSEG-1.0-SNAPSHOT.war`.

Si la compilation/deploiement echoue, conserver le premier message `ERROR`
de Maven ou la premiere exception `Caused by` de WildFly pour identifier la
cause exacte.

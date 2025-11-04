# Guide Green Code

Ce document propose des actions concrètes pour réduire l’empreinte énergétique du projet.

Objectifs:
- Minimiser les recours aux CPU, mémoire, E/S réseau et disque.
- Adapter la capacité au besoin (éviter les surprovisions).
- Favoriser la sobriété dans le code et l’architecture.

---

## 1) Actions transverses - à appliquer rapidement

- Désactiver DevTools en prod: retirer la dépendance `spring-boot-devtools` des profils.
- Activer l’initialisation paresseuse Spring (lazy): `spring.main.lazy-initialization=true` en prod pour réduire le coût la mémoire.
- Logging sobre par défaut: niveau `INFO`.
- Pagination systématique côté API et UI: pas de retours de listes sans pagination.
- DTO légers: ne charger que les champs nécessaires.
- Compression HTTP: activer gzip/br (taille > 1 Ko) pour payloads JSON.
- Timeouts et retries raisonnables: éviter les boucles d’attente et retries agressifs (Feign).
- Dimensionner les conteneurs: limites CPU/mémoire dans `docker-compose.yml`, 1 réplica par défaut, scaler selon la charge en prod.

---

## 2) JVM  – réglages éco

Avec Java 21, en prod conteneurisée, ajuster :
- `-XX:MaxRAMPercentage=60` (ou moins) pour limiter le heap (mémoire native).
- `-XX:InitialRAMPercentage=20` pour un compromis démarrage/mémoire.
- `-XX:+UseStringDeduplication` utile pour le JSON.

---

## 3) Spring Boot – configurations sobres

- Lazy init (prod) :
```
spring.main.lazy-initialization=true
```

- Logging :
```
logging.level.root=INFO
logging.file.name=logs/app.log
logging.logback.rollingpolicy.max-history=7
logging.logback.rollingpolicy.total-size-cap=200MB
```

- Actuator: exposer le strict nécessaire :
```
management.endpoints.web.exposure.include=health,info,prometheus
management.tracing.sampling.probability=0.01
```

- Compression HTTP (Spring Web):
```
server.compression.enabled=true
server.compression.min-response-size=1024
server.compression.mime-types=application/json,application/xml,text/plain,text/html
```

---

## 4) Gateway (WebFlux) – sobriété réseau

- Timeouts raisonnables (WebClient):
```
spring.codec.max-in-memory-size=1MB
spring.cloud.gateway.httpclient.connect-timeout=2000
spring.cloud.gateway.httpclient.response-timeout=5s
```

- Connection pooling:
```
spring.cloud.gateway.httpclient.pool.max-connections=100
spring.cloud.gateway.httpclient.pool.acquire-timeout=500ms
```

- Limiter les filtres coûteux, activer rate limiting simple si pertinent, ...

---

## 5) Eureka – réduction de l’empreinte

- Allonger les intervalles pour limiter le trafic de heartbeat (instances actives) :
```
eureka.instance.leaseRenewalIntervalInSeconds=30
```

---

## 6) Services REST

- JPA (auth/patient):
  - HikariCP:
    ```
    spring.datasource.hikari.minimum-idle=2 # nombre minimal de connexions ouvertes
    spring.datasource.hikari.maximum-pool-size=10 # limite le nombre maximal de connexions simultanées
    spring.jpa.open-in-view=false # désactive le contexte de persistence ouvert pendant la vue
    spring.jpa.properties.hibernate.default_batch_fetch_size=50
    spring.jpa.properties.hibernate.jdbc.batch_size=50 # optimisent les accès en base en groupant les lectures et écritures
    ```
  - DTO, `@Query` ciblées, index DB sur colonnes de recherche, pas de `fetch = EAGER` par défaut.
  - Pagination obligatoire sur toutes les listes.
- Feign :
  - Timeouts/compression:
    ```
    feign.client.config.default.connectTimeout=2000
    feign.client.config.default.readTimeout=5000
    feign.compression.request.enabled=true
    feign.compression.response.enabled=true
    ```

---

## 7) MongoDB (Note Service)

- Renvoyer uniquement les champs utiles.
- Index composés sur champs de recherche, TTL si données périssables.
- Limiter la taille de page, pas de `findAll()` non filtré.

---

## 8) Docker et docker-compose

- Passer aux builds multi-étapes pour des images plus petites :

Dockerfile type (exemple):
```Dockerfile
# build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests package

# runtime léger via jre
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=20 -XX:MaxRAMPercentage=60 -XX:+UseStringDeduplication"
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

- Ressources dans `docker-compose.yml` (éviter surprovisionner):
```yaml
deploy:
  resources:
    limits:
      cpus: "0.5"
      memory: 512M
    reservations:
      cpus: "0.1"
      memory: 128M
```
- Réplicas: scaler en prod selon la charge, pas par défaut.
- Healthchecks légers pour éviter redémarrages en boucle coûteux.

---

## 9) Frontend (Thymeleaf + Rollup.js)

- Thymeleaf: mettre en cache les fragments statiques.
- Rollup :
  - Minification du code Javascript et CSS.
  - Éviter bibliothèques lourdes si possible.
  - CSS: netoyer des classes non utilisées, regrouper media queries.
- UX sobre: pas de polling fréquent et debouncer/throttler côté client.
- Assets: formats modernes (WebP), dimensions adaptées, cache HTTP long si besoin.

---

## 10) CI/CD et builds

- Maven: activer cache (`~/.m2`), builds multi-modules ciblés (only-changed), éviter tests d’intégration coûteux sur chaque commit.
- Tests: paralléliser prudemment (`-T 1C`), coverage à seuil raisonnable, tests e2e sur planification (nightly) plutôt que à chaque push.
- Node/rollup: cache `node_modules` et cache Rollup; exécuter tests unités seulement sur changements du dossier `ui/`.

---

## 11) Pistes de refactoring Green (backlog)

1. Pagination stricte de toutes les endpoints listant des entités.
2. Ajouter des DTO JPA et Mongo pour éviter la surcharge d’objets et de réseaux.
3. Supprimer/conditionner `spring-boot-devtools` aux profils dev uniquement; exclure du repackage prod.
4. Configurer HikariCP et pools HTTP avec tailles minimales et timeouts courts.
5. Activer `spring.main.lazy-initialization=true` en prod, et exclure les auto-configs inutiles.
6. Introduire caches de lecture (Caffeine) sur points chauds (lecture patients/notes fréquemment demandées) avec TTL court.
7. Docker multi-étapes + layers Spring + limites CPU/Mem dans compose.
8. Frontend: code-splitting, nettoyage CSS, éviter polling, compresser assets.


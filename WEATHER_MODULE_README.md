# Module Météo - Documentation

Ce document décrit le module météo implémenté avec Spring Boot (backend) et Angular (frontend), utilisant Redis pour le cache avec TTL dynamique selon la saison.

## 🎯 Fonctionnalités

### Backend (Spring Boot)

1. **Entité Weather**
   - `city` : Nom de la ville
   - `temp` : Température en degrés Celsius
   - `humidity` : Taux d'humidité en pourcentage
   - `timestamp` : Date et heure de la dernière mise à jour

2. **WeatherService**
   - Récupération des données météo depuis l'API OpenWeatherMap
   - Gestion complète des erreurs (404, 401, erreurs réseau, etc.)
   - Utilisation de `@Cacheable` pour la lecture avec cache
   - Utilisation de `@CachePut` pour forcer la mise à jour du cache

3. **WeatherController REST**
   - `GET /api/weather/{city}` : Récupère la météo (utilise le cache)
   - `POST /api/weather/refresh/{city}` : Force la mise à jour du cache

4. **Cache Redis avec TTL dynamique**
   - **Été** (juin, juillet, août) : TTL de **5 minutes**
   - **Hiver** (décembre, janvier, février) : TTL de **30 minutes**
   - **Printemps/Automne** : TTL de **15 minutes**

5. **Logging des hits/misses**
   - Tous les accès au cache sont loggés automatiquement
   - Logs détaillés pour les hits, misses, puts et evictions

6. **Documentation Swagger**
   - Documentation complète de l'API disponible sur `/swagger-ui.html`
   - Descriptions détaillées de chaque endpoint

### Frontend (Angular)

1. **WeatherService**
   - Service Angular utilisant `HttpClient` pour appeler les endpoints REST
   - Méthodes `getWeather(city)` et `refreshWeather(city)`

2. **WeatherComponent**
   - Formulaire de recherche de ville
   - Affichage des données météo (température, humidité, timestamp)
   - Bouton pour forcer le rafraîchissement du cache
   - Gestion des erreurs avec messages utilisateur clairs
   - Interface utilisateur moderne et responsive

## 🚀 Configuration

### Backend

1. **Mode Démo (par défaut)**
   
   Le module fonctionne en **mode démo par défaut**, générant des données météo fictives pour permettre les tests sans clé API. Les données sont cohérentes (même ville = mêmes données) grâce à un système de hash.

   **Aucune configuration nécessaire** - le module fonctionne immédiatement en mode démo !

2. **Configuration de l'API météo réelle** (`application.yml`)
   ```yaml
   weather:
     api:
       url: https://api.openweathermap.org/data/2.5/weather
       key: ${WEATHER_API_KEY:demo}
       demo-mode: false  # Désactiver le mode démo
   ```

   Pour utiliser une vraie clé API OpenWeatherMap :
   - Créez un compte gratuit sur [OpenWeatherMap](https://openweathermap.org/api)
   - Obtenez votre clé API (gratuite jusqu'à 1000 appels/jour)
   - **Option 1** : Définissez la variable d'environnement :
     ```bash
     export WEATHER_API_KEY=votre_cle_api
     ```
   - **Option 2** : Modifiez `application.yml` :
     ```yaml
     weather:
       api:
         key: votre_cle_api
         demo-mode: false
     ```
   - Redémarrez l'application

2. **Redis**
   - Assurez-vous que Redis est démarré sur `localhost:6379`
   - La configuration Redis est dans `RedisConfig.java` et `SeasonalCacheConfig.java`

### Frontend

- L'URL de l'API backend est configurée dans `weather.service.ts` : `http://localhost:8081/api/weather`
- Assurez-vous que le backend est démarré avant de lancer le frontend

## 📋 Utilisation

### Backend

1. Démarrer Redis :
   ```bash
   redis-server
   ```

2. Démarrer le backend Spring Boot :
   ```bash
   cd CacheFlow/backend
   ./mvnw spring-boot:run
   ```

3. Accéder à la documentation Swagger :
   ```
   http://localhost:8081/swagger-ui.html
   ```

### Frontend

1. Installer les dépendances (si nécessaire) :
   ```bash
   cd CacheFlow/frontend
   npm install
   ```

2. Démarrer le serveur de développement :
   ```bash
   npm start
   ```

3. Accéder à l'application :
   ```
   http://localhost:4200
   ```

4. Naviguer vers le module météo via le menu ou directement :
   ```
   http://localhost:4200/weather
   ```

## 🔍 Tests des Endpoints

### GET /api/weather/{city}
```bash
curl http://localhost:8081/api/weather/Paris
```

### POST /api/weather/refresh/{city}
```bash
curl -X POST http://localhost:8081/api/weather/refresh/Paris
```

## 📊 Logs du Cache

Les logs du cache sont visibles dans la console du backend. Exemples :

```
Cache MISS - Cache: weather, Key: paris
Cache PUT - Cache: weather, Key: paris, TTL: 5 minutes
Cache HIT - Cache: weather, Key: paris
Cache REFRESH - Forcing update for city: Paris
```

## 🏗️ Architecture

### Backend Structure
```
backend/src/main/java/com/jee/backend/
├── model/
│   └── Weather.java                    # Entité météo
├── dto/
│   └── OpenWeatherResponse.java        # DTO pour la réponse API externe
├── service/
│   └── WeatherService.java             # Service météo avec cache
├── controller/
│   └── WeatherController.java          # Controller REST
└── config/
    ├── SeasonalCacheConfig.java        # Configuration cache avec TTL saisonnier
    └── RestTemplateConfig.java         # Configuration RestTemplate
```

### Frontend Structure
```
frontend/src/app/
├── model/
│   └── weather.ts                      # Interface Weather
├── service/
│   └── weather.service.ts              # Service Angular pour appels API
└── components/
    └── weather/
        ├── weather.component.ts         # Composant principal
        ├── weather.component.html       # Template HTML
        └── weather.component.css       # Styles CSS
```

## 🐛 Gestion des Erreurs

Le module gère plusieurs types d'erreurs :

- **Ville non trouvée** (404) : Message clair pour l'utilisateur
- **Erreur d'authentification API** (401) : Vérification de la clé API
- **Erreurs réseau** : Gestion des timeouts et erreurs de connexion
- **Erreurs serveur** (500) : Messages d'erreur génériques avec détails

## 📝 Notes

- **Mode Démo** : Par défaut, le module génère des données fictives pour les tests. Aucune clé API n'est nécessaire.
- **Fallback automatique** : Si l'API réelle échoue (erreur d'authentification, réseau, etc.), le système bascule automatiquement en mode démo.
- Le cache utilise la sérialisation JDK pour compatibilité avec Spring Boot 4.0
- Les clés de cache sont en minuscules pour éviter les doublons (Paris = paris)
- Le TTL est recalculé à chaque démarrage de l'application selon la saison actuelle
- Les données météo sont mises en cache automatiquement lors de la première requête
- En mode démo, les données sont générées de manière déterministe (même ville = mêmes valeurs)

## 🔐 Sécurité

- La clé API météo peut être configurée via variable d'environnement
- CORS est configuré pour permettre les requêtes depuis le frontend
- Les erreurs ne révèlent pas d'informations sensibles

## 📚 Ressources

- [OpenWeatherMap API Documentation](https://openweathermap.org/api)
- [Spring Cache Documentation](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Redis Documentation](https://redis.io/documentation)
- [Angular HttpClient](https://angular.io/api/common/http/HttpClient)


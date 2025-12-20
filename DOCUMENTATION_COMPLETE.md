# 📚 Documentation Complète - Module Météo

## Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture du système](#architecture-du-système)
3. [Fonctionnement détaillé](#fonctionnement-détaillé)
4. [Système de cache Redis](#système-de-cache-redis)
5. [Flux de données](#flux-de-données)
6. [Configuration](#configuration)
7. [Guide d'utilisation](#guide-dutilisation)
8. [Dépannage](#dépannage)

---

## 🎯 Vue d'ensemble

Le module météo est une application Spring Boot + Angular qui permet de récupérer et afficher les données météorologiques pour n'importe quelle ville. Il utilise Redis comme système de cache avec un TTL (Time To Live) dynamique qui s'adapte selon la saison.

### Fonctionnalités principales

- ✅ Récupération de données météo depuis une API externe (OpenWeatherMap)
- ✅ Cache Redis avec TTL dynamique selon la saison
- ✅ Mode démo intégré (données fictives pour les tests)
- ✅ Logging des hits/misses du cache
- ✅ Interface Angular moderne et responsive
- ✅ Documentation API Swagger complète

---

## 🏗️ Architecture du système

### Schéma général

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Frontend      │         │    Backend       │         │   External API  │
│   Angular       │────────▶│  Spring Boot     │────────▶│  OpenWeatherMap │
│                 │  HTTP   │                  │  HTTP   │                 │
└─────────────────┘         └────────┬─────────┘         └─────────────────┘
                                      │
                                      │ Cache
                                      ▼
                              ┌───────────────┐
                              │     Redis     │
                              │    Cache      │
                              └───────────────┘
```

### Structure des composants

#### Backend (Spring Boot)

```
com.jee.backend/
├── model/
│   └── Weather.java              # Entité météo (city, temp, humidity, timestamp)
├── dto/
│   └── OpenWeatherResponse.java  # DTO pour mapper la réponse API externe
├── service/
│   └── WeatherService.java       # Logique métier + cache
├── controller/
│   └── WeatherController.java    # Endpoints REST
└── config/
    ├── SeasonalCacheConfig.java  # Configuration cache avec TTL saisonnier
    ├── RestTemplateConfig.java   # Configuration RestTemplate pour appels HTTP
    └── RedisConfig.java          # Configuration Redis de base
```

#### Frontend (Angular)

```
src/app/
├── model/
│   └── weather.ts                # Interface TypeScript Weather
├── service/
│   └── weather.service.ts        # Service Angular pour appels API
└── components/
    └── weather/
        ├── weather.component.ts   # Composant principal
        ├── weather.component.html # Template HTML
        └── weather.component.css  # Styles CSS
```

---

## ⚙️ Fonctionnement détaillé

### 1. Récupération de la météo (GET /api/weather/{city})

#### Étape par étape :

1. **Requête utilisateur** → L'utilisateur saisit une ville dans le formulaire Angular
2. **Appel HTTP** → Le frontend envoie `GET /api/weather/Paris`
3. **Controller** → `WeatherController.getWeather()` reçoit la requête
4. **Service avec cache** → `WeatherService.getWeather()` est appelé avec `@Cacheable`
5. **Vérification cache** → Spring vérifie si les données sont en cache Redis
   - **Si CACHE HIT** : Retourne directement les données du cache (rapide ⚡)
   - **Si CACHE MISS** : Continue vers l'étape 6
6. **Appel API externe** → `fetchWeatherFromApi()` est exécuté
   - Mode démo : Génère des données fictives
   - Mode réel : Appelle OpenWeatherMap API
7. **Mise en cache** → Les données sont stockées dans Redis avec TTL
8. **Réponse** → Les données sont retournées au frontend
9. **Affichage** → Le composant Angular affiche les données

#### Code correspondant :

```java
@Cacheable(value = "weather", key = "#city.toLowerCase()", unless = "#result == null")
public Weather getWeather(String city) {
    logger.info("Cache MISS - Fetching weather for city: {}", city);
    return fetchWeatherFromApi(city);
}
```

**Explication** :
- `@Cacheable` : Annotation Spring qui active le cache automatique
- `value = "weather"` : Nom du cache Redis utilisé
- `key = "#city.toLowerCase()"` : Clé de cache = nom de ville en minuscules
- `unless = "#result == null"` : Ne pas mettre en cache si résultat null

### 2. Rafraîchissement du cache (POST /api/weather/refresh/{city})

#### Étape par étape :

1. **Requête utilisateur** → L'utilisateur clique sur "Rafraîchir le cache"
2. **Appel HTTP** → Le frontend envoie `POST /api/weather/refresh/Paris`
3. **Controller** → `WeatherController.refreshWeather()` reçoit la requête
4. **Service avec cache** → `WeatherService.refreshWeather()` est appelé avec `@CachePut`
5. **Force la mise à jour** → Ignore le cache et appelle toujours l'API
6. **Appel API externe** → Récupère les nouvelles données
7. **Mise à jour cache** → Remplace les anciennes données dans Redis
8. **Réponse** → Retourne les nouvelles données

#### Code correspondant :

```java
@CachePut(value = "weather", key = "#city.toLowerCase()")
public Weather refreshWeather(String city) {
    logger.info("Cache REFRESH - Forcing update for city: {}", city);
    return fetchWeatherFromApi(city);
}
```

**Explication** :
- `@CachePut` : Force la mise à jour du cache avec les nouvelles données
- Même clé que `@Cacheable` pour remplacer les données existantes

---

## 💾 Système de cache Redis

### TTL dynamique par saison

Le système calcule automatiquement le TTL (durée de vie) selon la saison actuelle :

| Saison | Mois | TTL | Raison |
|--------|------|-----|--------|
| **Été** | Juin, Juillet, Août | **5 minutes** | Météo change rapidement en été |
| **Hiver** | Décembre, Janvier, Février | **30 minutes** | Météo plus stable en hiver |
| **Printemps/Automne** | Autres mois | **15 minutes** | Valeur intermédiaire |

### Comment ça fonctionne ?

#### 1. Calcul de la saison

```java
private Duration getSeasonalTtl() {
    LocalDate now = LocalDate.now();
    Month currentMonth = now.getMonth();

    if (currentMonth == Month.JUNE || currentMonth == Month.JULY || 
        currentMonth == Month.AUGUST) {
        return Duration.ofMinutes(5);  // Été
    } else if (currentMonth == Month.DECEMBER || 
               currentMonth == Month.JANUARY || 
               currentMonth == Month.FEBRUARY) {
        return Duration.ofMinutes(30); // Hiver
    } else {
        return Duration.ofMinutes(15);  // Printemps/Automne
    }
}
```

#### 2. Configuration du cache

```java
RedisCacheConfiguration weatherConfig = RedisCacheConfiguration.defaultCacheConfig()
    .entryTtl(weatherTtl)  // TTL calculé selon la saison
    .serializeKeysWith(...)
    .serializeValuesWith(...)
    .disableCachingNullValues();
```

#### 3. Application au cache météo

```java
return RedisCacheManager.builder(cacheWriter)
    .cacheDefaults(defaultConfig)           // Pour autres caches (products)
    .withCacheConfiguration("weather", weatherConfig)  // Cache météo avec TTL saisonnier
    .build();
```

### Logging des hits/misses

Le système log automatiquement tous les accès au cache :

#### LoggingRedisCacheWriter personnalisé

```java
@Override
public byte[] get(String name, byte[] key) {
    byte[] value = delegate.get(name, key);
    if ("weather".equals(name)) {
        if (value != null) {
            logger.info("Cache HIT - Cache: {}, Key: {}", name, new String(key));
        } else {
            logger.info("Cache MISS - Cache: {}, Key: {}", name, new String(key));
        }
    }
    return value;
}
```

#### Exemples de logs

```
INFO  - Cache MISS - Cache: weather, Key: paris
INFO  - DEMO MODE - Generating mock weather data for city: Paris
INFO  - Generated mock weather for Paris: temp=18.5°C, humidity=65.0%
INFO  - Cache PUT - Cache: weather, Key: paris, TTL: 5 minutes

INFO  - Cache HIT - Cache: weather, Key: paris
```

---

## 🔄 Flux de données

### Scénario 1 : Première requête (Cache MISS)

```
Utilisateur → Frontend → Backend → Cache Redis (vide) → API externe/Mode démo
                                                              ↓
                                                         Données météo
                                                              ↓
Utilisateur ← Frontend ← Backend ← Cache Redis (stocke) ←────┘
```

**Durée** : ~500ms (appel API) + ~10ms (cache) = ~510ms

### Scénario 2 : Requête suivante (Cache HIT)

```
Utilisateur → Frontend → Backend → Cache Redis (données présentes)
                                                              ↓
Utilisateur ← Frontend ← Backend ←───────────────────────────┘
```

**Durée** : ~10ms (cache uniquement) ⚡

### Scénario 3 : Cache expiré (TTL dépassé)

```
Utilisateur → Frontend → Backend → Cache Redis (expiré) → API externe/Mode démo
                                                              ↓
                                                         Nouvelles données
                                                              ↓
Utilisateur ← Frontend ← Backend ← Cache Redis (mise à jour) ←┘
```

**Durée** : ~510ms (comme scénario 1)

### Scénario 4 : Rafraîchissement manuel

```
Utilisateur (bouton refresh) → Frontend → Backend → Ignore cache → API externe/Mode démo
                                                                      ↓
                                                                 Nouvelles données
                                                                      ↓
Utilisateur ← Frontend ← Backend ← Cache Redis (remplace) ←──────────┘
```

**Durée** : ~510ms (force la mise à jour)

---

## ⚙️ Configuration

### Backend (application.yml)

```yaml
# Configuration Redis
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
  cache:
    type: redis
    cache-names:
      - weather  # Cache météo

# Configuration API météo
weather:
  api:
    url: https://api.openweathermap.org/data/2.5/weather
    key: ${WEATHER_API_KEY:demo}      # Variable d'environnement ou 'demo'
    demo-mode: true                    # Mode démo activé par défaut
```

### Variables d'environnement

```bash
# Pour utiliser l'API réelle OpenWeatherMap
export WEATHER_API_KEY=votre_cle_api_ici

# Pour désactiver le mode démo
export WEATHER_DEMO_MODE=false
```

### Frontend (weather.service.ts)

```typescript
private apiUrl = 'http://localhost:8081/api/weather';
```

---

## 📖 Guide d'utilisation

### 1. Démarrer Redis

```bash
# Windows
redis-server

# Linux/Mac
redis-server /usr/local/etc/redis.conf
```

### 2. Démarrer le backend

```bash
cd CacheFlow/backend
./mvnw spring-boot:run
```

**Vérification** : http://localhost:8081/swagger-ui.html

### 3. Démarrer le frontend

```bash
cd CacheFlow/frontend
npm install  # Première fois uniquement
npm start
```

**Accès** : http://localhost:4200/weather

### 4. Utilisation

1. **Rechercher une ville** :
   - Saisissez le nom d'une ville (ex: "Paris")
   - Cliquez sur "Rechercher" ou appuyez sur Entrée
   - Les données météo s'affichent

2. **Rafraîchir le cache** :
   - Après avoir recherché une ville
   - Cliquez sur "🔄 Rafraîchir le cache"
   - Les données sont mises à jour depuis l'API

3. **Tester le cache** :
   - Recherchez "Paris" → Cache MISS (première fois)
   - Recherchez "Paris" à nouveau → Cache HIT (données du cache)
   - Attendez 5-30 minutes selon la saison → Cache expiré
   - Recherchez "Paris" → Cache MISS (nouvelle récupération)

---

## 🔍 Dépannage

### Problème : Erreur "Weather API authentication failed"

**Solution** : Le mode démo est activé par défaut. Le système bascule automatiquement en mode démo si l'API échoue.

**Pour utiliser l'API réelle** :
1. Créez un compte sur https://openweathermap.org/api
2. Obtenez votre clé API gratuite
3. Configurez-la (voir section Configuration)

### Problème : Redis ne démarre pas

**Vérifications** :
```bash
# Vérifier si Redis est installé
redis-cli ping
# Devrait répondre : PONG

# Vérifier le port
netstat -an | grep 6379
```

**Solution** : Installez Redis depuis https://redis.io/download

### Problème : Le frontend ne se connecte pas au backend

**Vérifications** :
1. Backend démarré sur http://localhost:8081
2. CORS configuré (déjà fait dans `CorsConfig.java`)
3. Pas de firewall bloquant le port 8081

**Test** :
```bash
curl http://localhost:8081/api/weather/Paris
```

### Problème : Le cache ne fonctionne pas

**Vérifications** :
1. Redis est démarré
2. Les logs montrent "Cache HIT" ou "Cache MISS"
3. Vérifier les logs du backend pour les erreurs Redis

**Test du cache** :
```bash
# Connexion à Redis
redis-cli

# Voir les clés du cache
KEYS weather:*

# Voir une valeur
GET weather:paris
```

### Problème : TTL ne change pas selon la saison

**Explication** : Le TTL est calculé au démarrage de l'application. Il faut redémarrer le backend pour que le nouveau TTL soit appliqué.

**Vérification** : Regardez les logs au démarrage :
```
INFO - Saison actuelle: été - TTL configuré: 5 minutes
```

---

## 📊 Exemples de logs

### Cache MISS (première requête)

```
INFO  - GET /api/weather/Paris - Request received
INFO  - Cache MISS - Cache: weather, Key: paris
INFO  - DEMO MODE - Generating mock weather data for city: Paris
INFO  - Generated mock weather for Paris: temp=18.5°C, humidity=65.0%
INFO  - Cache PUT - Cache: weather, Key: paris, TTL: 5 minutes
INFO  - GET /api/weather/Paris - Success: temp=18.5°C, humidity=65.0%
```

### Cache HIT (données en cache)

```
INFO  - GET /api/weather/Paris - Request received
INFO  - Cache HIT - Cache: weather, Key: paris
INFO  - GET /api/weather/Paris - Success: temp=18.5°C, humidity=65.0%
```

### Rafraîchissement du cache

```
INFO  - POST /api/weather/refresh/Paris - Refresh request received
INFO  - Cache REFRESH - Forcing update for city: Paris
INFO  - DEMO MODE - Generating mock weather data for city: Paris
INFO  - Generated mock weather for Paris: temp=19.2°C, humidity=63.0%
INFO  - Cache PUT - Cache: weather, Key: paris, TTL: 5 minutes
INFO  - POST /api/weather/refresh/Paris - Cache refreshed: temp=19.2°C, humidity=63.0%
```

---

## 🎓 Concepts clés expliqués

### @Cacheable vs @CachePut

| Annotation | Comportement | Quand l'utiliser |
|------------|-------------|------------------|
| `@Cacheable` | Vérifie le cache d'abord. Si présent → retourne. Si absent → exécute la méthode et met en cache. | Pour les lectures (GET) |
| `@CachePut` | Ignore le cache. Exécute toujours la méthode et met à jour le cache avec le résultat. | Pour les mises à jour (POST refresh) |

### Sérialisation Redis

Le système utilise `JdkSerializationRedisSerializer` pour stocker les objets Java dans Redis. Cela signifie que :
- Les objets doivent implémenter `Serializable`
- Les données sont stockées en format binaire
- Compatible avec Spring Boot 4.0

### Clés de cache

Format : `weather:paris` (cache_name:key)

- Le nom de la ville est converti en minuscules pour éviter les doublons
- "Paris" et "paris" utilisent la même clé de cache
- Chaque ville a sa propre entrée dans le cache

---

## 📚 Ressources supplémentaires

- [Spring Cache Documentation](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Redis Documentation](https://redis.io/documentation)
- [OpenWeatherMap API](https://openweathermap.org/api)
- [Angular HttpClient Guide](https://angular.io/guide/http)

---

## ✅ Checklist de vérification

Avant de démarrer, vérifiez :

- [ ] Redis est installé et démarré
- [ ] Backend Spring Boot démarre sans erreur
- [ ] Frontend Angular démarre sans erreur
- [ ] Les logs montrent "Saison actuelle" au démarrage
- [ ] Le cache fonctionne (voir logs "Cache HIT/MISS")
- [ ] L'interface Angular s'affiche correctement
- [ ] Les données météo s'affichent (mode démo ou réel)

---

**Documentation créée le** : $(date)
**Version** : 1.0.0
**Auteur** : Module Météo Team


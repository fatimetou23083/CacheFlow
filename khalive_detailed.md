# PROJET CACHEFLOW - DOCUMENTATION TECHNIQUE
> **Fichiers livrables demandé par : khalive**

## 1. Vue d'ensemble
CacheFlow est une application Full-Stack (Angular + Spring Boot) démontrant la puissance du caching distribué avec Redis. Le projet est divisé en 6 modules interconnectés, chacun illustrant une stratégie de cache spécifique.

---

## 2. Infrastructure & Technologies
| Composant | Technologie | Rôle |
|-----------|-------------|------|
| **Backend** | Spring Boot 3.x | API REST, Logique métier |
| **Frontend** | Angular 16+ | Interface Utilisateur, Dashboard |
| **Base de données** | MongoDB | Stockage persistant (Produits, Utilisateurs) |
| **Cache / Broker** | Redis | Caching distribué, Pub/Sub, Sessions |
| **Conteneurisation** | Docker | Orchestration des services (Redis, Mongo) |

---

## 3. Détail des Modules (Implémentation P1-P6)

### 📍 P1: Module PRODUITS (Cache Simple)
*Stratégie : Cache-Aside pour lecture fréquente / écriture rare.*
- **Entité Implémentée** : `Product` (id, name, price, category).
- **Logique Cache** :
  - `@Cacheable("products")` : Les requêtes `GET /products` sont mises en cache.
  - `@CacheEvict(allEntries=true)` : `POST`, `PUT`, `DELETE` vident le cache pour garantir la fraîcheur.
  - **TTL** : Configuré globalement à 10 minutes.

### 📍 P2: Module MÉTÉO (TTL Intelligent)
*Stratégie : Cache avec expiration temporelle stricte et fallback.*
- **Service** : `WeatherService` appelle l'API OpenWeatherMap.
- **Logique Cache** :
  - `@Cacheable(value="weather", key="#city")` : Met en cache par ville.
  - **TTL Dynamique** : Le service rafraîchit les données automatiquement.
  - **Fallback** : Mode "Demo" si l'API externe échoue.
  - **Refresh** : Endpoint `/refresh` pour forcer la mise à jour (`@CachePut`).

### 📍 P3: Module DEVISES (Cache Distribué & Auto-Refresh)
*Stratégie : Cache partagé avec tâches planifiées.*
- **Service** : `CurrencyService`.
- **Logique Cache** :
  - **Clé Composite** : `#from + '+' + #to` (ex: "USD+EUR").
  - **Auto-Refresh** : `@Scheduled(fixedRate = 3600000)` (1h) met à jour les taux en arrière-plan.
  - **Redis Cache** : Les taux sont stockés directement dans Redis, accessibles par plusieurs instances.

### 📍 P4: SESSIONS (Redis Session Store)
*Stratégie : Gestion de session sans état (Stateless server).*
- **Implémentation** : `Spring Session Data Redis`.
- **Fonctionnement** :
  - Les sessions utilisateurs ne sont PAS stockées dans la RAM du serveur Java.
  - Elles sont sérialisées dans Redis (`spring:session:sessions:...`).
  - **Avantage** : Si le backend redémarre, les utilisateurs restent connectés.

### 📍 P5: NOTIFICATIONS (Redis Pub/Sub)
*Stratégie : Messagerie temps réel.*
- **Composant** : `NotificationService`.
- **Architecture** :
  - Le backend publie un message sur le channel Redis `notifications`.
  - Les abonnés (clients WebSocket ou autres services) reçoivent l'alerte instantanément.
  - **Persistance** : Les notifications sont aussi sauvegardées en MongoDB.

### 📍 P6: FRONTEND & MONITORING
*Stratégie : Interface utilisateur réactive et administration du cache.*
- **UI** : Design "Glassmorphism 2025" (Tailwind CSS).
- **Monitoring Cache** :
  - `CacheController` implémenté pour exposer les stats Redis (Keys, Memory, Hits/Misses).
  - Boutons "Vider le cache" connectés aux endpoints `@CacheEvict`.
  - **Dashboard** : Vue centralisée des métriques.

---

## 4. Guide des Commandes

### Pré-requis
- Docker Desktop lancé.
- Java 17+ et Node.js 16+ installés.

### Démarrage Rapide

**1. Démarrer l'infrastructure (Redis + Mongo)**
```powershell
# À la racine du projet
docker-compose up -d
```

**2. Démarrer le Backend (Spring Boot)**
```powershell
cd backend
./mvnw spring-boot:run
```

**3. Démarrer le Frontend (Angular)**
```powershell
cd frontend
npm install
npm start
```

### Endpoints Clés (Test via Postman/Browser)
- **Produits** : `GET http://localhost:8081/api/products` (Mis en cache)
- **Météo** : `GET http://localhost:8081/api/weather/Paris`
- **Devises** : `GET http://localhost:8081/api/currencies/convert/USD/EUR/100`
- **Monitoring** : `GET http://localhost:8081/api/cache/stats`

---

## 5. Fonctionnalités Implémentées (Validation)
- [x] CRUD Produits avec Cache (P1)
- [x] Météo avec Cache 10min (P2)
- [x] Conversion Devises + Auto-refresh (P3)
- [x] Login/Logout via Redis Sessions (P4)
- [x] Notifications Temps Réel (P5)
- [x] UI/UX Moderne + Stats Cache (P6)



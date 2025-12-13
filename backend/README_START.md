# Guide de Démarrage - Backend CacheFlow

## Prérequis

Avant de démarrer le backend, assurez-vous que les services suivants sont démarrés :

### 1. MongoDB
```bash
# Vérifier si MongoDB est démarré
# Windows: Vérifier dans les services Windows
# Ou démarrer manuellement:
mongod
```

### 2. Redis
```bash
# Vérifier si Redis est démarré
# Windows: 
redis-server
# Ou vérifier dans les services Windows
```

## Démarrage du Backend

### Option 1: Avec Maven
```bash
cd backend
mvn spring-boot:run
```

### Option 2: Avec l'IDE
- Exécuter la classe `BackendApplication.java`
- Ou utiliser le plugin Spring Boot de votre IDE

## Vérification

Une fois démarré, le backend devrait être accessible sur :
- **API**: http://localhost:8081/api/products
- **Swagger**: http://localhost:8081/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health

## Dépannage

### Erreur: "Connection refused" pour MongoDB
- Vérifier que MongoDB est démarré sur le port 27017
- Vérifier l'URI dans `application.yml`

### Erreur: "Connection refused" pour Redis
- Vérifier que Redis est démarré sur le port 6379
- Le backend peut démarrer sans Redis mais le cache ne fonctionnera pas

### Erreur: "Port 8081 already in use"
- Arrêter l'application qui utilise le port 8081
- Ou changer le port dans `application.yml`

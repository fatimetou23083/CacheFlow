# Guide de Dépannage - CacheFlow

## Erreur: "Http failure response for http://localhost:8081/api/products: 0 Unknown Error"

Cette erreur signifie que le backend ne répond pas. Voici les étapes pour résoudre le problème :

### 1. Vérifier que le Backend est démarré

**Option A: Avec Maven**
```bash
cd backend
mvn spring-boot:run
```

**Option B: Avec l'IDE**
- Exécuter `BackendApplication.java`
- Vérifier les logs dans la console

### 2. Vérifier que MongoDB est démarré

**Windows:**
```bash
# Vérifier dans les services Windows
# Ou démarrer manuellement:
mongod
```

**Vérifier la connexion:**
```bash
# Tester la connexion MongoDB
mongo
# Ou
mongosh
```

### 3. Vérifier que Redis est démarré

**Windows:**
```bash
# Démarrer Redis
redis-server
```

**Vérifier la connexion:**
```bash
# Tester Redis
redis-cli ping
# Devrait répondre: PONG
```

### 4. Tester l'API directement

**Avec curl (PowerShell):**
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/api/products/test" -Method GET
```

**Avec Postman:**
- URL: `http://localhost:8081/api/products/test`
- Method: GET
- Devrait retourner: `{"status":"ok","message":"ProductController is working"}`

### 5. Vérifier les ports

**Vérifier que le port 8081 est libre:**
```powershell
netstat -ano | findstr :8081
```

**Si le port est utilisé:**
- Arrêter l'application qui utilise le port
- Ou changer le port dans `application.yml`

### 6. Vérifier les logs du Backend

Cherchez dans les logs:
- `Started BackendApplication` = Backend démarré avec succès
- `Connection refused` = MongoDB ou Redis non démarré
- `Port already in use` = Port 8081 déjà utilisé

### 7. Test rapide

1. Démarrer MongoDB
2. Démarrer Redis (optionnel, le backend peut démarrer sans)
3. Démarrer le backend
4. Tester: `http://localhost:8081/api/products/test`
5. Si ça fonctionne, tester: `http://localhost:8081/api/products`

## Erreurs courantes

### "Connection refused to localhost:27017"
→ MongoDB n'est pas démarré

### "Connection refused to localhost:6379"
→ Redis n'est pas démarré (le backend peut quand même démarrer)

### "Port 8081 already in use"
→ Un autre processus utilise le port 8081

### "CORS error"
→ Vérifier que `CorsConfig.java` est bien configuré

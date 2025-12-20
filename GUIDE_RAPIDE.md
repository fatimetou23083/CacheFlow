# 🚀 Guide Rapide - Module Météo

## Démarrage en 3 étapes

### 1️⃣ Démarrer Redis

```bash
redis-server
```

### 2️⃣ Démarrer le Backend

```bash
cd CacheFlow/backend
./mvnw spring-boot:run
```

✅ Backend disponible sur : http://localhost:8081
✅ Swagger UI : http://localhost:8081/swagger-ui.html

### 3️⃣ Démarrer le Frontend

```bash
cd CacheFlow/frontend
npm install  # Première fois uniquement
npm start
```

✅ Frontend disponible sur : http://localhost:4200
✅ Module météo : http://localhost:4200/weather

---

## 🎯 Utilisation

### Rechercher la météo d'une ville

1. Ouvrez http://localhost:4200/weather
2. Saisissez une ville (ex: "Paris")
3. Cliquez sur "🔍 Rechercher"
4. Les données météo s'affichent !

### Rafraîchir le cache

1. Après avoir recherché une ville
2. Cliquez sur "🔄 Rafraîchir le cache"
3. Les données sont mises à jour

---

## 📊 Comment tester le cache ?

### Test 1 : Cache MISS (première fois)

1. Recherchez "Paris" → **Cache MISS** dans les logs
2. Données récupérées depuis l'API/mode démo

### Test 2 : Cache HIT (données en cache)

1. Recherchez "Paris" à nouveau → **Cache HIT** dans les logs
2. Données récupérées depuis Redis (rapide ⚡)

### Test 3 : Cache expiré

1. Attendez 5-30 minutes selon la saison
2. Recherchez "Paris" → **Cache MISS** (TTL expiré)
3. Nouvelles données récupérées

---

## 🔧 Configuration rapide

### Mode Démo (par défaut)

✅ **Aucune configuration nécessaire** - Fonctionne immédiatement !

Le système génère des données météo fictives pour les tests.

### Mode API Réelle (optionnel)

1. Créez un compte sur https://openweathermap.org/api
2. Obtenez votre clé API gratuite
3. Configurez-la :

```bash
export WEATHER_API_KEY=votre_cle_api
```

4. Modifiez `application.yml` :
```yaml
weather:
  api:
    demo-mode: false
```

5. Redémarrez le backend

---

## 📝 Endpoints API

### GET /api/weather/{city}
Récupère la météo (utilise le cache)

**Exemple** :
```bash
curl http://localhost:8081/api/weather/Paris
```

### POST /api/weather/refresh/{city}
Force la mise à jour du cache

**Exemple** :
```bash
curl -X POST http://localhost:8081/api/weather/refresh/Paris
```

---

## 🐛 Problèmes courants

### ❌ "Weather API authentication failed"

✅ **Solution** : C'est normal ! Le mode démo est activé par défaut.
Le système génère automatiquement des données fictives.

### ❌ Redis ne démarre pas

✅ **Solution** : Installez Redis depuis https://redis.io/download

### ❌ Frontend ne se connecte pas au backend

✅ **Vérifications** :
- Backend démarré sur port 8081 ?
- Testez : `curl http://localhost:8081/api/weather/Paris`

---

## 📚 Documentation complète

Pour plus de détails, consultez :
- `DOCUMENTATION_COMPLETE.md` - Documentation détaillée
- `WEATHER_MODULE_README.md` - Documentation du module

---

## 💡 Astuces

1. **Voir les logs du cache** : Regardez la console du backend pour voir "Cache HIT" ou "Cache MISS"

2. **TTL selon la saison** :
   - Été (juin-août) : 5 minutes
   - Hiver (déc-fév) : 30 minutes
   - Printemps/Automne : 15 minutes

3. **Tester avec plusieurs villes** : Chaque ville a son propre cache

4. **Swagger UI** : http://localhost:8081/swagger-ui.html pour tester l'API directement

---

**Bon développement ! 🚀**


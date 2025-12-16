# 🌤️ Module Météo - Documentation Complète

## 📚 Documentation disponible

Ce module météo dispose de plusieurs documents de documentation pour répondre à tous vos besoins :

### 🚀 [Guide Rapide](GUIDE_RAPIDE.md)
**Pour démarrer rapidement en 3 étapes**
- Démarrage Redis, Backend, Frontend
- Utilisation basique
- Dépannage rapide
- ⏱️ Temps de lecture : 5 minutes

### 🔍 [Explication du Fonctionnement](EXPLICATION_FONCTIONNEMENT.md)
**Comprendre comment ça marche en détail**
- Schémas visuels du flux de données
- Exemples concrets avec logs
- Explication des annotations Spring (@Cacheable, @CachePut)
- Cycle de vie du cache
- ⏱️ Temps de lecture : 15 minutes

### 📖 [Documentation Complète](DOCUMENTATION_COMPLETE.md)
**Documentation technique exhaustive**
- Architecture complète du système
- Configuration détaillée
- Guide d'utilisation avancé
- Dépannage approfondi
- Concepts techniques expliqués
- ⏱️ Temps de lecture : 30 minutes

### 📋 [README du Module](WEATHER_MODULE_README.md)
**Vue d'ensemble du module**
- Fonctionnalités principales
- Structure des fichiers
- Configuration de base
- ⏱️ Temps de lecture : 10 minutes

---

## 🎯 Par où commencer ?

### Si vous voulez démarrer rapidement :
👉 Lisez le [Guide Rapide](GUIDE_RAPIDE.md)

### Si vous voulez comprendre le fonctionnement :
👉 Lisez [Explication du Fonctionnement](EXPLICATION_FONCTIONNEMENT.md)

### Si vous voulez une documentation complète :
👉 Lisez la [Documentation Complète](DOCUMENTATION_COMPLETE.md)

### Si vous voulez une vue d'ensemble :
👉 Lisez le [README du Module](WEATHER_MODULE_README.md)

---

## 🏁 Démarrage ultra-rapide

```bash
# 1. Démarrer Redis
redis-server

# 2. Démarrer le Backend (dans un terminal)
cd CacheFlow/backend
./mvnw spring-boot:run

# 3. Démarrer le Frontend (dans un autre terminal)
cd CacheFlow/frontend
npm start

# 4. Ouvrir dans le navigateur
# http://localhost:4200/weather
```

**C'est tout ! Le module fonctionne en mode démo par défaut.** 🎉

---

## ✨ Fonctionnalités principales

- ✅ **Cache Redis** avec TTL dynamique selon la saison
- ✅ **Mode démo** intégré (pas besoin de clé API)
- ✅ **Logging** automatique des hits/misses
- ✅ **Interface Angular** moderne et responsive
- ✅ **Documentation Swagger** complète
- ✅ **Gestion d'erreurs** robuste avec fallback automatique

---

## 📊 Architecture en bref

```
Frontend Angular → Backend Spring Boot → Cache Redis
                              ↓
                    API OpenWeatherMap (optionnel)
```

**Flux** :
1. Utilisateur saisit une ville
2. Frontend appelle le backend
3. Backend vérifie le cache Redis
4. Si cache MISS → Appelle API/mode démo
5. Stocke dans Redis avec TTL
6. Retourne les données au frontend

---

## 🔧 Configuration

### Mode Démo (par défaut)
✅ **Aucune configuration nécessaire** - Fonctionne immédiatement !

### Mode API Réelle
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

---

## 📝 Endpoints API

- `GET /api/weather/{city}` - Récupère la météo (utilise le cache)
- `POST /api/weather/refresh/{city}` - Force la mise à jour du cache

**Swagger UI** : http://localhost:8081/swagger-ui.html

---

## 🐛 Problèmes courants

### "Weather API authentication failed"
✅ **Normal !** Le mode démo est activé par défaut. Le système génère automatiquement des données fictives.

### Redis ne démarre pas
✅ Installez Redis depuis https://redis.io/download

### Frontend ne se connecte pas
✅ Vérifiez que le backend est démarré sur http://localhost:8081

---

## 📚 Ressources

- [Spring Cache Documentation](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Redis Documentation](https://redis.io/documentation)
- [OpenWeatherMap API](https://openweathermap.org/api)
- [Angular HttpClient](https://angular.io/api/common/http/HttpClient)

---

## 📞 Support

Pour toute question ou problème :
1. Consultez la [Documentation Complète](DOCUMENTATION_COMPLETE.md)
2. Vérifiez la section [Dépannage](DOCUMENTATION_COMPLETE.md#dépannage)
3. Consultez les logs du backend pour plus de détails

---

**Bon développement ! 🚀**


# RÉSUMÉ DU PROJET - CACHEFLOW
> **Livrable : khalive**

## Objectif
Démonstrateur technique d'une architecture Micro-services / Full-stack optimisée par Redis. Le projet prouve comment Redis accélère les applications via le caching, gère les sessions distribuées et permet la communication temps réel.

## Architecture Technique
- **Backend** : Spring Boot 3 (Java 17)
- **Frontend** : Angular 16 (Tailwind CSS, Glassmorphism UI)
- **Data** : MongoDB (Persistance), Redis (Cache & Broker)

## État d'Avancement (P1 - P6)

| Module | Statut | Fonctionnalité Clé Implémentée |
|--------|--------|--------------------------------|
| **P1 Produits** | ✅ Terminé | Cache-Aside (Lecture rapide, invalidation à l'écriture). |
| **P2 Météo** | ✅ Terminé | Cache TTL Intelligent + Mode Démo + Fallback API. |
| **P3 Devises** | ✅ Terminé | Cache Distribué + Tâches planifiées (Auto-refresh 1h). |
| **P4 Sessions** | ✅ Terminé | Sessions stockées dans Redis (Stateless backend). |
| **P5 Notifs** | ✅ Terminé | Redis Pub/Sub pour alertes temps réel. |
| **P6 Frontend** | ✅ Terminé | UI Moderne (2025), Monitoring Cache, Admin Dashboard. |

## Commandes de Lancement
1. **Infra** : `docker-compose up -d`
2. **Back** : `mvn spring-boot:run`
3. **Front** : `npm start`

## Fonctionnalités "Extra" Ajoutées
1. **Layout Dashboard** : Interface avec Sidebar fixe et zones de scroll indépendantes.
2. **Monitoring** : Endpoint `/api/cache/stats` pour voir l'usage mémoire Redis en direct.
3. **Sécurité** : Configuration CORS robuste et gestion centralisée des erreurs.

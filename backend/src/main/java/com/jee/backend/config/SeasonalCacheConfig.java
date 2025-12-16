package com.jee.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.concurrent.CompletableFuture;

@Configuration
@EnableCaching
public class SeasonalCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(SeasonalCacheConfig.class);
    private static final String WEATHER_CACHE = "weather";

    /**
     * Détermine la saison actuelle et retourne le TTL approprié
     * Été (juin, juillet, août) : 5 minutes
     * Hiver (décembre, janvier, février) : 30 minutes
     * Printemps/Automne : 15 minutes (valeur par défaut)
     */
    private Duration getSeasonalTtl() {
        LocalDate now = LocalDate.now();
        Month currentMonth = now.getMonth();

        Duration ttl;
        String season;

        if (currentMonth == Month.JUNE || currentMonth == Month.JULY || currentMonth == Month.AUGUST) {
            // Été : 5 minutes
            ttl = Duration.ofMinutes(5);
            season = "été";
        } else if (currentMonth == Month.DECEMBER || currentMonth == Month.JANUARY || currentMonth == Month.FEBRUARY) {
            // Hiver : 30 minutes
            ttl = Duration.ofMinutes(30);
            season = "hiver";
        } else {
            // Printemps/Automne : 15 minutes
            ttl = Duration.ofMinutes(15);
            season = "printemps/automne";
        }

        logger.info("Saison actuelle: {} - TTL configuré: {} minutes", season, ttl.toMinutes());
        return ttl;
    }

    @Bean
    @Primary
    public CacheManager seasonalCacheManager(RedisConnectionFactory connectionFactory) {
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer(
                Thread.currentThread().getContextClassLoader()
        );

        // Configuration par défaut pour les autres caches (products, product)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // Configuration spécifique pour le cache météo avec TTL dynamique par saison
        Duration weatherTtl = getSeasonalTtl();
        RedisCacheConfiguration weatherConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(weatherTtl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // Créer un RedisCacheWriter personnalisé avec logging des hits/misses
        RedisCacheWriter defaultWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisCacheWriter cacheWriter = new LoggingRedisCacheWriter(defaultWriter, connectionFactory);

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(WEATHER_CACHE, weatherConfig)
                .transactionAware()
                .build();
    }

    /**
     * RedisCacheWriter personnalisé qui log les hits et misses du cache
     */
    private static class LoggingRedisCacheWriter implements RedisCacheWriter {
        private final RedisCacheWriter delegate;
        private final RedisConnectionFactory connectionFactory;

        public LoggingRedisCacheWriter(RedisCacheWriter delegate, RedisConnectionFactory connectionFactory) {
            this.delegate = delegate;
            this.connectionFactory = connectionFactory;
        }

        @Override
        public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
            return delegate.store(name, key, value, ttl);
        }

        @Override
        public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
            return delegate.retrieve(name, key, ttl);
        }

        @Override
        public byte[] get(String name, byte[] key) {
            byte[] value = delegate.get(name, key);
            if (WEATHER_CACHE.equals(name)) {
                if (value != null) {
                    logger.info("Cache HIT - Cache: {}, Key: {}", name, new String(key));
                } else {
                    logger.info("Cache MISS - Cache: {}, Key: {}", name, new String(key));
                }
            }
            return value;
        }

        @Override
        public void put(String name, byte[] key, byte[] value, Duration ttl) {
            delegate.put(name, key, value, ttl);
            if (WEATHER_CACHE.equals(name)) {
                logger.info("Cache PUT - Cache: {}, Key: {}, TTL: {} minutes", 
                        name, new String(key), ttl != null ? ttl.toMinutes() : "default");
            }
        }

        @Override
        public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
            byte[] result = delegate.putIfAbsent(name, key, value, ttl);
            if (WEATHER_CACHE.equals(name)) {
                if (result == null) {
                    logger.info("Cache PUT (if absent) - Cache: {}, Key: {}, TTL: {} minutes", 
                            name, new String(key), ttl != null ? ttl.toMinutes() : "default");
                } else {
                    logger.info("Cache SKIP (already exists) - Cache: {}, Key: {}", name, new String(key));
                }
            }
            return result;
        }

        @Override
        public void evict(String name, byte[] key) {
            RedisConnection connection = connectionFactory.getConnection();
            try {
                connection.del(key);
                if (WEATHER_CACHE.equals(name)) {
                    logger.info("Cache EVICT - Cache: {}, Key: {}", name, new String(key));
                }
            } finally {
                connection.close();
            }
        }

        @Override
        public void clear(String name, byte[] pattern) {
            RedisConnection connection = connectionFactory.getConnection();
            try {
                byte[][] keys = connection.keys(pattern).toArray(new byte[0][]);
                if (keys.length > 0) {
                    connection.del(keys);
                    if (WEATHER_CACHE.equals(name)) {
                        logger.info("Cache CLEAR - Cache: {}, Pattern: {}, Keys deleted: {}", 
                                name, new String(pattern), keys.length);
                    }
                }
            } finally {
                connection.close();
            }
        }

        @Override
        public CacheStatistics getCacheStatistics(String cacheName) {
            return delegate.getCacheStatistics(cacheName);
        }

        @Override
        public void clearStatistics(String cacheName) {
            delegate.clearStatistics(cacheName);
        }

        @Override
        public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector statisticsCollector) {
            return new LoggingRedisCacheWriter(delegate.withStatisticsCollector(statisticsCollector), connectionFactory);
        }
    }
}


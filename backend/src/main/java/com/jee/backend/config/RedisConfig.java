package com.jee.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        // Ne pas initialiser la connexion au démarrage pour éviter les erreurs si Redis n'est pas disponible
        factory.setValidateConnection(false);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        
        // Utiliser JdkSerializationRedisSerializer pour la sérialisation des objets
        // Compatible avec Spring Boot 4.0 (les sérialiseurs Jackson sont dépréciés)
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();
        
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Utiliser JdkSerializationRedisSerializer pour la sérialisation des objets
        // Compatible avec Spring Boot 4.0 (les sérialiseurs Jackson sont dépréciés)
        // Note: Les classes doivent implémenter Serializable
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer(
            Thread.currentThread().getContextClassLoader()
        );
        
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL de 10 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // Créer un RedisCacheWriter personnalisé qui utilise DEL au lieu de UNLINK
        RedisCacheWriter defaultWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisCacheWriter cacheWriter = new DelRedisCacheWriter(defaultWriter, connectionFactory);

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    /**
     * RedisCacheWriter personnalisé qui utilise DEL au lieu de UNLINK
     * pour que les opérations @CacheEvict apparaissent clairement dans les logs Redis
     */
    private static class DelRedisCacheWriter implements RedisCacheWriter {
        private final RedisCacheWriter delegate;
        private final RedisConnectionFactory connectionFactory;

        public DelRedisCacheWriter(RedisCacheWriter delegate, RedisConnectionFactory connectionFactory) {
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
            return delegate.get(name, key);
        }

        @Override
        public void put(String name, byte[] key, byte[] value, Duration ttl) {
            delegate.put(name, key, value, ttl);
        }

        @Override
        public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
            return delegate.putIfAbsent(name, key, value, ttl);
        }

        @Override
        public void evict(String name, byte[] key) {
            // Utiliser DEL au lieu de UNLINK pour apparaître dans les logs
            RedisConnection connection = connectionFactory.getConnection();
            try {
                connection.del(key);
            } finally {
                connection.close();
            }
        }

        @Override
        public void clear(String name, byte[] pattern) {
            // Utiliser DEL au lieu de UNLINK pour apparaître dans les logs
            RedisConnection connection = connectionFactory.getConnection();
            try {
                byte[][] keys = connection.keys(pattern).toArray(new byte[0][]);
                if (keys.length > 0) {
                    connection.del(keys);
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
            return new DelRedisCacheWriter(delegate.withStatisticsCollector(statisticsCollector), connectionFactory);
        }
    }
}

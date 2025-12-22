package com.example.voicechanger.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for Spring Cache
 * Enables caching for performance optimization
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    /**
     * Configure cache manager for in-memory caching
     * Uses ConcurrentHashMap for thread-safe caching
     *
     * @return CacheManager instance
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "userVoiceCodesMap"  // Cache name used in VoiceUserMappingService
        );
    }
}

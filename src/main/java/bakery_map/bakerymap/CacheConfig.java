package bakery_map.bakerymap;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
 
        manager.registerCustomCache("osrmRoute",
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .recordStats()
                        .build());
 
        manager.registerCustomCache("odsayRoute",
                Caffeine.newBuilder()
                        .maximumSize(5_000)
                        .expireAfterWrite(6, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        return manager;
    }
}

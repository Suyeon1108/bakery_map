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

        // OSRM: 도로/도보 경로는 자주 안 바뀜 → 24시간
        manager.registerCustomCache("osrmRoute",
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        // ODsay: 대중교통 배차 변동 고려 → 30분
        manager.registerCustomCache("odsayRoute",
                Caffeine.newBuilder()
                        .maximumSize(5_000)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build());

        return manager;
    }
}

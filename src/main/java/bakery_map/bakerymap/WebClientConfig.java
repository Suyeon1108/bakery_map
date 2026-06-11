package bakery_map.bakerymap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * 범용 WebClient 빈
     * - baseUrl 없이 생성 (OSRM은 mode마다 URL이 달라서 uri()에서 직접 지정)
     * - 응답 버퍼 5MB로 제한 (대용량 폴리라인 대비)
     */
    @Bean
    public WebClient defaultWebClient() {
        return WebClient.builder()
                .codecs(c -> c.defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024))
                .build();
    }
}

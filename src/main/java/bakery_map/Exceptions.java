import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// ── 1. 커스텀 예외 ─────────────────────────────────────────────
public class RouteCalculationException extends RuntimeException {
    public RouteCalculationException(String message) {
        super(message);
    }
}

// ── 2. 전역 예외 핸들러 ────────────────────────────────────────
@Slf4j // ✅ 롬복 로깅 어노테이션 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 외부 API(OSRM, ODsay) 호출 실패 */
    @ExceptionHandler(RouteCalculationException.class)
    public ResponseEntity<Map<String, String>> handleRouteError(RouteCalculationException e) { 
        log.error("[Route API Error] 502 Bad Gateway: {}", e.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", e.getMessage()));
    }

    /** 잘못된 요청 파라미터 (지원하지 않는 mode, 코스 목적지 부족 등) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) { 
        log.warn("[Bad Request] 400 Bad Request: {}", e.getMessage());
        
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
    }
 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllUncaughtException(Exception e) { 
        log.error("[Unhandled Exception] 500 Internal Server Error", e);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) 
                .body(Map.of("error", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
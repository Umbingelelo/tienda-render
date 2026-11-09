package cl.duoc.tiendaneon.salud;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/health -> 200 {"estado":"ok"}
 *
 * La usan:
 * - Render, para saber que la API terminó de arrancar (Health Check Path).
 * - El robot de UptimeRobot, que la visita cada 5 minutos para que Render no la duerma.
 *
 * A propósito NO consulta la base de datos: si el robot despertara Neon cada 5 minutos,
 * gastaría las horas gratis de Neon.
 */
@RestController
public class SaludController {

    @GetMapping("/api/health")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }
}

package cl.duoc.tiendaneon.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Convierte cada error en un JSON con la misma forma, para que el frontend sepa qué mostrar:
 *
 * { "status": 400, "error": "Bad Request", "mensaje": "Hay campos inválidos",
 *   "campos": { "nombre": "El nombre es obligatorio" }, "ruta": "/api/productos" }
 *
 * "campos" solo aparece cuando falla una validación (@Valid).
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    /** 400: el JSON llegó, pero algún campo no cumple las reglas de Producto (@NotBlank, @Min...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        Map<String, Object> cuerpo = cuerpo(HttpStatus.BAD_REQUEST, "Hay campos inválidos", req);
        cuerpo.put("campos", campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** 400: el body no es un JSON válido (falta una coma, una comilla, o un número viene como texto). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonMalFormado(HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "El JSON está mal formado", req);
    }

    /** 400: /api/productos/abc -> el id debe ser un número. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoIncorrecto(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue(), req);
    }

    /** 404 "Producto 99 no existe" (y cualquier otro ResponseStatusException del controller). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> conEstado(ResponseStatusException ex, HttpServletRequest req) {
        return responder(ex.getStatusCode(), ex.getReason(), req);
    }

    /** 404: la ruta no existe, por ejemplo /api/producto (sin la "s"). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> rutaInexistente(HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "La ruta " + req.getRequestURI() + " no existe", req);
    }

    /** 405: la ruta existe, pero no con ese método (por ejemplo DELETE /api/productos sin id). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "El método " + ex.getMethod() + " no está permitido en " + req.getRequestURI(), req);
    }

    /** 415: se envió un body sin el header Content-Type: application/json. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> sinContentType(HttpServletRequest req) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envía el body como JSON, con el header Content-Type: application/json", req);
    }

    /** 500: cualquier otro error. El detalle queda en la consola (o en los Logs de Render), no se le muestra al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor. Revisa los logs.", req);
    }

    private ResponseEntity<Map<String, Object>> responder(HttpStatusCode estado, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(estado).body(cuerpo(estado, mensaje, req));
    }

    private Map<String, Object> cuerpo(HttpStatusCode estado, String mensaje, HttpServletRequest req) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("status", estado.value());
        HttpStatus conocido = HttpStatus.resolve(estado.value());
        cuerpo.put("error", conocido != null ? conocido.getReasonPhrase() : "Error");
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("ruta", req.getRequestURI());
        return cuerpo;
    }
}

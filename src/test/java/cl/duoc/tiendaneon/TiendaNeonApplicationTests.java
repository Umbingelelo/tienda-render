package cl.duoc.tiendaneon;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.tiendaneon.producto.Producto;
import cl.duoc.tiendaneon.producto.ProductoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración: usan la MISMA base de datos configurada en .env
 * (o variables de entorno). Si pasan, la conexión a Neon y las rutas funcionan.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TiendaNeonApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ProductoRepository repository;

    @Test
    void creaLeeFiltraYEliminaUnProducto() throws Exception {
        String json = """
                {"nombre":"Producto de prueba Zq","precio":1990,"stock":3,
                 "imagenUrl":"https://i.ibb.co/ejemplo/foto.jpg"}
                """;

        String respuesta = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        try {
            mvc.perform(get("/api/productos/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Producto de prueba Zq"));

            mvc.perform(get("/api/productos").param("nombre", "prueba zq"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(id));

            mvc.perform(delete("/api/productos/" + id)).andExpect(status().isNoContent());
            mvc.perform(get("/api/productos/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensaje").value("Producto " + id + " no existe"));
        } finally {
            repository.findById(id).ifPresent(repository::delete);
        }
    }

    @Test
    void postRespondeLocationDelNuevoProducto() throws Exception {
        String location = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Con location\",\"precio\":10,\"stock\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
        Assertions.assertEquals("/api/productos/" + id, location);
        repository.deleteById(id);
    }

    @Test
    void rechazaProductoInvalidoConMensajesPorCampo() throws Exception {
        long antes = repository.count();
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"precio\":-1,\"stock\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Hay campos inválidos"))
                .andExpect(jsonPath("$.campos.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.campos.precio").value("El precio no puede ser negativo"))
                .andExpect(jsonPath("$.campos.stock").doesNotExist());
        Assertions.assertEquals(antes, repository.count());
    }

    @Test
    void erroresDeFormaRespondenJson() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El JSON está mal formado"));
        mvc.perform(post("/api/productos").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(delete("/api/productos"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/api/productos/abc"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/producto"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La ruta /api/producto no existe"));
    }

    @Test
    void healthRespondeOk() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"));
    }

    @Test
    void permiteCorsDesdeExpoWeb() throws Exception {
        mvc.perform(get("/api/productos").header("Origin", "http://localhost:8081"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    @Test
    void cargaDatosIniciales() {
        Assertions.assertTrue(
                repository.findAll().stream().map(Producto::getNombre).anyMatch("Polerón"::equals));
    }
}

# Actividad Clase 2: tu API en internet, despierta y consumida desde el celular

**DSY1104 Desarrollo Full Stack II · Parcial 3 (EA3) · Actividad 3.3 Integración RESTful en la nube**
Se trabaja en el equipo del caso semestral (máximo 3 integrantes). Tiempo: el bloque 3 y lo que quede del bloque 2. Evaluación formativa (no lleva nota).

## Objetivo

Que el backend de la tienda del equipo:
1. quede **desplegado en Render** con una URL pública `https://...onrender.com`;
2. **no se duerma**, gracias a un robot de UptimeRobot que lo visita cada 5 minutos;
3. responda **las rutas y los códigos correctos**, comprobados con Postman;
4. sea **consumido desde la app React Native** en un celular.

## Antes de empezar

- La base de **Neon** de la Clase 1 y su `.env` (los 3 datos). Si no la tienen, hagan primero los pasos 1 y 2 de https://github.com/Umbingelelo/tienda-neon.
- Cuenta de **GitHub** (cada integrante).
- **Postman** (web o escritorio).
- Para la Parte D: **Node.js 22 LTS o superior** (mínimo 22.13) en el PC y **Expo Go** actualizado en el celular.
- Guía completa, con capturas: el **README** de https://github.com/Umbingelelo/tienda-render. Los pasos de abajo dicen qué paso del README seguir.
- Repartan roles: **Nube** (Partes A y B, en su cuenta) · **Rutas** (Parte C) · **App** (Parte D).
- **Trabajen en paralelo:** mientras Nube despliega (tarda), **Rutas** arma las peticiones en Postman y **App** hace `npm install`, instala Expo Go y prueba con la URL del profesor que ya viene en `app-movil/src/api.js` (creen y borren **solo sus** productos). Cuando Nube entregue la URL, cambien solo la URL base.

> ⚠️ **Una sola cuenta de Render y un solo servicio con robot por equipo.** Render da 750 horas gratis al mes por workspace (cada cuenta trae uno) y un servicio despierto todo el mes usa ~744. Si alguien crea un segundo servicio "de prueba" con robot, Render suspende todo.

---

## Parte A: Desplegar en Render (la hace UN integrante)

1. **Repositorio** (README, paso 0). Elijan **una** opción:
   - **A1 (recomendada):** en el repo del profesor, **Use this template → Create a new repository** → nombre `tienda-<equipo>`.
   - **A2:** usar el backend del equipo de la Clase 1, copiando los archivos de la tabla **Opción B** del README (incluye reemplazar `Producto`, `ProductoRepository` y `ProductoController`, y copiar `app-movil/`). Prueben `http://localhost:8080/api/health` en su PC antes de subir.

   Confirmen en GitHub que **no** está el archivo `.env`.
2. Crear cuenta en **https://dashboard.render.com/register** con **GitHub** (paso 1).
3. **New Web Service** → **GitHub** → instalar Render **solo en su repositorio** → elegirlo (paso 2).
4. Configurar (paso 3): **Name** `tienda-<equipo>` · **Language** Docker · **Branch** `main` · **Region** Virginia (US East) · plan **Free ($0)**. ⚠️ Por defecto viene marcado el de $7.
5. **Environment Variables → Add from .env** → pegar las 3 líneas del `.env` (paso 4).
6. **Advanced → Health Check Path:** `/api/health` (paso 5).
7. **Deploy web service** y esperar `Your service is live 🎉` en los logs (paso 6).
8. Compartir la URL pública con el equipo (esa sí se puede compartir: no es secreta).

**Evidencia A:**
- Captura de Render con el servicio en **Live**, el plan **Free** y la URL visible.
- Captura de los **Logs** con `Started ...Application` y `Your service is live`.
- Captura del navegador con `https://SU-URL.onrender.com/api/health` → `{"estado":"ok"}`.

## Parte B: El robot (la hace el mismo integrante de la Parte A)

1. **https://dashboard.uptimerobot.com/sign-up** → en *Website to monitor* pegar `https://SU-URL.onrender.com/api/health` → **Github** (README, paso 8).
2. Dejar **Email** como aviso → **Next** → **Skip onboarding**. No agregar más monitores.
3. Esperar a que el monitor quede **Up**.

**Evidencia B:** captura del detalle del monitor donde se lean **Up**, **Checked every 5m** y la URL terminada en **`/api/health`**.

**Pregunta B (respóndanla en la entrega):** ¿por qué el robot visita `/api/health` y no `/api/productos`? ¿Y por qué no serviría `/robots.txt`?

## Parte C: Rutas y códigos con Postman (todo el equipo, contra la URL pública)

Hagan las 9 peticiones de la tabla del **README, paso 7**, usando **su** URL. Para cada una anoten el código que recibieron:

| # | Petición | Código esperado | Código obtenido |
|---|---|---|---|
| 1 | `GET /api/productos` | 200 | |
| 2 | `GET /api/productos?nombre=...` (una palabra de sus productos) | 200 | |
| 3 | `POST /api/productos` con un producto válido de su tienda | 201 | |
| 4 | `POST /api/productos` con `nombre` vacío y `precio` negativo | 400 | |
| 5 | `POST /api/productos` con un JSON roto | 400 | |
| 6 | `GET /api/productos/99999` | 404 | |
| 7 | `DELETE /api/productos` (sin id) | 405 | |
| 8 | `DELETE /api/productos/{id que devolvió la #3}` | 204 | |
| 9 | `POST /api/productos` con Body **raw → Text** | 415 | |

Luego respondan en 1 o 2 líneas cada una:
1. En la petición 3, ¿qué dice el header `Location` de la respuesta y para qué sirve?
2. En la petición 4, ¿en qué parte del JSON de error está el mensaje de **cada** campo?
3. ¿Por qué estas rutas son correctas y `POST /api/crearProducto` no lo sería?

**Evidencia C:** la tabla completa y captura de Postman de las peticiones **3** (con los **Headers de la respuesta** mostrando `Location`) y **4** (con `campos`).

## Parte D: La app en el celular (README, paso 9)

1. Descargar el repositorio del equipo (**Code → Download ZIP**, o `git clone`) y abrir en VS Code **la carpeta del repositorio** (la que contiene `app-movil`). Si su repo no tiene `app-movil` (A2 sin copiarla), descárguenla del repo del profesor.
2. En `app-movil/src/api.js` cambiar `API_URL` por **su** URL de Render (sin `/` al final).
3. En la terminal: `cd app-movil` → `npm install` → `npx expo start`.
4. Abrir **Expo Go** en el celular y escanear el QR. Si no conecta: usen el **hotspot** del celular para el PC, o `npx expo login` + `npx expo start --tunnel` (README, paso 9.2).
5. En la app:
   - ver la lista de productos (`GET → 200 OK`);
   - guardar con el nombre vacío y precio `-1` y ver los mensajes bajo cada campo (**400**);
   - crear un producto real de su tienda (**201**) y **tomar la captura en ese momento**;
   - borrar ese producto (**204**), no los de ejemplo.

**Evidencia D:** foto o captura **del celular** mostrando la app con: (1) los errores del 400 bajo los campos y (2) el aviso `201 Created`.

## Desafío (opcional)

Agreguen al backend una ruta nueva que respete las reglas de nombres, por ejemplo **productos con poco stock**:

- `GET /api/productos?stockMax=5` → 200 + productos con `stock <= 5`.
  - En `ProductoRepository`: `List<Producto> findByStockLessThanEqual(Integer stock);` (necesita `import java.util.List;`).
  - En `ProductoController.listar(...)`: agreguen un segundo parámetro `@RequestParam(required = false) Integer stockMax` y úsenlo antes del `return`: `if (stockMax != null) return repository.findByStockLessThanEqual(stockMax);`
  - Piensen: si llegan `nombre` y `stockMax` juntos, ¿qué debería responder? (Pista: Spring también entiende `findByNombreContainingIgnoreCaseAndStockLessThanEqual(nombre, stockMax)`.)
  - El nombre del parámetro distingue mayúsculas: `?stockmax=5` se ignora sin error.
- Hagan `git push` y comprueben que Render la desplegó solo (**Events**).
- Muéstrenla en la app: en `src/api.js` copien `listarProductos()` como `listarPocoStock()` con `` fetch(`${API_URL}/api/productos?stockMax=5`) `` y agreguen un botón "Poco stock".

**Evidencia:** captura de Postman con la ruta nueva respondiendo desde la URL de Render.

---

## Entrega (al final de la clase)

**Un documento por equipo, subido en AVA**, con:

1. Nombre del equipo, integrantes y sección.
2. Link al repositorio de GitHub **del equipo** (sin `.env` ni claves) y la **URL pública de Render**.
3. Las evidencias A, B, C y D (y la del desafío, si lo hicieron).
4. Las respuestas de la Parte B y la Parte C.

## Pauta de revisión (formativa)

| Criterio | Logrado | Parcial | No logrado |
|---|---|---|---|
| A. Despliegue | Servicio **Live** en plan Free, URL pública responde, claves solo en variables de entorno | Desplegado, pero con claves en el repositorio o no responde siempre | No desplegado |
| B. Robot | Monitor **Up** cada 5 min a `/api/health` y respuesta correcta | Monitor creado, pero a otra ruta o sin explicar el porqué | Sin robot |
| C. Rutas y códigos | Los 9 códigos obtenidos coinciden y las 3 respuestas son correctas | 6 a 8 códigos correctos, o respuestas incompletas | Menos de 6 |
| D. App | La app en el celular consume **su** URL y muestra 200, 400, 201 y 204 | Funciona solo en el navegador del PC, o contra la URL del profesor | No funciona |

## Si algo falla

Revisen la tabla **"Problemas frecuentes"** del README antes de llamar al docente. En Render, abran **Logs** y busquen las líneas `Caused by:`: la **última** suele ser la causa real.

# Tienda Render: tu API en internet y tu app consumiéndola

Proyecto de la **Clase 2** de **DSY1104 Desarrollo Full Stack II**. Es la misma API de productos de la Clase 1 (Spring Boot + Neon), ahora preparada para:

1. vivir en internet, en **Render**, con una URL pública `https://...onrender.com`;
2. no dormirse, gracias a un **robot (UptimeRobot)** que la visita cada 5 minutos;
3. ser consumida desde una **app React Native (Expo)** en tu celular.

```
 Celular con Expo Go                     Render (plan Free)                    Neon
┌──────────────────┐   HTTPS + JSON   ┌──────────────────────────┐   SQL   ┌────────────┐
│ app-movil (RN)   │ ───────────────► │ Spring Boot en Docker    │ ──────► │ PostgreSQL │
│ fetch(API_URL…)  │ ◄─────────────── │ /api/productos           │ ◄────── │ productos  │
└──────────────────┘   200/201/400…   │ /api/health              │         └────────────┘
                                      └──────────────────────────┘
                                                 ▲
                         UptimeRobot ────────────┘  GET /api/health cada 5 min
                         (el "robot")               para que Render no la duerma
```

**¿Por qué no basta con `localhost`?** `localhost` significa "este mismo computador". Si la app corre en tu **celular**, `localhost` es el celular, no tu PC: la app nunca encuentra la API. Por eso la API tiene que estar en internet.

**Palabras que vas a ver**

| Palabra | Qué significa |
|---|---|
| Render | Empresa que ejecuta tu backend en internet. Su plan **Free** es gratis. |
| Deploy (desplegar) | Subir una versión de tu código para que quede funcionando en internet. |
| Docker / `Dockerfile` | Receta que dice cómo construir y ejecutar tu app. Render no trae Java: con el `Dockerfile` arma una "caja" que sí lo tiene. |
| Variable de entorno | Un dato (como la clave de Neon) que se entrega al programa desde afuera, sin escribirlo en el código. Es lo mismo que tu `.env`, pero en Render. |
| Health check | Una ruta simple (`/api/health`) que responde "estoy vivo". |
| Dormir (*spin down*) | Render Free apaga tu API si pasan **15 minutos** sin visitas. La siguiente visita la despierta, pero tarda **cerca de 1 minuto**. |
| UptimeRobot | Servicio gratis que visita tu URL cada 5 minutos. Así tu API nunca cumple 15 minutos sin visitas. |
| Expo Go | App del celular que ejecuta tu proyecto React Native sin instalar nada más. |

## Requisitos

| Necesitas | Detalle |
|---|---|
| Cuenta de **GitHub** | La usarás para entrar a Render y a UptimeRobot. |
| Tu base de **Neon** de la Clase 1 | Con sus 3 datos: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (el contenido de tu `.env`). Si no la tienes, sigue los pasos 1 y 2 del README de la Clase 1: https://github.com/Umbingelelo/tienda-neon |
| **Postman** | Ahora sirve **también la versión web**, porque la API ya no está en `localhost`. |
| **Node.js 20 o superior** (LTS) | Solo para la app (parte final). Descárgalo en https://nodejs.org. Para comprobarlo: `node -v`. |
| **Expo Go** en tu celular | Desde Play Store o App Store. Este proyecto usa **Expo SDK 58**: instala la versión más reciente de Expo Go. |

No necesitas tarjeta de crédito para nada de esto.

---

## Las rutas de esta API (el contrato)

Una **ruta** es método + dirección. Estas son todas las de la tienda. La **URL base** es la de tu servicio, por ejemplo `https://tienda-los-tres.onrender.com`.

| Método | Ruta | Body | Éxito | Errores posibles |
|---|---|---|---|---|
| GET | `/api/health` | — | **200** `{"estado":"ok"}` | — |
| GET | `/api/productos` | — | **200** + lista | — |
| GET | `/api/productos?nombre=pol` | — | **200** + lista filtrada | — |
| GET | `/api/productos/{id}` | — | **200** + producto | **404** si no existe, **400** si el id no es número |
| POST | `/api/productos` | producto en JSON | **201** + header `Location` + producto con su `id` | **400** campos inválidos o JSON mal formado, **415** sin `Content-Type` |
| PUT | `/api/productos/{id}` | producto **completo** en JSON | **200** + producto actualizado | **400**, **404** |
| DELETE | `/api/productos/{id}` | — | **204** sin cuerpo | **404** |

**Cómo se nombran:** sustantivo en **plural** (`productos`), minúsculas, sin verbos en la URL (el verbo es el **método**: GET, POST, PUT, DELETE). El `{id}` va en la ruta porque identifica **un** producto; los filtros van después de `?` (*query params*).

| ❌ Mal | ✅ Bien |
|---|---|
| `GET /api/getProductos` | `GET /api/productos` |
| `POST /api/crearProducto` | `POST /api/productos` |
| `GET /api/borrarProducto?id=3` | `DELETE /api/productos/3` |
| `GET /api/producto/3` (singular) | `GET /api/productos/3` |

**Un producto** se ve así:

```json
{ "id": 1, "nombre": "Polerón", "descripcion": "Con capucha", "precio": 24990, "stock": 10, "imagenUrl": null }
```

**Validaciones** (están en `Producto.java`; las revisa `@Valid` en el controller):

| Campo | Regla | Mensaje si falla |
|---|---|---|
| `nombre` | obligatorio, máximo 100 caracteres | `El nombre es obligatorio` |
| `precio` | obligatorio, 0 o más | `El precio es obligatorio` / `El precio no puede ser negativo` |
| `stock` | obligatorio, 0 o más | `El stock es obligatorio` / `El stock no puede ser negativo` |
| `descripcion`, `imagenUrl` | opcionales, máximo 500 caracteres | `... puede tener como máximo 500 caracteres` |

**Todos los errores tienen la misma forma** (los arma `ManejadorErrores.java`), así el frontend siempre sabe dónde mirar:

```json
{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Hay campos inválidos",
  "ruta": "/api/productos",
  "campos": {
    "nombre": "El nombre es obligatorio",
    "precio": "El precio no puede ser negativo"
  }
}
```

`campos` aparece solo cuando falla una validación. Un 404 se ve así: `{"status":404,"error":"Not Found","mensaje":"Producto 99 no existe","ruta":"/api/productos/99"}`.

---

## Paso 0: Tener tu código en GitHub

Render despliega **desde un repositorio de GitHub**. Elige **una** opción:

### Opción A (recomendada): copiar esta plantilla

1. Arriba en esta página, presiona el botón verde **Use this template** → **Create a new repository**.

   ![Botón Use this template](docs/img/github-01-use-template.png)

2. **Owner:** tu usuario. **Repository name:** `tienda-<nombre-de-tu-equipo>`, por ejemplo `tienda-los-tres`. Puede ser **Public** o **Private**: Render funciona con los dos. Presiona **Create repository**.

   ![Formulario para crear el repositorio desde la plantilla](docs/img/github-02-crear-repo.png)

### Opción B: usar el backend de tu equipo (el de la Clase 1)

Copia a tu repositorio estos archivos de esta plantilla, en las mismas carpetas:

| Archivo | Para qué |
|---|---|
| `Dockerfile` y `.dockerignore` | Para que Render pueda construir tu API (en la raíz, junto a `pom.xml`). |
| `src/main/java/.../salud/SaludController.java` | La ruta `/api/health` para el robot. Cambia la primera línea `package` por el paquete de tu proyecto. |
| `src/main/java/.../config/ManejadorErrores.java` | Errores en JSON con `campos`. Igual: ajusta el `package`. |

Y revisa que tu `application.properties` tenga `server.port=${PORT:8080}` (Render entrega el puerto en la variable `PORT`).

> En las dos opciones: **tu `.env` NO debe estar en GitHub.** Entra a tu repositorio en el navegador y confirma que no aparece un archivo `.env`. Si aparece, bórralo y cambia la clave en Neon (**Reset password**), porque ya es pública.

## Paso 1: Crear tu cuenta en Render

> Las capturas son reales y se tomaron en noviembre de 2026. Si Render cambia un botón de lugar, busca el mismo texto.

1. Entra a **https://dashboard.render.com/register**.
2. Presiona **GitHub** (no uses email: con GitHub, Render ve tus repositorios directamente). Autoriza a Render.
3. Si te pregunta para qué usarás Render, responde cualquier opción.

   ![Pantalla de registro de Render con el botón GitHub](docs/img/render-01-registro.png)

**Una cuenta de Render por equipo.** La crea **un** integrante (el mismo repositorio sirve para todos). Lee el porqué en el paso 8.

## Paso 2: Crear el Web Service y elegir el repositorio

1. En el panel verás **Create a new Service**. En la tarjeta **Web Services**, presiona **New Web Service →**.
   *Si no ves esa pantalla, usa el botón **+ New** (arriba a la derecha) → **Web Service**.*

   ![Pantalla Create a new Service con la tarjeta Web Services](docs/img/render-02-nuevo-web-service.png)

2. En **Source Code** deja la pestaña **Git Provider** y presiona **GitHub**.

   ![Conectar GitHub en Render](docs/img/render-03-conectar-github.png)

3. Se abre GitHub preguntando dónde instalar Render. Elige **tu usuario**. Luego marca **Only select repositories**, abre **Select repositories**, busca tu repositorio (`tienda-...`) y selecciónalo. Presiona **Install**.
   *GitHub puede pedirte confirmar con tu clave, con la app GitHub Mobile o con un código: es normal.*

   ![GitHub: instalar Render solo en el repositorio elegido](docs/img/render-04-github-instalar.png)

4. Vuelves a Render. Escribe el nombre de tu repositorio en el buscador y haz clic en él.

   ![Elegir el repositorio en Render](docs/img/render-05-elegir-repo.png)

## Paso 3: Configurar el servicio

Render detecta el `Dockerfile` y muestra *"It looks like you're using Docker"*. Completa así:

| Campo | Qué poner |
|---|---|
| **Name** | `tienda-<nombre-de-tu-equipo>`. Será parte de tu URL. |
| **Language** | **Docker** (ya viene elegido). |
| **Branch** | `main` |
| **Region** | **Virginia (US East)**: es la más cercana a tu base de Neon en São Paulo. |
| **Root Directory** | Vacío. |

![Nombre, Docker, rama main y región Virginia](docs/img/render-06-configurar-nombre-docker.png)

**Compute (plan):** ⚠️ Render deja marcado por defecto el plan de **$7 / month**. Baja y elige el de **$0 / month · Free**. Abajo de la página debe decir **$0 / month**.

![Plan Free de $0 seleccionado](docs/img/render-07-plan-free.png)

## Paso 4: Variables de entorno (tu `.env`, pero en Render)

Tu `.env` no está en GitHub (bien hecho), así que Render no tiene las claves de Neon. Se las entregas aquí:

1. En **Environment Variables**, presiona **Add from .env**.
2. Pega **el contenido de tu `.env`**, las mismas 3 líneas de la Clase 1, **sin comillas**:

   ```properties
   DB_URL=jdbc:postgresql://ep-xxxx-xxxx.c-2.sa-east-1.aws.neon.tech/neondb?sslmode=require
   DB_USERNAME=neondb_owner
   DB_PASSWORD=npg_tu_clave
   ```

   ![Ventana Add from .env con las 3 variables](docs/img/render-08-add-from-env.png)

3. Presiona **Add variables**. Deben quedar 3 filas: `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Render oculta los valores con puntos; con el ícono del ojo puedes revisarlos.

   ![Las 3 variables de entorno agregadas](docs/img/render-09-variables.png)

> Revisa lo mismo que en la Clase 1: `DB_URL` empieza con `jdbc:postgresql://`, termina en `?sslmode=require` y no tiene `&channel_binding=require`.

## Paso 5: Health Check Path

1. Abre la sección **Advanced** (más abajo).
2. En **Health Check Path** escribe: `/api/health`

   ![Health Check Path con /api/health](docs/img/render-10-health-check.png)

Con esto Render sabe cuándo tu API terminó de arrancar y si sigue viva. El resto de **Advanced** se deja como está.

## Paso 6: Desplegar

1. Presiona **Deploy web service** (abajo a la izquierda).
2. Render muestra los **logs** (la consola de tu API, pero en internet). Primero construye la imagen con Maven (2 a 5 minutos la primera vez) y luego arranca Spring Boot.
3. **Funcionó si** aparecen estas líneas:

   ```
   Started TiendaNeonApplication in 46.4 seconds
   ==> Your service is live 🎉
   ==> Available at your primary URL https://tienda-....onrender.com
   ```

   ![Logs de Render con Your service is live](docs/img/render-11-logs-live.png)

4. Tu **URL pública** aparece arriba, bajo el nombre del servicio, junto a la etiqueta verde **Live**. Cópiala: la usarás en todo lo que sigue.

   ![Servicio Live con su URL pública](docs/img/render-12-servicio-url.png)

Si aparece un error, busca en los logs la **primera** línea que diga `Caused by:` y revisa **Problemas frecuentes** al final.

> **Cada `git push` a `main` vuelve a desplegar solo** (*Auto-Deploy*). Para ver en qué va, entra a **Events** o **Logs** en el menú izquierdo.

## Paso 7: Probar tus rutas en internet

1. En el navegador abre `https://TU-URL.onrender.com/api/health`. Debe mostrar `{"estado":"ok"}`.
2. Abre `https://TU-URL.onrender.com/api/productos`. Verás los productos de **tu** base de Neon: los mismos que veías en `localhost` en la Clase 1.
3. En **Postman**, prueba estas peticiones (para las que llevan body: **Body → raw → JSON**):

| # | Petición | Body | Código esperado |
|---|---|---|---|
| 1 | `GET /api/productos` | — | **200** |
| 2 | `GET /api/productos?nombre=pol` | — | **200** (solo los que contienen "pol") |
| 3 | `POST /api/productos` | `{"nombre":"Gorro","precio":8990,"stock":12}` | **201**. Mira la pestaña **Headers**: `Location: /api/productos/{id}` |
| 4 | `POST /api/productos` | `{"nombre":"","precio":-1}` | **400** con `campos` |
| 5 | `POST /api/productos` | `{"nombre": ` (JSON roto a propósito) | **400** `El JSON está mal formado` |
| 6 | `GET /api/productos/99999` | — | **404** `Producto 99999 no existe` |
| 7 | `DELETE /api/productos` (sin id) | — | **405** método no permitido |
| 8 | `DELETE /api/productos/{id del paso 3}` | — | **204** (sin cuerpo) |

**La primera petición puede tardar hasta 1 minuto** si la API estaba dormida. Después responde rápido. Eso lo arregla el paso 8.

## Paso 8: El robot que no deja dormir a tu API (UptimeRobot)

Render Free apaga tu API después de **15 minutos sin visitas**. UptimeRobot la visita cada **5 minutos**, así nunca llega a 15.

1. Entra a **https://dashboard.uptimerobot.com/sign-up**.
2. En **Website to monitor** pega tu URL **con `/api/health` al final**: `https://TU-URL.onrender.com/api/health`
3. Presiona **Github** y autoriza.

   ![Registro en UptimeRobot con la URL del health check](docs/img/robot-01-registro.png)

4. UptimeRobot crea el monitor solo y avisa *"We'll start checking it every 5 minutes"*. En las preguntas siguientes deja **Email** marcado (te avisa si tu API se cae), presiona **Next** y luego **Skip onboarding** → **Skip onboarding**. **No agregues más monitores.**

   ![Monitor creado, revisión cada 5 minutos](docs/img/robot-02-monitor-creado.png)

5. En **Monitors** verás tu monitor. A los pocos minutos queda en verde: **Up**.

   ![Lista de monitores](docs/img/robot-03-lista-monitores.png)

6. Haz clic en él para ver el detalle. Revisa que diga **HTTP/S monitor for https://...onrender.com/api/health** y **Checked every 5m**.

   ![Detalle del monitor: Up, cada 5 minutos, apuntando a /api/health](docs/img/robot-04-monitor-up.png)

<details>
<summary>¿Ya tenías cuenta en UptimeRobot, o no te creó el monitor? Créalo a mano.</summary>

1. En **Monitors**, presiona **+ New**.
2. **Monitor type:** HTTP / website monitoring. **URL to monitor:** `https://TU-URL.onrender.com/api/health`.

   ![Formulario Add single monitor con la URL](docs/img/robot-05-nuevo-monitor.png)

3. Baja hasta **Monitor interval** y deja **5m** (los intervalos menores son pagados).

   ![Intervalo de 5 minutos](docs/img/robot-06-intervalo.png)

4. Presiona **Create monitor**.
</details>

**¿Por qué `/api/health` y no otra ruta?**
- `/api/health` responde sin consultar la base de datos. Si el robot llamara a `/api/productos`, despertaría a **Neon** cada 5 minutos y gastaría sus horas gratis.
- No uses `/robots.txt`: mientras tu servicio duerme, Render responde esa ruta por su cuenta y **no lo despierta**.

**Ojo con las horas gratis:** Render da **750 horas Free al mes por cuenta**. Un servicio despierto todo el mes usa unas 744: cabe justo. **Dos servicios despiertos no caben** y Render suspende todos tus servicios Free hasta el mes siguiente. Por eso: **un solo servicio con robot por cuenta**, y una cuenta por equipo.

## Paso 9: La app React Native consumiendo tu API

La carpeta `app-movil/` es una app Expo que lista, crea y borra productos, y muestra el código de cada respuesta.

![La app listando productos desde Render](docs/img/app-01-lista.png)

### 9.1 Apuntar la app a TU API

Abre `app-movil/src/api.js` y cambia **solo** esta línea por tu URL (sin `/` al final):

```js
export const API_URL = 'https://tienda-los-tres.onrender.com';
```

Todas las llamadas están en ese archivo. Por ejemplo, crear un producto:

```js
export async function crearProducto(producto) {
  const res = await fetch(`${API_URL}/api/productos`, {
    method: 'POST',                                   // el método
    headers: { 'Content-Type': 'application/json' },  // "te mando JSON" (sin esto: 415)
    body: JSON.stringify(producto),                   // el objeto JS convertido a texto JSON
  });
  const data = await res.json();
  if (res.status === 400) throw { status: 400, campos: data.campos ?? {}, mensaje: data.mensaje };
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return data; // 201: el producto con su id
}
```

`res.ok` es `true` para los códigos 200 a 299. Para el `DELETE` (204) **no** se llama a `res.json()`, porque la respuesta viene vacía.

### 9.2 Ejecutarla

En una terminal (en VS Code: **Terminal → New Terminal**):

```bash
cd app-movil
npm install
npx expo start
```

- **En tu celular:** abre **Expo Go** y escanea el código QR que aparece en la terminal (en iPhone, escanéalo con la cámara).
- **En el navegador del PC:** presiona la tecla `w` en la terminal.

Si el celular no conecta (redes distintas, wifi de Duoc con restricciones), detén con **Ctrl+C** y usa `npx expo start --tunnel`.

### 9.3 Probar los códigos desde la app

| Acción en la app | Código | Qué muestra |
|---|---|---|
| Abrir la app | 200 | La lista de productos y "Estado: despierta ✅" |
| Guardar con el nombre vacío y precio `-1` | 400 | El mensaje de **cada campo** bajo su input |
| Guardar un producto válido | 201 | `201 Created: producto #N guardado en Neon` |
| Borrar un producto | 204 | `204 No Content: producto #N eliminado` |

![La app mostrando los errores del 400 bajo cada campo](docs/img/app-02-error-400.png)
![La app después de crear un producto (201)](docs/img/app-03-creado-201.png)

> **CORS** solo existe en navegadores. La app en Expo Go no lo necesita. Si usas la versión web (`w`, puerto 8081) ya está permitido. Si tu frontend web usa otro puerto, en Render → **Environment** → **Edit** → **Add variable**: key `CORS_ORIGINS`, value con la lista **completa**, por ejemplo `http://localhost:8081,http://localhost:5173`. Presiona **Save, rebuild, and deploy**.

---

## Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| El deploy falla en `RUN mvn ... package` | Tu código no compila. | Ejecútalo primero en tu PC. Lee en los logs la línea `[ERROR]` con el archivo y la línea. |
| `Could not resolve placeholder 'DB_URL'` | Falta una variable de entorno o está mal escrita. | **Environment** → revisa que existan `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, con ese nombre exacto. |
| `password authentication failed for user` | Clave equivocada o alguien presionó **Reset password** en Neon. | Copia de nuevo la clave desde Neon y actualízala en **Environment** → **Save, rebuild, and deploy**. |
| `The connection attempt failed` / `UnknownHostException` | `DB_URL` mal armada. | Debe ser `jdbc:postgresql://HOST/neondb?sslmode=require`, sin comillas ni espacios. |
| `No open ports detected` / el deploy queda en *In progress* y falla por *timed out* | Spring no usa el puerto que da Render. | En `application.properties`: `server.port=${PORT:8080}`. |
| `Exited with status 137` o `OutOfMemoryError` | Java usó más de los 512 MB del plan Free. | Usa el `Dockerfile` de esta plantilla (limita la memoria con `JAVA_TOOL_OPTIONS`). |
| La primera petición tarda ~1 minuto | La API estaba dormida. | Normal sin robot. Configura UptimeRobot (paso 8). |
| `502 Bad Gateway` justo después de un `git push` | Render está desplegando la versión nueva. | Espera a que el deploy termine (**Events**). |
| UptimeRobot marca **Down** | La API se cayó o la URL del monitor está mal. | Abre tu URL `/api/health` en el navegador y mira los **Logs** de Render. |
| Render dice que tus servicios están suspendidos | Se acabaron las 750 horas del mes (¿dos servicios con robot?). | Deja un solo servicio con robot. Las horas se renuevan el día 1. |
| La app dice `Network request failed` | `API_URL` mal escrita. | Debe empezar con `https://`, sin `/` al final. Ábrela en el navegador del celular para probarla. |
| Expo Go: *Project is incompatible with this version of Expo Go* | Tu Expo Go no es para SDK 58. | Actualiza Expo Go desde la tienda. |
| En el navegador: `blocked by CORS policy` | Tu frontend web usa un puerto no permitido. | Agrega `CORS_ORIGINS` en **Environment** (ver paso 9.3). |
| El servicio desplegó una versión vieja | El `push` fue a otra rama. | Render despliega `main`. Revisa con `git branch`. |

## Ejecutar en tu PC (igual que en la Clase 1)

Crea el `.env` junto a `pom.xml` con tus 3 datos de Neon y ejecuta `TiendaNeonApplication.java` con **Run**, o `./mvnw spring-boot:run` (Windows PowerShell: `.\mvnw.cmd spring-boot:run`). La API queda en `http://localhost:8080`.

Pruebas automáticas (usan la base del `.env`): `./mvnw test`. Revisan las rutas, los códigos 200/201/204/400/404/405/415, los mensajes de validación, `/api/health` y CORS.

## Estructura

```
tienda-render/
├── Dockerfile                    receta para Render (Maven compila, Java 17 ejecuta)
├── pom.xml
├── src/main/java/cl/duoc/tiendaneon/
│   ├── producto/
│   │   ├── Producto.java             entidad (tabla productos) + validaciones
│   │   ├── ProductoRepository.java   acceso a Neon (JPA arma el SQL)
│   │   └── ProductoController.java   las rutas /api/productos
│   ├── salud/SaludController.java    GET /api/health (no toca la BD)
│   └── config/
│       ├── ManejadorErrores.java     todos los errores en el mismo JSON
│       ├── CorsConfig.java           orígenes web permitidos
│       └── DatosIniciales.java       3 productos de ejemplo si la tabla está vacía
├── src/main/resources/application.properties
├── app-movil/                    app React Native (Expo SDK 58)
│   ├── App.js                        pantalla: lista, formulario, borrar
│   └── src/api.js                    TODAS las llamadas a la API (cambia API_URL aquí)
├── clase2/ACTIVIDAD.md           actividad de la Clase 2
└── docs/img/                     capturas de esta guía
```

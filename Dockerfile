# Receta para que Render construya y ejecute esta API.
# Render no trae Java instalado: con este archivo arma un contenedor que sí lo tiene.

# --- Etapa 1: compilar el proyecto con Maven y Java 17 ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Primero solo el pom.xml: así Docker reutiliza las librerías descargadas si no cambia.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas usan la base de datos, que no está disponible al construir: se saltan.
RUN mvn -B -q package -DskipTests

# --- Etapa 2: ejecutar solo el .jar con un Java liviano ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# El plan Free de Render tiene 512 MB de RAM y 0,1 CPU: limitamos la memoria de Java
# y le pedimos que compile menos al arrancar, para que despierte más rápido.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
# Render entrega el puerto en la variable PORT (application.properties la lee).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

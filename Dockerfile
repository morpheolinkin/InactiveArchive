# Etapa 1: Build (Construção)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Empacota o projeto pulando os testes para ser mais rápido no deploy
RUN mvn clean package -DskipTests

# Etapa 2: Runtime (Execução)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copia o arquivo .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Expor a porta 8080
EXPOSE 8080
# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
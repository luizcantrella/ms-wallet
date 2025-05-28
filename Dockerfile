# Etapa 1: build da aplicação usando Maven
FROM maven:3.9.9-amazoncorretto-21-alpine AS builder

# Define o diretório de trabalho
WORKDIR /build

# Copia os arquivos do projeto para o container
COPY pom.xml .
COPY ./src ./src

# Executa o build da aplicação
RUN mvn clean package -DskipTests

# Etapa 2: imagem final com Corretto
FROM amazoncorretto:21-alpine3.21

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR gerado da etapa de build
COPY --from=builder /build/target/*.jar app.jar

# Expõe a porta da aplicação (ajuste conforme necessário)
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
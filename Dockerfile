# Multi-stage build para otimizar tamanho da imagem
FROM eclipse-temurin:21-jdk-alpine AS builder

# Instalar dependências necessárias para build
RUN apk add --no-cache maven

# Definir diretório de trabalho
WORKDIR /app

# Copiar apenas os arquivos necessários para build
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Baixar dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src src

# Build da aplicação
RUN mvn clean package -DskipTests -B

# Stage final - apenas runtime
FROM eclipse-temurin:21-jre-alpine

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
  adduser -u 1001 -S appuser -G appgroup

# Instalar dependências mínimas
RUN apk add --no-cache tzdata

# Definir timezone
ENV TZ=UTC

# Criar diretório da aplicação
WORKDIR /app

# Copiar apenas o JAR compilado
COPY --from=builder /app/target/*.jar app.jar

# Mudar propriedade do arquivo para o usuário da aplicação
RUN chown -R appuser:appgroup /app

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+UseStringDeduplication", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "/app/app.jar"] 
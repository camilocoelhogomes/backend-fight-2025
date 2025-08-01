FROM ghcr.io/graalvm/graalvm-community:21 AS binary-source

# Configurar variáveis de ambiente para build nativo
ENV MAVEN_OPTS="-Xmx6g -XX:+UseG1GC"
ENV NATIVE_IMAGE_XMX="6g"

# Instalar Maven sem update para evitar conflitos
RUN microdnf install -y maven

# Definir diretório de trabalho
WORKDIR /app

# Copiar apenas os arquivos necessários para build
COPY pom.xml .

# Copiar código fonte
COPY src src
# Build da aplicação nativa
RUN mvn clean package -Pnative -DskipTests -B


FROM ubuntu:latest
WORKDIR /app
COPY --from=binary-source /app/target/figth /app/figth
RUN chmod +x /app/figth
EXPOSE 8080
CMD ["/app/figth"]
# Health check

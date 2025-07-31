# Multi-stage build para Spring Boot Native
FROM ghcr.io/graalvm/graalvm-community:21 AS builder

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


# Comando padrão que permite acesso interativo
CMD ["/bin/bash"]
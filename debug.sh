#!/bin/bash

echo "🚀 Iniciando Figth Application em modo DEBUG..."
echo "📍 Porta de debug: 5005"
echo "🌐 Aplicação: http://localhost:8080"
echo "🔍 Debug: localhost:5005"
echo ""

# Verificar se o PostgreSQL está rodando
if ! docker ps | grep -q postgres; then
    echo "⚠️  PostgreSQL não está rodando. Iniciando..."
    docker-compose up -d postgres
    sleep 5
fi

# Iniciar aplicação em modo debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" 
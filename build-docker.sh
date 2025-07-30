#!/bin/bash

echo "🐳 Build da Imagem Docker - Figth"
echo ""

# Configurações
IMAGE_NAME="figth"
TAG="latest"
FULL_IMAGE_NAME="${IMAGE_NAME}:${TAG}"

echo "📦 Iniciando build da imagem: ${FULL_IMAGE_NAME}"
echo ""

# Build da imagem
echo "🔨 Executando docker build..."
docker build -t ${FULL_IMAGE_NAME} .

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build concluído com sucesso!"
    echo ""
    
    # Mostrar informações da imagem
    echo "📊 Informações da imagem:"
    docker images ${FULL_IMAGE_NAME}
    
    echo ""
    echo "🚀 Para executar a aplicação:"
    echo "docker run -p 8080:8080 ${FULL_IMAGE_NAME}"
    
    echo ""
    echo "🔍 Para ver logs:"
    echo "docker logs <container_id>"
    
    echo ""
    echo "🛑 Para parar:"
    echo "docker stop <container_id>"
    
else
    echo ""
    echo "❌ Erro no build da imagem!"
    exit 1
fi 
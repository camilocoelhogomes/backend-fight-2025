# 🐳 Configuração Docker - PostgreSQL

## 📋 Visão Geral

Esta configuração utiliza o **PostgreSQL 17.2** (versão mais recente e estável) através do Docker Compose, sem necessidade de Dockerfile customizado.

## 🚀 Como usar

### Iniciar o banco de dados
```bash
docker-compose up -d
```

### Verificar status
```bash
docker-compose ps
```

### Ver logs
```bash
docker-compose logs postgres
```

### Parar o container
```bash
docker-compose down
```

### Parar e remover volumes (⚠️ ATENÇÃO: remove todos os dados)
```bash
docker-compose down -v
```

## ⚙️ Configurações

### Banco de Dados
- **Database**: `figth_db`
- **User**: `figth_user`
- **Password**: `figth_password`
- **Porta**: `5432`

### Configurações de Performance
- **Max Connections**: 100
- **Shared Buffers**: 256MB
- **Effective Cache Size**: 1GB
- **Maintenance Work Mem**: 64MB
- **WAL Buffers**: 16MB

### Volumes
- **Dados**: `postgres_data` (persistente)
- **Scripts de inicialização**: `./init-scripts/`
- **Configuração**: `./postgres-config/postgresql.conf`

## 🔧 Estrutura de Arquivos

```
├── docker-compose.yml          # Configuração principal
├── postgres-config/
│   └── postgresql.conf         # Configurações do PostgreSQL
├── init-scripts/
│   └── 01-init.sql            # Scripts de inicialização
└── .dockerignore              # Arquivos ignorados no build
```

## 📊 Monitoramento

O container inclui **healthcheck** que verifica a conectividade a cada 30 segundos.

## 🔗 Conectando via Aplicação

### Spring Boot (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/figth_db
    username: figth_user
    password: figth_password
    driver-class-name: org.postgresql.Driver
```

### Conectando via psql
```bash
psql -h localhost -p 5432 -U figth_user -d figth_db
``` 
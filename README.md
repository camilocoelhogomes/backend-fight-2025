# 🥊 Figth - Spring Boot Application

## 📋 Visão Geral

Aplicação Spring Boot com PostgreSQL, configurada para usar **OpenJDK 21** e **Maven 3.9.11** através do **asdf**.

## 🚀 Configuração Rápida

### 1. Configurar Ambiente
```bash
# Opção 1: Usar comando direto (recomendado)
./init_env

# Opção 2: Usar script completo
./scripts/init-env.sh
```

### 2. Iniciar PostgreSQL
```bash
docker-compose up -d
```

### 3. Executar Aplicação
```bash
mvn spring-boot:run
```

## ⚙️ Configuração do Ambiente

### Pré-requisitos
- **asdf** instalado
- **Docker** e **Docker Compose** instalados
- **OpenJDK 21.0.2** (gerenciado pelo asdf)
- **Maven 3.9.11** (gerenciado pelo asdf)

### Versões Configuradas
- **Java**: OpenJDK 21.0.2
- **Maven**: 3.9.11
- **Spring Boot**: 3.5.4
- **PostgreSQL**: 17.2

## 🔧 Comandos Úteis

### Desenvolvimento
```bash
# Compilar projeto
mvn clean compile

# Executar aplicação
mvn spring-boot:run

# Executar testes
mvn test

# Gerar JAR
mvn clean package
```

### Docker
```bash
# Iniciar PostgreSQL
docker-compose up -d

# Ver logs do PostgreSQL
docker-compose logs postgres

# Parar PostgreSQL
docker-compose down

# Parar e remover volumes
docker-compose down -v
```

### Ambiente
```bash
# Configurar ambiente (forma mais simples)
./init_env

# Ou usar script completo
./scripts/init-env.sh

# Verificar versões
asdf current

# Verificar Java
java -version

# Verificar Maven
mvn --version
```

## 🌐 Endpoints Disponíveis

### Actuator Endpoints
- **Health Check**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Environment**: `http://localhost:8080/actuator/env`

### Exemplo de Health Check
```bash
curl http://localhost:8080/actuator/health | jq .
```

## 🗄️ Banco de Dados

### Configurações
- **Database**: `figth_db`
- **User**: `figth_user`
- **Password**: `figth_password`
- **Porta**: `5432`

### Conexão via Aplicação
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/figth_db
    username: figth_user
    password: figth_password
```

## 📁 Estrutura do Projeto

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/backend/figth/
│   │   │       └── FigthApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── scripts/
│   ├── init-env.sh          # Script de inicialização
│   ├── dev-setup.sh         # Script de configuração
│   └── setup-alias.sh       # Script para configurar alias
├── postgres-config/
│   └── postgresql.conf      # Configurações do PostgreSQL
├── init-scripts/
│   └── 01-init.sql         # Scripts de inicialização
├── docker-compose.yml       # Configuração Docker
├── .tool-versions          # Versões do asdf
├── init_env               # Comando de inicialização
├── pom.xml                # Dependências Maven
└── README.md              # Este arquivo
```

## 🔍 Troubleshooting

### Problema: Java não encontrado
```bash
# Verificar se asdf está configurado
asdf current

# Recarregar configuração
source ~/.zshrc

# Ou usar comando de inicialização
./init_env
```

### Problema: PostgreSQL não conecta
```bash
# Verificar se Docker está rodando
docker ps

# Iniciar PostgreSQL
docker-compose up -d

# Verificar logs
docker-compose logs postgres
```

### Problema: Porta 8080 ocupada
```bash
# Verificar processos na porta
lsof -i :8080

# Matar processo se necessário
kill -9 <PID>
```

## 📊 Monitoramento

A aplicação inclui **Spring Boot Actuator** com:
- ✅ Health checks detalhados
- 📈 Métricas de aplicação
- 🔧 Informações do ambiente
- 📋 Status dos componentes

## 🛠️ Tecnologias

- **Spring Boot 3.5.4**
- **Spring Data JPA**
- **Spring Cloud OpenFeign**
- **PostgreSQL 17.2**
- **Docker & Docker Compose**
- **asdf** (Version Manager)
- **OpenJDK 21**
- **Maven 3.9.11**
- **Lombok** 
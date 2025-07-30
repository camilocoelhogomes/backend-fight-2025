# API de Pagamentos - Figth

## 🚀 Performance com Virtual Threads

Esta API está configurada para usar **Virtual Threads do Java 21**, permitindo processar milhares de requisições simultâneas com máxima eficiência.

### Benefícios dos Virtual Threads:
- ✅ **Alta Concorrência**: Suporte a milhares de conexões simultâneas
- ✅ **Baixo Uso de Memória**: Virtual Threads são muito mais leves que Platform Threads
- ✅ **Ideal para I/O**: Perfeito para APIs REST com operações de banco e rede
- ✅ **Escalabilidade**: Pode escalar para dezenas de milhares de requisições/segundo
- ✅ **Código Limpo**: Usa `@Async` do Spring Boot sem gerenciamento manual de executors

## Endpoints

### POST /payments

Processa um pagamento com os dados fornecidos usando Virtual Threads para máxima performance.

#### Request Body

```json
{
    "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
    "amount": 19.90
}
```

#### Parâmetros

- `correlationId` (String): Identificador único do pagamento
- `amount` (BigDecimal): Valor do pagamento

#### Response

**Status Code:** 200 OK

```json
{
    "message": "Payment processed successfully",
    "status": "SUCCESS"
}
```

### GET /health/threads

Monitora informações sobre os Virtual Threads em execução.

#### Response

```json
{
    "totalStartedThreadCount": 150,
    "activeThreadCount": 45,
    "peakThreadCount": 200,
    "daemonThreadCount": 25,
    "virtualThreadsEnabled": true,
    "javaVersion": "21.0.1",
    "availableProcessors": 8
}
```

### GET /health/ping

Health check simples.

#### Response

```
pong
```

## Como executar

1. **Compilar o projeto:**
   ```bash
   mvn clean compile
   ```

2. **Executar a aplicação:**
   ```bash
   mvn spring-boot:run
   ```

3. **Testar o endpoint:**
   ```bash
   curl -X POST http://localhost:8080/payments \
     -H "Content-Type: application/json" \
     -d '{"correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3", "amount": 19.90}'
   ```

4. **Monitorar Virtual Threads:**
   ```bash
   curl http://localhost:8080/health/threads
   ```

## Estrutura do Projeto

```
src/main/java/com/backend/figth/
├── FigthApplication.java          # Classe principal da aplicação
├── config/
│   └── VirtualThreadConfig.java   # Configuração de Virtual Threads + @Async
├── controller/
│   ├── PaymentController.java     # Controller REST de pagamentos
│   └── HealthController.java      # Controller de monitoramento
├── service/
│   └── PaymentService.java        # Lógica de negócio com @Async
└── dto/
    ├── PaymentRequestDTO.java     # DTO de entrada
    └── PaymentResponseDTO.java    # DTO de saída
```

## Configurações de Performance

### Virtual Threads + @Async
- **Tomcat Executor**: Configurado para usar `Executors.newVirtualThreadPerTaskExecutor()`
- **@Async Support**: Habilitado com `@EnableAsync` e executor customizado
- **Pool de Conexões**: HikariCP otimizado para 200 conexões máximas
- **Threads do Tomcat**: 200 threads máximos com 10 threads mínimos

### Otimizações
- **Batch Processing**: Hibernate configurado para batch operations
- **Connection Pool**: Configurações otimizadas para alta concorrência
- **Logging**: Níveis ajustados para performance em produção
- **Async Operations**: Gateway e persistência executados em paralelo

## Tecnologias Utilizadas

- **Java 21** - Com suporte nativo a Virtual Threads
- **Spring Boot 3.5.4** - Framework web otimizado
- **Spring Web** - Para APIs REST
- **Spring Async** - Para operações assíncronas
- **HikariCP** - Pool de conexões de alta performance
- **Lombok** - Para redução de boilerplate
- **Maven** - Gerenciamento de dependências

## Teste de Performance

Para testar a capacidade de processamento:

```bash
# Teste de carga simples com Apache Bench
ab -n 1000 -c 100 -H "Content-Type: application/json" \
   -p payment_data.json \
   http://localhost:8080/payments/

# Ou usando wrk
wrk -t12 -c400 -d30s -s payment_script.lua http://localhost:8080/payments
```

## Próximos Passos

Para expandir a funcionalidade:

1. ✅ **Virtual Threads** - Implementado
2. ✅ **@Async Clean** - Implementado sem gerenciamento manual
3. 🔄 **Validações avançadas** - Adicionar Bean Validation
4. 🔄 **Persistência otimizada** - Implementar repository pattern
5. 🔄 **Cache distribuído** - Redis para alta performance
6. 🔄 **Métricas detalhadas** - Micrometer + Prometheus
7. 🔄 **Testes de carga** - Gatling ou JMeter 
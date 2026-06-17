# Reservas Service — Atacama Domes

Microservicio de gestión de domos, reservas y pagos del lodge boutique Atacama Domes.

## Stack
- Java 21 · Spring Boot 3.5 · almacenamiento en memoria (`ConcurrentHashMap`)
- Puerto: **8081**

## Endpoints

### Domos
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/domos` | Listar todos los domos |
| GET | `/domos/{id}` | Obtener domo por ID |
| GET | `/domos/{id}/detalle` | Ficha: superficie, equipación, ubicación, tipo |
| GET | `/domos/disponibles?checkIn=&checkOut=&pasajeros=` | Buscar disponibilidad |
| POST | `/domos` | Crear domo |
| PUT | `/domos/{id}` | Actualizar domo |

### Reservas
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/reservas` | Listar reservas |
| GET | `/reservas/{id}` | Obtener reserva |
| GET | `/reservas/codigo/{codigo}` | Buscar por código RES-XXXX |
| POST | `/reservas` | Crear reserva (valida solapamiento y capacidad) |
| POST | `/reservas/{id}/cancelar` | Cancelar reserva |

### Pagos
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/reservas/{id}/pagos` | Listar pagos de una reserva |
| POST | `/reservas/{id}/pagos` | Registrar pago (confirma reserva si cubre depósito) |

## Datos semilla
Al iniciar se cargan automáticamente:
- **5 domos**: Luna, Salar, Cielo, Volcán (DISPONIBLE) y Oasis (MANTENIMIENTO)
- **13 reservas** con estados CONFIRMADA, CANCELADA y PENDIENTE_PAGO en fechas distintas

## Pruebas unitarias

```bash
./mvnw test
```

23 tests en `ReservasStoreTest` — sin contexto Spring, instancian el store directamente.

## Migración BD (referencia)

`src/main/resources/db/migration/V1__initial.sql` documenta el esquema completo para Flyway/Liquibase si se migra a una BD real.

## Ejecutar en local

```bash
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t dd-reservas .
```

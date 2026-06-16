# Reservas Service - Atacama Domes

Microservicio basico para gestionar domos, reservas de hospedaje y pagos simulados del MVP de Atacama Domes.

Esta version sigue enfoque KISS: no usa base de datos, no usa JPA y guarda datos en memoria mientras la aplicacion esta corriendo.

## Stack

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Boot Actuator
- Maven

## Puerto

```text
http://localhost:8081
```

Cuando se consume desde el gateway:

```text
http://localhost:8080/api/reservas
```

## Datos iniciales

Al iniciar carga domos de ejemplo:

- Domo Luna
- Domo Salar
- Domo Cielo

## Endpoints

### Domos

```http
GET    /domos
GET    /domos/{id}
POST   /domos
PUT    /domos/{id}
GET    /domos/disponibles?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD&pasajeros=2
```

### Reservas

```http
GET    /reservas
GET    /reservas/{id}
GET    /reservas/codigo/{codigo}
POST   /reservas
POST   /reservas/{id}/cancelar
```

### Pagos

```http
GET    /reservas/{id}/pagos
POST   /reservas/{id}/pagos
```

### Health

```http
GET    /actuator/health
```

## Ejecutar

Desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

O usando el jar:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\reservas-service-0.0.1-SNAPSHOT.jar
```

## Pruebas

```powershell
.\mvnw.cmd test
```

## Ejemplos con PowerShell

Listar domos:

```powershell
Invoke-RestMethod http://localhost:8081/domos
```

Buscar domos disponibles:

```powershell
Invoke-RestMethod "http://localhost:8081/domos/disponibles?checkIn=2026-07-01&checkOut=2026-07-03&pasajeros=2"
```

Crear reserva:

```powershell
$reserva = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/reservas `
  -ContentType "application/json" `
  -Body '{"domoId":1,"clienteNombre":"Cliente Demo","clienteEmail":"demo@test.cl","clienteTelefono":"+56900000000","checkIn":"2026-07-01","checkOut":"2026-07-03","pasajeros":2}'

$reserva
```

Registrar pago aprobado:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8081/reservas/$($reserva.id)/pagos" `
  -ContentType "application/json" `
  -Body '{"monto":85000,"metodo":"TRANSFERENCIA","estado":"APROBADO"}'
```

Consultar reserva confirmada:

```powershell
Invoke-RestMethod "http://localhost:8081/reservas/$($reserva.id)"
```

## Reglas simples implementadas

- Una reserva requiere domo, fechas validas y pasajeros.
- `checkIn` debe ser anterior a `checkOut`.
- No se permite reservar un domo ocupado en fechas cruzadas.
- El total se calcula como noches por precio por noche.
- El deposito requerido es el 50 por ciento del total.
- Una reserva inicia como `PENDIENTE_PAGO`.
- Un pago `APROBADO` que cubre el deposito confirma la reserva.
- Una reserva `CANCELADA` no bloquea disponibilidad.

## Notas

- Los datos se pierden al apagar el servicio.
- Este servicio no usa base de datos real todavia.
- Para la demostracion automatizada, usar el script raiz `crud-microservices-demo.ps1`.

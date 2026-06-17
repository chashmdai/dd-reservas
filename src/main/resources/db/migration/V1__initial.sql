-- ============================================================
-- V1__initial.sql  –  Reservas Service
-- Esquema e datos iniciales para migración con Flyway/Liquibase
-- ============================================================

-- ─── Domos ───────────────────────────────────────────────────────────────────

CREATE TABLE domo (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    nombre           VARCHAR(120) NOT NULL,
    capacidad        INT          NOT NULL CHECK (capacidad > 0),
    precio_por_noche DECIMAL(12,2) NOT NULL,
    estado           VARCHAR(30)  NOT NULL DEFAULT 'DISPONIBLE',
    superficie       DOUBLE,
    equipacion       TEXT,
    ubicacion        VARCHAR(255),
    tipo             VARCHAR(50)
);

-- ─── Reservas ─────────────────────────────────────────────────────────────────

CREATE TABLE reserva (
    id                  BIGINT       PRIMARY KEY AUTO_INCREMENT,
    codigo              VARCHAR(20)  NOT NULL UNIQUE,
    domo_id             BIGINT       NOT NULL REFERENCES domo(id),
    cliente_nombre      VARCHAR(150) NOT NULL,
    cliente_email       VARCHAR(150),
    cliente_telefono    VARCHAR(30),
    check_in            DATE         NOT NULL,
    check_out           DATE         NOT NULL,
    pasajeros           INT          NOT NULL,
    noches              INT          NOT NULL,
    total               DECIMAL(12,2) NOT NULL,
    deposito_requerido  DECIMAL(12,2) NOT NULL,
    estado              VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE_PAGO',
    CONSTRAINT chk_fechas CHECK (check_out > check_in)
);

-- ─── Pagos ───────────────────────────────────────────────────────────────────

CREATE TABLE pago (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    reserva_id  BIGINT        NOT NULL REFERENCES reserva(id),
    monto       DECIMAL(12,2) NOT NULL,
    metodo      VARCHAR(50)   NOT NULL DEFAULT 'TRANSFERENCIA',
    estado      VARCHAR(30)   NOT NULL DEFAULT 'APROBADO',
    fecha       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ─── Datos iniciales: Domos ───────────────────────────────────────────────────

INSERT INTO domo (nombre, capacidad, precio_por_noche, estado, superficie, equipacion, ubicacion, tipo) VALUES
('Domo Luna',   2, 85000,  'DISPONIBLE',    38.5, 'Cama doble, calefaccion, bano privado, telescopio, jacuzzi exterior', 'Sector Valle del Rio, km 3', 'GEODESICO'),
('Domo Salar',  4, 120000, 'DISPONIBLE',    52.0, '2 camas matrimoniales, kitchenette, bano privado, terraza panoramica, bicicletas', 'Borde Salar de Atacama, km 7', 'CUPULA'),
('Domo Cielo',  3, 100000, 'DISPONIBLE',    45.0, 'Cama matrimonial, sofa cama, bano privado, piso calefaccionado, hamaca', 'Cerro Quitor, altura 2450m', 'GEODESICO'),
('Domo Volcan', 2, 95000,  'DISPONIBLE',    36.0, 'Cama doble, bano privado, estufa a lena, deck privado con vista al Licancabur', 'Ruta 23 km 45, faldas del Licancabur', 'GEODESICO'),
('Domo Oasis',  5, 140000, 'MANTENIMIENTO', 68.0, '3 camas, sala de estar, cocina equipada, 2 banos, piscina privada, BBQ', 'Oasis de Chiu-Chiu, sector norte', 'CUPULA_GRANDE');

-- ─── Datos iniciales: Reservas ────────────────────────────────────────────────

-- Domo Luna (id=1): 4 reservas en fechas distintas
INSERT INTO reserva (codigo, domo_id, cliente_nombre, cliente_email, cliente_telefono, check_in, check_out, pasajeros, noches, total, deposito_requerido, estado) VALUES
('RES-0001', 1, 'Sofia Morales',   'sofia@mail.cl',   '+56912345678', '2026-01-10', '2026-01-13', 2, 3, 255000.00, 127500.00, 'CONFIRMADA'),
('RES-0002', 1, 'Carlos Pena',     'carlos@mail.cl',  '+56987654321', '2026-02-05', '2026-02-08', 1, 3, 255000.00, 127500.00, 'CANCELADA'),
('RES-0003', 1, 'Maria Fuentes',   'maria@mail.cl',   '+56911112222', '2026-07-15', '2026-07-18', 2, 3, 255000.00, 127500.00, 'PENDIENTE_PAGO'),
('RES-0004', 1, 'Jorge Diaz',      'jorge@mail.cl',   '+56933334444', '2026-03-20', '2026-03-23', 2, 3, 255000.00, 127500.00, 'CONFIRMADA');

-- Domo Salar (id=2): 3 reservas
INSERT INTO reserva (codigo, domo_id, cliente_nombre, cliente_email, cliente_telefono, check_in, check_out, pasajeros, noches, total, deposito_requerido, estado) VALUES
('RES-0005', 2, 'Ana Ramirez',     'ana@mail.cl',     '+56955556666', '2026-01-20', '2026-01-24', 4, 4, 480000.00, 240000.00, 'CONFIRMADA'),
('RES-0006', 2, 'Pablo Torres',    'pablo@mail.cl',   '+56977778888', '2026-02-14', '2026-02-17', 2, 3, 360000.00, 180000.00, 'CANCELADA'),
('RES-0007', 2, 'Valentina Rios',  'vale@mail.cl',    '+56999990000', '2026-08-10', '2026-08-14', 3, 4, 480000.00, 240000.00, 'PENDIENTE_PAGO');

-- Domo Cielo (id=3): 4 reservas
INSERT INTO reserva (codigo, domo_id, cliente_nombre, cliente_email, cliente_telefono, check_in, check_out, pasajeros, noches, total, deposito_requerido, estado) VALUES
('RES-0008', 3, 'Luis Herrera',    'luis@mail.cl',    '+56922223333', '2026-01-05', '2026-01-07', 2, 2, 200000.00, 100000.00, 'CONFIRMADA'),
('RES-0009', 3, 'Camila Vega',     'camila@mail.cl',  '+56944445555', '2026-04-22', '2026-04-25', 3, 3, 300000.00, 150000.00, 'CANCELADA'),
('RES-0010', 3, 'Roberto Soto',    'roberto@mail.cl', '+56966667777', '2026-09-01', '2026-09-04', 2, 3, 300000.00, 150000.00, 'PENDIENTE_PAGO'),
('RES-0011', 3, 'Daniela Munoz',   'daniela@mail.cl', '+56988889999', '2026-05-10', '2026-05-13', 3, 3, 300000.00, 150000.00, 'CONFIRMADA');

-- Domo Volcan (id=4): 1 reserva
INSERT INTO reserva (codigo, domo_id, cliente_nombre, cliente_email, cliente_telefono, check_in, check_out, pasajeros, noches, total, deposito_requerido, estado) VALUES
('RES-0012', 4, 'Tomas Araya',     'tomas@mail.cl',   '+56911223344', '2026-06-05', '2026-06-08', 2, 3, 285000.00, 142500.00, 'CONFIRMADA');

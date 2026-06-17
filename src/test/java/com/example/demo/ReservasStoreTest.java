package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReservasStore - pruebas unitarias")
class ReservasStoreTest {

    private ReservasStore store;

    @BeforeEach
    void setUp() {
        store = new ReservasStore();
        store.cargarDatosIniciales();
    }

    // ─── Domos ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga inicial: debe haber al menos 3 domos")
    void datosIniciales_domosPresentes() {
        List<Domo> domos = store.findDomos();
        assertTrue(domos.size() >= 3, "Se esperan al menos 3 domos iniciales");
    }

    @Test
    @DisplayName("Carga inicial: los domos tienen campos de detalle completos")
    void datosIniciales_domosConDetalle() {
        Domo domo = store.findDomos().get(0);
        assertNotNull(domo.superficie(), "Superficie no debe ser null");
        assertNotNull(domo.ubicacion(), "Ubicacion no debe ser null");
        assertNotNull(domo.tipo(), "Tipo no debe ser null");
        assertNotNull(domo.equipacion(), "Equipacion no debe ser null");
    }

    @Test
    @DisplayName("findDomo: retorna el domo si existe")
    void findDomo_existente() {
        Long id = store.findDomos().get(0).id();
        Optional<Domo> result = store.findDomo(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
    }

    @Test
    @DisplayName("findDomo: retorna vacío si no existe")
    void findDomo_inexistente() {
        Optional<Domo> result = store.findDomo(9999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createDomo: asigna id y persiste el domo")
    void createDomo_asignaId() {
        Domo input = new Domo(null, "Domo Test", 2, new BigDecimal("75000"), "DISPONIBLE",
                30.0, "Cama doble, bano privado", "Sector norte", "GEODESICO");
        Domo saved = store.createDomo(input);

        assertNotNull(saved.id());
        assertEquals("Domo Test", saved.nombre());
        assertEquals(2, saved.capacidad());
        assertEquals("GEODESICO", saved.tipo());
        assertTrue(store.findDomo(saved.id()).isPresent());
    }

    @Test
    @DisplayName("updateDomo: actualiza los campos del domo existente")
    void updateDomo_actualiza() {
        Long id = store.findDomos().get(0).id();
        Domo update = new Domo(null, "Domo Modificado", 3, new BigDecimal("90000"), "DISPONIBLE",
                40.0, "Nueva equipacion", "Nueva ubicacion", "CUPULA");
        Optional<Domo> result = store.updateDomo(id, update);

        assertTrue(result.isPresent());
        assertEquals("Domo Modificado", result.get().nombre());
        assertEquals(3, result.get().capacidad());
        assertEquals("CUPULA", result.get().tipo());
    }

    @Test
    @DisplayName("updateDomo: retorna vacío si el domo no existe")
    void updateDomo_inexistente() {
        Domo update = new Domo(null, "X", 1, BigDecimal.ONE, "DISPONIBLE", null, null, null, null);
        Optional<Domo> result = store.updateDomo(9999L, update);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findDisponibles: filtra por capacidad minima")
    void findDisponibles_filtraCapacidad() {
        List<Domo> disponibles = store.findDisponibles(null, null, 100);
        assertTrue(disponibles.isEmpty(), "No debe haber domos con capacidad >= 100");
    }

    @Test
    @DisplayName("findDisponibles: retorna domos DISPONIBLE con capacidad suficiente")
    void findDisponibles_retornaCorrectos() {
        List<Domo> disponibles = store.findDisponibles("2030-01-01", "2030-01-03", 2);
        assertTrue(disponibles.stream().allMatch(d -> "DISPONIBLE".equals(d.estado())));
        assertTrue(disponibles.stream().allMatch(d -> d.capacidad() >= 2));
    }

    // ─── Reservas ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga inicial: debe haber al menos 10 reservas con distintos estados")
    void datosIniciales_reservasPresentes() {
        List<Reserva> reservas = store.findReservas();
        assertTrue(reservas.size() >= 10, "Se esperan al menos 10 reservas iniciales");

        boolean hayConfirmada = reservas.stream().anyMatch(r -> "CONFIRMADA".equals(r.estado()));
        boolean hayCancelada = reservas.stream().anyMatch(r -> "CANCELADA".equals(r.estado()));
        boolean hayPendiente = reservas.stream().anyMatch(r -> "PENDIENTE_PAGO".equals(r.estado()));

        assertTrue(hayConfirmada, "Debe haber al menos una reserva CONFIRMADA");
        assertTrue(hayCancelada, "Debe haber al menos una reserva CANCELADA");
        assertTrue(hayPendiente, "Debe haber al menos una reserva PENDIENTE_PAGO");
    }

    @Test
    @DisplayName("Carga inicial: las reservas tienen fechas distintas")
    void datosIniciales_reservasFechasDistintas() {
        List<Reserva> reservas = store.findReservas();
        long fechasUnicas = reservas.stream().map(Reserva::checkIn).distinct().count();
        assertTrue(fechasUnicas >= 5, "Las reservas deben tener al menos 5 fechas de check-in distintas");
    }

    @Test
    @DisplayName("createReserva: crea correctamente con estado PENDIENTE_PAGO")
    void createReserva_exitosa() {
        Long domoId = store.findDisponibles("2030-06-01", "2030-06-03", 1).get(0).id();

        Reserva input = new Reserva(null, null, domoId, "Cliente Test", "test@test.cl",
                "+56900000000", "2030-06-01", "2030-06-03", 1, null, null, null, null);
        Optional<Reserva> result = store.createReserva(input);

        assertTrue(result.isPresent());
        assertEquals("PENDIENTE_PAGO", result.get().estado());
        assertNotNull(result.get().codigo());
        assertTrue(result.get().codigo().startsWith("RES-"));
        assertEquals(2, result.get().noches());
    }

    @Test
    @DisplayName("createReserva: rechaza si checkIn >= checkOut")
    void createReserva_fechasInvertidas() {
        Long domoId = store.findDomos().stream()
                .filter(d -> "DISPONIBLE".equals(d.estado()))
                .findFirst().get().id();

        Reserva input = new Reserva(null, null, domoId, "Test", null, null,
                "2030-06-05", "2030-06-01", 1, null, null, null, null);
        Optional<Reserva> result = store.createReserva(input);
        assertTrue(result.isEmpty(), "Debe rechazar fechas invertidas");
    }

    @Test
    @DisplayName("createReserva: rechaza si el domo ya tiene reserva en esas fechas")
    void createReserva_domoOcupado() {
        Long domoId = store.findDisponibles("2030-07-01", "2030-07-04", 1).get(0).id();

        Reserva primera = new Reserva(null, null, domoId, "A", null, null,
                "2030-07-01", "2030-07-04", 1, null, null, null, null);
        store.createReserva(primera);

        Reserva segunda = new Reserva(null, null, domoId, "B", null, null,
                "2030-07-02", "2030-07-05", 1, null, null, null, null);
        Optional<Reserva> result = store.createReserva(segunda);
        assertTrue(result.isEmpty(), "Debe rechazar reserva con fechas superpuestas");
    }

    @Test
    @DisplayName("createReserva: rechaza si pasajeros supera la capacidad del domo")
    void createReserva_capacidadInsuficiente() {
        // Domo Luna tiene capacidad 2
        Long domoId = store.findDomos().stream()
                .filter(d -> d.nombre().equals("Domo Luna"))
                .findFirst().get().id();

        Reserva input = new Reserva(null, null, domoId, "Grupo", null, null,
                "2030-08-01", "2030-08-03", 10, null, null, null, null);
        Optional<Reserva> result = store.createReserva(input);
        assertTrue(result.isEmpty(), "Debe rechazar cuando pasajeros supera capacidad");
    }

    @Test
    @DisplayName("createReserva: calcula total = noches * precioPorNoche")
    void createReserva_calculaTotal() {
        // Domo Luna: 85000/noche
        Long domoId = store.findDomos().stream()
                .filter(d -> d.nombre().equals("Domo Luna"))
                .findFirst().get().id();

        Reserva input = new Reserva(null, null, domoId, "Test", null, null,
                "2030-09-01", "2030-09-04", 1, null, null, null, null);
        Optional<Reserva> result = store.createReserva(input);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("255000"), result.get().total());
        assertEquals(new BigDecimal("127500.00"), result.get().depositoRequerido());
    }

    @Test
    @DisplayName("cancelarReserva: cambia estado a CANCELADA")
    void cancelarReserva_exitosa() {
        Long domoId = store.findDisponibles("2030-10-01", "2030-10-03", 1).get(0).id();
        Reserva input = new Reserva(null, null, domoId, "A", null, null,
                "2030-10-01", "2030-10-03", 1, null, null, null, null);
        Long id = store.createReserva(input).get().id();

        Optional<Reserva> cancelada = store.cancelarReserva(id);
        assertTrue(cancelada.isPresent());
        assertEquals("CANCELADA", cancelada.get().estado());
    }

    @Test
    @DisplayName("cancelarReserva: retorna vacío si la reserva no existe")
    void cancelarReserva_inexistente() {
        assertTrue(store.cancelarReserva(9999L).isEmpty());
    }

    @Test
    @DisplayName("createPago: confirma la reserva cuando el monto cubre el deposito")
    void createPago_confirmaReserva() {
        Long domoId = store.findDisponibles("2030-11-01", "2030-11-03", 1).get(0).id();
        Reserva input = new Reserva(null, null, domoId, "A", null, null,
                "2030-11-01", "2030-11-03", 1, null, null, null, null);
        Reserva reserva = store.createReserva(input).get();

        Pago pago = new Pago(null, null, reserva.depositoRequerido(), "TRANSFERENCIA", "APROBADO", null);
        store.createPago(reserva.id(), pago);

        Reserva actualizada = store.findReserva(reserva.id()).get();
        assertEquals("CONFIRMADA", actualizada.estado());
    }

    @Test
    @DisplayName("createPago: no confirma si el monto no cubre el deposito")
    void createPago_montoInsuficiente() {
        Long domoId = store.findDisponibles("2030-12-01", "2030-12-03", 1).get(0).id();
        Reserva input = new Reserva(null, null, domoId, "A", null, null,
                "2030-12-01", "2030-12-03", 1, null, null, null, null);
        Reserva reserva = store.createReserva(input).get();

        Pago pago = new Pago(null, null, new BigDecimal("1"), "TRANSFERENCIA", "APROBADO", null);
        store.createPago(reserva.id(), pago);

        Reserva actualizada = store.findReserva(reserva.id()).get();
        assertEquals("PENDIENTE_PAGO", actualizada.estado());
    }

    @Test
    @DisplayName("findReservaByCodigo: encuentra la reserva por su codigo")
    void findReservaByCodigo_exitosa() {
        Long domoId = store.findDisponibles("2031-01-01", "2031-01-03", 1).get(0).id();
        Reserva input = new Reserva(null, null, domoId, "A", null, null,
                "2031-01-01", "2031-01-03", 1, null, null, null, null);
        Reserva reserva = store.createReserva(input).get();

        Optional<Reserva> found = store.findReservaByCodigo(reserva.codigo());
        assertTrue(found.isPresent());
        assertEquals(reserva.id(), found.get().id());
    }

    @Test
    @DisplayName("findDomoDetalle: retorna superficie, equipacion, ubicacion y tipo")
    void findDomoDetalle_retornaCampos() {
        Long id = store.findDomos().get(0).id();
        Optional<DomoDetalle> detalle = store.findDomoDetalle(id);

        assertTrue(detalle.isPresent());
        assertNotNull(detalle.get().superficie());
        assertNotNull(detalle.get().equipacion());
        assertNotNull(detalle.get().ubicacion());
        assertNotNull(detalle.get().tipo());
    }

    @Test
    @DisplayName("findDomoDetalle: retorna vacío si el domo no existe")
    void findDomoDetalle_inexistente() {
        assertTrue(store.findDomoDetalle(9999L).isEmpty());
    }
}

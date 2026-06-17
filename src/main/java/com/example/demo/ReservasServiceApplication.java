package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ReservasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservasServiceApplication.class, args);
	}

}

record Domo(
		Long id,
		String nombre,
		Integer capacidad,
		BigDecimal precioPorNoche,
		String estado,
		Double superficie,
		String equipacion,
		String ubicacion,
		String tipo) {
}

record DomoDetalle(Long id, String nombre, Double superficie, String equipacion, String ubicacion, String tipo) {
}

record Reserva(
		Long id,
		String codigo,
		Long domoId,
		String clienteNombre,
		String clienteEmail,
		String clienteTelefono,
		String checkIn,
		String checkOut,
		Integer pasajeros,
		Integer noches,
		BigDecimal total,
		BigDecimal depositoRequerido,
		String estado) {
}

record Pago(Long id, Long reservaId, BigDecimal monto, String metodo, String estado, String fecha) {
}

@Service
class ReservasStore {

	private static final List<String> ESTADOS_QUE_BLOQUEAN = List.of("PENDIENTE_PAGO", "CONFIRMADA");

	private final AtomicLong domoIds = new AtomicLong();
	private final AtomicLong reservaIds = new AtomicLong();
	private final AtomicLong pagoIds = new AtomicLong();

	private final Map<Long, Domo> domos = new ConcurrentHashMap<>();
	private final Map<Long, Reserva> reservas = new ConcurrentHashMap<>();
	private final Map<Long, Pago> pagos = new ConcurrentHashMap<>();

	@PostConstruct
	void cargarDatosIniciales() {
		// --- Domos ---
		Domo domoLuna = createDomo(new Domo(null, "Domo Luna", 2, new BigDecimal("85000"), "DISPONIBLE",
				38.5, "Cama doble, calefaccion, bano privado, telescopio, jacuzzi exterior", "Sector Valle del Rio, km 3", "GEODESICO"));
		Domo domoSalar = createDomo(new Domo(null, "Domo Salar", 4, new BigDecimal("120000"), "DISPONIBLE",
				52.0, "2 camas matrimoniales, kitchenette, bano privado, terraza panoramica, bicicletas", "Borde Salar de Atacama, km 7", "CUPULA"));
		Domo domoCielo = createDomo(new Domo(null, "Domo Cielo", 3, new BigDecimal("100000"), "DISPONIBLE",
				45.0, "Cama matrimonial, sofa cama, bano privado, piso calefaccionado, hamaca", "Cerro Quitor, altura 2450m", "GEODESICO"));
		Domo domoVolcan = createDomo(new Domo(null, "Domo Volcan", 2, new BigDecimal("95000"), "DISPONIBLE",
				36.0, "Cama doble, bano privado, estufa a lena, deck privado con vista al Licancabur", "Ruta 23 km 45, faldas del Licancabur", "GEODESICO"));
		Domo domoOasis = createDomo(new Domo(null, "Domo Oasis", 5, new BigDecimal("140000"), "MANTENIMIENTO",
				68.0, "3 camas, sala de estar, cocina equipada, 2 banos, piscina privada, BBQ", "Oasis de Chiu-Chiu, sector norte", "CUPULA_GRANDE"));

		// --- Reservas pre-cargadas (minimo 10, fechas distintas, estados variados) ---

		// Domo Luna (id=1)
		insertarReserva(domoLuna.id(), "Sofia Morales", "sofia@mail.cl", "+56912345678",
				"2026-01-10", "2026-01-13", 2, "CONFIRMADA");
		insertarReserva(domoLuna.id(), "Carlos Pena", "carlos@mail.cl", "+56987654321",
				"2026-02-05", "2026-02-08", 1, "CANCELADA");
		insertarReserva(domoLuna.id(), "Maria Fuentes", "maria@mail.cl", "+56911112222",
				"2026-07-15", "2026-07-18", 2, "PENDIENTE_PAGO");
		insertarReserva(domoLuna.id(), "Jorge Diaz", "jorge@mail.cl", "+56933334444",
				"2026-03-20", "2026-03-23", 2, "CONFIRMADA");

		// Domo Salar (id=2)
		insertarReserva(domoSalar.id(), "Ana Ramirez", "ana@mail.cl", "+56955556666",
				"2026-01-20", "2026-01-24", 4, "CONFIRMADA");
		insertarReserva(domoSalar.id(), "Pablo Torres", "pablo@mail.cl", "+56977778888",
				"2026-02-14", "2026-02-17", 2, "CANCELADA");
		insertarReserva(domoSalar.id(), "Valentina Rios", "vale@mail.cl", "+56999990000",
				"2026-08-10", "2026-08-14", 3, "PENDIENTE_PAGO");

		// Domo Cielo (id=3)
		insertarReserva(domoCielo.id(), "Luis Herrera", "luis@mail.cl", "+56922223333",
				"2026-01-05", "2026-01-07", 2, "CONFIRMADA");
		insertarReserva(domoCielo.id(), "Camila Vega", "camila@mail.cl", "+56944445555",
				"2026-04-22", "2026-04-25", 3, "CANCELADA");
		insertarReserva(domoCielo.id(), "Roberto Soto", "roberto@mail.cl", "+56966667777",
				"2026-09-01", "2026-09-04", 2, "PENDIENTE_PAGO");
		insertarReserva(domoCielo.id(), "Daniela Munoz", "daniela@mail.cl", "+56988889999",
				"2026-05-10", "2026-05-13", 3, "CONFIRMADA");

		// Domo Volcan (id=4)
		insertarReserva(domoVolcan.id(), "Tomas Araya", "tomas@mail.cl", "+56911223344",
				"2026-06-05", "2026-06-08", 2, "CONFIRMADA");
	}

	private void insertarReserva(Long domoId, String clienteNombre, String email, String tel,
								 String checkIn, String checkOut, int pasajeros, String estado) {
		Domo domo = domos.get(domoId);
		if (domo == null) return;
		LocalDate ci = LocalDate.parse(checkIn);
		LocalDate co = LocalDate.parse(checkOut);
		long nights = ChronoUnit.DAYS.between(ci, co);
		BigDecimal total = domo.precioPorNoche().multiply(BigDecimal.valueOf(nights));
		BigDecimal deposito = total.multiply(new BigDecimal("0.50"));
		long id = reservaIds.incrementAndGet();
		Reserva r = new Reserva(id, "RES-" + String.format("%04d", id), domoId,
				clienteNombre, email, tel, checkIn, checkOut, pasajeros, (int) nights, total, deposito, estado);
		reservas.put(id, r);
	}

	List<Domo> findDomos() {
		return domos.values().stream().sorted(Comparator.comparing(Domo::id)).toList();
	}

	Optional<Domo> findDomo(Long id) {
		return Optional.ofNullable(domos.get(id));
	}

	Optional<DomoDetalle> findDomoDetalle(Long id) {
		Domo d = domos.get(id);
		if (d == null) return Optional.empty();
		return Optional.of(new DomoDetalle(d.id(), d.nombre(), d.superficie(), d.equipacion(), d.ubicacion(), d.tipo()));
	}

	Domo createDomo(Domo input) {
		Domo saved = new Domo(domoIds.incrementAndGet(), textOr(input.nombre(), "Domo sin nombre"),
				numberOr(input.capacidad(), 1), moneyOr(input.precioPorNoche()), textOr(input.estado(), "DISPONIBLE"),
				input.superficie(), input.equipacion(), input.ubicacion(), input.tipo());
		domos.put(saved.id(), saved);
		return saved;
	}

	Optional<Domo> updateDomo(Long id, Domo input) {
		if (!domos.containsKey(id)) {
			return Optional.empty();
		}

		Domo saved = new Domo(id, textOr(input.nombre(), "Domo sin nombre"), numberOr(input.capacidad(), 1),
				moneyOr(input.precioPorNoche()), textOr(input.estado(), "DISPONIBLE"),
				input.superficie(), input.equipacion(), input.ubicacion(), input.tipo());
		domos.put(id, saved);
		return Optional.of(saved);
	}

	List<Domo> findDisponibles(String checkIn, String checkOut, Integer pasajeros) {
		int pasajerosRequeridos = numberOr(pasajeros, 1);
		LocalDate inicio = parseDateOrNull(checkIn);
		LocalDate fin = parseDateOrNull(checkOut);

		return domos.values().stream()
				.filter(domo -> "DISPONIBLE".equals(domo.estado()))
				.filter(domo -> domo.capacidad() >= pasajerosRequeridos)
				.filter(domo -> inicio == null || fin == null || isDisponible(domo.id(), inicio, fin))
				.sorted(Comparator.comparing(Domo::id))
				.toList();
	}

	List<Reserva> findReservas() {
		return reservas.values().stream().sorted(Comparator.comparing(Reserva::id)).toList();
	}

	Optional<Reserva> findReserva(Long id) {
		return Optional.ofNullable(reservas.get(id));
	}

	Optional<Reserva> findReservaByCodigo(String codigo) {
		return reservas.values().stream()
				.filter(reserva -> reserva.codigo().equalsIgnoreCase(codigo))
				.findFirst();
	}

	Optional<Reserva> createReserva(Reserva input) {
		if (input.domoId() == null) {
			return Optional.empty();
		}

		Domo domo = domos.get(input.domoId());
		LocalDate checkIn = parseDateOrNull(input.checkIn());
		LocalDate checkOut = parseDateOrNull(input.checkOut());
		int pasajeros = numberOr(input.pasajeros(), 1);

		if (domo == null || !"DISPONIBLE".equals(domo.estado()) || checkIn == null || checkOut == null
				|| !checkIn.isBefore(checkOut) || pasajeros <= 0 || pasajeros > domo.capacidad()
				|| !isDisponible(domo.id(), checkIn, checkOut)) {
			return Optional.empty();
		}

		long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
		BigDecimal total = domo.precioPorNoche().multiply(BigDecimal.valueOf(nights));
		BigDecimal deposito = total.multiply(new BigDecimal("0.50"));
		long id = reservaIds.incrementAndGet();

		Reserva saved = new Reserva(id, "RES-" + String.format("%04d", id), domo.id(),
				textOr(input.clienteNombre(), "Cliente"), input.clienteEmail(), input.clienteTelefono(),
				checkIn.toString(), checkOut.toString(), pasajeros, Math.toIntExact(nights), total, deposito,
				"PENDIENTE_PAGO");
		reservas.put(saved.id(), saved);
		return Optional.of(saved);
	}

	Optional<Reserva> cancelarReserva(Long id) {
		Reserva current = reservas.get(id);
		if (current == null) {
			return Optional.empty();
		}

		Reserva saved = new Reserva(current.id(), current.codigo(), current.domoId(), current.clienteNombre(),
				current.clienteEmail(), current.clienteTelefono(), current.checkIn(), current.checkOut(),
				current.pasajeros(), current.noches(), current.total(), current.depositoRequerido(), "CANCELADA");
		reservas.put(id, saved);
		return Optional.of(saved);
	}

	List<Pago> findPagos(Long reservaId) {
		return pagos.values().stream()
				.filter(pago -> reservaId.equals(pago.reservaId()))
				.sorted(Comparator.comparing(Pago::id))
				.toList();
	}

	Optional<Pago> createPago(Long reservaId, Pago input) {
		Reserva reserva = reservas.get(reservaId);
		if (reserva == null || "CANCELADA".equals(reserva.estado())) {
			return Optional.empty();
		}

		Pago saved = new Pago(pagoIds.incrementAndGet(), reservaId, moneyOr(input.monto()),
				textOr(input.metodo(), "TRANSFERENCIA"), textOr(input.estado(), "APROBADO"), LocalDateTime.now().toString());
		pagos.put(saved.id(), saved);

		if ("APROBADO".equals(saved.estado()) && totalPagadoAprobado(reservaId).compareTo(reserva.depositoRequerido()) >= 0) {
			confirmarReserva(reserva);
		}

		return Optional.of(saved);
	}

	private void confirmarReserva(Reserva reserva) {
		Reserva confirmed = new Reserva(reserva.id(), reserva.codigo(), reserva.domoId(), reserva.clienteNombre(),
				reserva.clienteEmail(), reserva.clienteTelefono(), reserva.checkIn(), reserva.checkOut(), reserva.pasajeros(),
				reserva.noches(), reserva.total(), reserva.depositoRequerido(), "CONFIRMADA");
		reservas.put(confirmed.id(), confirmed);
	}

	private BigDecimal totalPagadoAprobado(Long reservaId) {
		return pagos.values().stream()
				.filter(pago -> reservaId.equals(pago.reservaId()))
				.filter(pago -> "APROBADO".equals(pago.estado()))
				.map(Pago::monto)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private boolean isDisponible(Long domoId, LocalDate checkIn, LocalDate checkOut) {
		return reservas.values().stream()
				.filter(reserva -> domoId.equals(reserva.domoId()))
				.filter(reserva -> ESTADOS_QUE_BLOQUEAN.contains(reserva.estado()))
				.noneMatch(reserva -> overlaps(checkIn, checkOut, LocalDate.parse(reserva.checkIn()),
						LocalDate.parse(reserva.checkOut())));
	}

	private static boolean overlaps(LocalDate start, LocalDate end, LocalDate otherStart, LocalDate otherEnd) {
		return start.isBefore(otherEnd) && end.isAfter(otherStart);
	}

	private static LocalDate parseDateOrNull(String value) {
		try {
			return value == null || value.isBlank() ? null : LocalDate.parse(value);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	private static int numberOr(Integer value, int fallback) {
		return value == null ? fallback : value;
	}

	private static BigDecimal moneyOr(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private static String textOr(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
}

@RestController
@RequestMapping("/domos")
class DomoController {

	private final ReservasStore store;

	DomoController(ReservasStore store) {
		this.store = store;
	}

	@GetMapping
	List<Domo> all() {
		return store.findDomos();
	}

	@GetMapping("/{id}")
	ResponseEntity<Domo> find(@PathVariable Long id) {
		return ResponseEntity.of(store.findDomo(id));
	}

	@GetMapping("/{id}/detalle")
	ResponseEntity<DomoDetalle> detalle(@PathVariable Long id) {
		return ResponseEntity.of(store.findDomoDetalle(id));
	}

	@GetMapping("/disponibles")
	List<Domo> disponibles(
			@RequestParam(required = false) String checkIn,
			@RequestParam(required = false) String checkOut,
			@RequestParam(required = false) Integer pasajeros) {
		return store.findDisponibles(checkIn, checkOut, pasajeros);
	}

	@PostMapping
	ResponseEntity<Domo> create(@RequestBody Domo domo) {
		return ResponseEntity.status(HttpStatus.CREATED).body(store.createDomo(domo));
	}

	@PutMapping("/{id}")
	ResponseEntity<Domo> update(@PathVariable Long id, @RequestBody Domo domo) {
		return ResponseEntity.of(store.updateDomo(id, domo));
	}
}

@RestController
@RequestMapping("/reservas")
class ReservaController {

	private final ReservasStore store;

	ReservaController(ReservasStore store) {
		this.store = store;
	}

	@GetMapping
	List<Reserva> all() {
		return store.findReservas();
	}

	@GetMapping("/{id}")
	ResponseEntity<Reserva> find(@PathVariable Long id) {
		return ResponseEntity.of(store.findReserva(id));
	}

	@GetMapping("/codigo/{codigo}")
	ResponseEntity<Reserva> findByCodigo(@PathVariable String codigo) {
		return ResponseEntity.of(store.findReservaByCodigo(codigo));
	}

	@PostMapping
	ResponseEntity<?> create(@RequestBody Reserva reserva) {
		return store.createReserva(reserva)
				.<ResponseEntity<?>>map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "No se pudo crear la reserva")));
	}

	@PostMapping("/{id}/cancelar")
	ResponseEntity<Reserva> cancelar(@PathVariable Long id) {
		return ResponseEntity.of(store.cancelarReserva(id));
	}

	@GetMapping("/{id}/pagos")
	ResponseEntity<?> pagos(@PathVariable Long id) {
		return store.findReserva(id)
				.<ResponseEntity<?>>map(reserva -> ResponseEntity.ok(store.findPagos(id)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Reserva no encontrada")));
	}

	@PostMapping("/{id}/pagos")
	ResponseEntity<?> createPago(@PathVariable Long id, @RequestBody Pago pago) {
		return store.createPago(id, pago)
				.<ResponseEntity<?>>map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "No se pudo registrar el pago")));
	}
}

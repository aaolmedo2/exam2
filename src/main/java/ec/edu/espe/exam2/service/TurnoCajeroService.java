package ec.edu.espe.exam2.service;

import ec.edu.espe.exam2.dto.IniciarTurnoDto;
import ec.edu.espe.exam2.dto.TurnoCajeroDto;
import ec.edu.espe.exam2.dto.CerrarTurnoDto;
import ec.edu.espe.exam2.exception.CreateException;
import ec.edu.espe.exam2.exception.EntityNotFoundException;
import ec.edu.espe.exam2.exception.UpdateException;
import ec.edu.espe.exam2.mapper.TurnoCajeroMapper;
import ec.edu.espe.exam2.mapper.DenominacionMapper;
import ec.edu.espe.exam2.model.TurnoCajero;
import ec.edu.espe.exam2.repository.TurnoCajeroRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TurnoCajeroService {

    private final TurnoCajeroRepository turnoCajeroRepository;
    private final TurnoCajeroMapper turnoCajeroMapper;
    private final DenominacionMapper denominacionMapper;
    private final TransaccionTurnoService transaccionTurnoService;
    private final ValidacionService validacionService;

    public TurnoCajeroService(TurnoCajeroRepository turnoCajeroRepository,
            TurnoCajeroMapper turnoCajeroMapper,
            DenominacionMapper denominacionMapper,
            TransaccionTurnoService transaccionTurnoService,
            ValidacionService validacionService) {
        this.turnoCajeroRepository = turnoCajeroRepository;
        this.turnoCajeroMapper = turnoCajeroMapper;
        this.denominacionMapper = denominacionMapper;
        this.transaccionTurnoService = transaccionTurnoService;
        this.validacionService = validacionService;
    }

    public TurnoCajeroDto iniciarTurno(IniciarTurnoDto iniciarTurnoDto) throws CreateException {
        try {
            log.info("Iniciando turno para cajero: {} en caja: {}",
                    iniciarTurnoDto.getCodigoCajero(), iniciarTurnoDto.getCodigoCaja());

            // Validaciones
            if (!validacionService.validarCodigoCaja(iniciarTurnoDto.getCodigoCaja())) {
                throw new CreateException("Código de caja inválido");
            }

            if (!validacionService.validarCodigoCajero(iniciarTurnoDto.getCodigoCajero())) {
                throw new CreateException("Código de cajero inválido");
            }

            if (!validacionService.validarDenominaciones(iniciarTurnoDto.getDenominacionesIniciales())) {
                throw new CreateException("Denominaciones inválidas");
            }

            // Validar que no exista un turno abierto para el cajero
            if (existeTurnoAbierto(iniciarTurnoDto.getCodigoCajero())) {
                throw new CreateException("El cajero ya tiene un turno abierto");
            }

            // Generar código de turno
            String codigoTurno = generarCodigoTurno(iniciarTurnoDto.getCodigoCaja(), iniciarTurnoDto.getCodigoCajero());

            // Calcular monto inicial
            Double montoInicial = calcularMontoTotal(iniciarTurnoDto.getDenominacionesIniciales());

            // Crear turno
            TurnoCajero turno = new TurnoCajero();
            turno.setCodigoCaja(iniciarTurnoDto.getCodigoCaja());
            turno.setCodigoCajero(iniciarTurnoDto.getCodigoCajero());
            turno.setCodigoTurno(codigoTurno);
            turno.setInicioTurno(new Date());
            turno.setMontoInicial(montoInicial);
            turno.setDenominacionesIniciales(
                    denominacionMapper.toEntityList(iniciarTurnoDto.getDenominacionesIniciales()));
            turno.setEstado("ABIERTO");
            turno.setAlertaCierre(false);

            TurnoCajero turnoGuardado = turnoCajeroRepository.save(turno);

            log.info("Turno iniciado exitosamente con código: {}", codigoTurno);
            return turnoCajeroMapper.toDto(turnoGuardado);

        } catch (Exception e) {
            log.error("Error al iniciar turno: {}", e.getMessage());
            throw new CreateException("Error al iniciar turno: " + e.getMessage());
        }
    }

    public TurnoCajeroDto cerrarTurno(CerrarTurnoDto cerrarTurnoDto) throws EntityNotFoundException, UpdateException {
        try {
            log.info("Cerrando turno con código: {}", cerrarTurnoDto.getCodigoTurno());

            // Validaciones
            if (!validacionService.validarCodigoTurno(cerrarTurnoDto.getCodigoTurno())) {
                throw new UpdateException("Código de turno inválido");
            }

            if (!validacionService.validarDenominaciones(cerrarTurnoDto.getDenominacionesFinales())) {
                throw new UpdateException("Denominaciones finales inválidas");
            }

            // Buscar turno
            Optional<TurnoCajero> turnoOpt = turnoCajeroRepository.findByCodigoTurno(cerrarTurnoDto.getCodigoTurno());
            if (!turnoOpt.isPresent()) {
                throw new EntityNotFoundException("Turno no encontrado con código: " + cerrarTurnoDto.getCodigoTurno());
            }

            TurnoCajero turno = turnoOpt.get();

            // Validar que el turno esté abierto
            if (!"ABIERTO".equals(turno.getEstado())) {
                throw new UpdateException("El turno ya está cerrado");
            }

            // Calcular monto final declarado
            Double montoFinalDeclarado = calcularMontoTotal(cerrarTurnoDto.getDenominacionesFinales());

            // Calcular monto teórico basado en transacciones
            Double montoTeorico = calcularMontoTeorico(turno);

            // Verificar diferencias
            Double diferencia = montoFinalDeclarado - montoTeorico;
            boolean hayAlerta = Math.abs(diferencia) > 0.01; // Tolerancia de 1 centavo

            // Actualizar turno
            turno.setFinTurno(new Date());
            turno.setMontoFinal(montoFinalDeclarado);
            turno.setDenominacionesFinales(denominacionMapper.toEntityList(cerrarTurnoDto.getDenominacionesFinales()));
            turno.setEstado("CERRADO");
            turno.setAlertaCierre(hayAlerta);
            turno.setDiferenciaCierre(diferencia);

            TurnoCajero turnoActualizado = turnoCajeroRepository.save(turno);

            if (hayAlerta) {
                log.warn("Turno cerrado con alerta. Diferencia: ${}", diferencia);
            } else {
                log.info("Turno cerrado exitosamente sin diferencias");
            }

            return turnoCajeroMapper.toDto(turnoActualizado);

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al cerrar turno: {}", e.getMessage());
            throw new UpdateException("Error al cerrar turno: " + e.getMessage());
        }
    }

    public TurnoCajeroDto obtenerTurnoPorCodigo(String codigoTurno) throws EntityNotFoundException {
        log.info("Buscando turno con código: {}", codigoTurno);

        Optional<TurnoCajero> turnoOpt = turnoCajeroRepository.findByCodigoTurno(codigoTurno);
        if (!turnoOpt.isPresent()) {
            throw new EntityNotFoundException("Turno no encontrado con código: " + codigoTurno);
        }

        return turnoCajeroMapper.toDto(turnoOpt.get());
    }

    public List<TurnoCajeroDto> obtenerTurnosPorCajero(String codigoCajero) {
        log.info("Obteniendo turnos para cajero: {}", codigoCajero);

        List<TurnoCajero> turnos = turnoCajeroRepository.findByCodigoCajeroAndEstado(codigoCajero, "ABIERTO");
        turnos.addAll(turnoCajeroRepository.findByCodigoCajeroAndEstado(codigoCajero, "CERRADO"));

        return turnos.stream()
                .map(turnoCajeroMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TurnoCajeroDto> obtenerTurnosPorCaja(String codigoCaja) {
        log.info("Obteniendo turnos para caja: {}", codigoCaja);

        List<TurnoCajero> turnos = turnoCajeroRepository.findByCodigoCajaAndEstado(codigoCaja, "ABIERTO");
        turnos.addAll(turnoCajeroRepository.findByCodigoCajaAndEstado(codigoCaja, "CERRADO"));

        return turnos.stream()
                .map(turnoCajeroMapper::toDto)
                .collect(Collectors.toList());
    }

    public boolean existeTurnoAbierto(String codigoCajero) {
        return turnoCajeroRepository.findFirstByCodigoCajeroAndEstado(codigoCajero, "ABIERTO").isPresent();
    }

    public String generarCodigoTurno(String codigoCaja, String codigoCajero) {
        LocalDate fecha = LocalDate.now();
        String fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return codigoCaja + "-" + codigoCajero + "-" + fechaFormateada;
    }

    private Double calcularMontoTotal(List<ec.edu.espe.exam2.dto.DenominacionDto> denominaciones) {
        if (denominaciones == null || denominaciones.isEmpty()) {
            return 0.0;
        }

        return denominaciones.stream()
                .mapToDouble(d -> d.getBillete() * d.getCantidad())
                .sum();
    }

    private Double calcularMontoTeorico(TurnoCajero turno) {
        // Monto inicial
        Double montoTeorico = turno.getMontoInicial();

        // Sumar/restar transacciones
        Double montoTransacciones = transaccionTurnoService.calcularMontoTotalTurno(turno.getCodigoTurno());
        montoTeorico += montoTransacciones;

        return montoTeorico;
    }

    public TurnoCajeroDto buscarTurnoActivo(String codigoCaja, String codigoCajero) throws EntityNotFoundException {
        log.info("Buscando turno activo para caja: {} y cajero: {}", codigoCaja, codigoCajero);

        List<TurnoCajero> turnosActivos = turnoCajeroRepository.findByCodigoCajaAndCodigoCajeroAndEstado(
                codigoCaja, codigoCajero, "ABIERTO");

        if (turnosActivos.isEmpty()) {
            throw new EntityNotFoundException(
                    "No hay turno activo para la caja " + codigoCaja + " y cajero " + codigoCajero);
        }

        if (turnosActivos.size() > 1) {
            log.warn("Se encontraron múltiples turnos activos para caja: {} y cajero: {}", codigoCaja, codigoCajero);
        }

        return turnoCajeroMapper.toDto(turnosActivos.get(0));
    }
}

package ec.edu.espe.exam2.service;

import ec.edu.espe.exam2.dto.ProcesarTransaccionDto;
import ec.edu.espe.exam2.dto.TransaccionTurnoDto;
import ec.edu.espe.exam2.exception.CreateException;
import ec.edu.espe.exam2.exception.EntityNotFoundException;
import ec.edu.espe.exam2.mapper.TransaccionTurnoMapper;
import ec.edu.espe.exam2.mapper.DenominacionMapper;
import ec.edu.espe.exam2.model.TransaccionTurno;
import ec.edu.espe.exam2.model.TurnoCajero;
import ec.edu.espe.exam2.repository.TransaccionTurnoRepository;
import ec.edu.espe.exam2.repository.TurnoCajeroRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransaccionTurnoService {

    private final TransaccionTurnoRepository transaccionTurnoRepository;
    private final TurnoCajeroRepository turnoCajeroRepository;
    private final TransaccionTurnoMapper transaccionTurnoMapper;
    private final DenominacionMapper denominacionMapper;
    private final ValidacionService validacionService;

    public TransaccionTurnoService(TransaccionTurnoRepository transaccionTurnoRepository,
            TurnoCajeroRepository turnoCajeroRepository,
            TransaccionTurnoMapper transaccionTurnoMapper,
            DenominacionMapper denominacionMapper,
            ValidacionService validacionService) {
        this.transaccionTurnoRepository = transaccionTurnoRepository;
        this.turnoCajeroRepository = turnoCajeroRepository;
        this.transaccionTurnoMapper = transaccionTurnoMapper;
        this.denominacionMapper = denominacionMapper;
        this.validacionService = validacionService;
    }

    public TransaccionTurnoDto procesarTransaccion(ProcesarTransaccionDto procesarTransaccionDto, String codigoTurno)
            throws CreateException, EntityNotFoundException {
        try {
            log.info("Procesando transacción tipo: {} para turno: {}",
                    procesarTransaccionDto.getTipoTransaccion(), codigoTurno);

            // Validaciones
            if (!validacionService.validarDenominaciones(procesarTransaccionDto.getDenominaciones())) {
                throw new CreateException("Denominaciones inválidas");
            }

            if (!validacionService.validarTipoTransaccion(procesarTransaccionDto.getTipoTransaccion())) {
                throw new CreateException("Tipo de transacción inválido");
            }

            // Verificar que el turno exista y esté abierto
            Optional<TurnoCajero> turnoOpt = turnoCajeroRepository.findByCodigoTurno(codigoTurno);
            if (!turnoOpt.isPresent()) {
                throw new EntityNotFoundException("Turno no encontrado con código: " + codigoTurno);
            }

            TurnoCajero turno = turnoOpt.get();
            if (!"ABIERTO".equals(turno.getEstado())) {
                throw new CreateException("El turno no está abierto");
            }

            // Calcular monto si no se proporciona
            Double montoTotal = procesarTransaccionDto.getMontoTotal();
            if (montoTotal == null) {
                montoTotal = calcularMontoTotal(procesarTransaccionDto.getDenominaciones());
            }

            // Ajustar monto según tipo de transacción
            Double montoAjustado = ajustarMontoPorTipo(montoTotal, procesarTransaccionDto.getTipoTransaccion());

            // Crear transacción
            TransaccionTurno transaccion = new TransaccionTurno();
            transaccion.setCodigoCaja(turno.getCodigoCaja());
            transaccion.setCodigoCajero(turno.getCodigoCajero());
            transaccion.setCodigoTurno(codigoTurno);
            transaccion.setTipoTransaccion(procesarTransaccionDto.getTipoTransaccion());
            transaccion.setMontoTotal(montoAjustado);
            transaccion.setDenominaciones(denominacionMapper.toEntityList(procesarTransaccionDto.getDenominaciones()));
            transaccion.setFechaTransaccion(new Date());

            TransaccionTurno transaccionGuardada = transaccionTurnoRepository.save(transaccion);

            log.info("Transacción guardada exitosamente con ID: {}", transaccionGuardada.getId());
            return transaccionTurnoMapper.toDto(transaccionGuardada);

        } catch (Exception e) {
            log.error("Error al procesar transacción: {}", e.getMessage());
            throw e;
        }
    }

    public List<TransaccionTurnoDto> obtenerTransaccionesPorTurno(String codigoTurno) {
        log.info("Obteniendo transacciones para turno: {}", codigoTurno);

        List<TransaccionTurno> transacciones = transaccionTurnoRepository
                .findByCodigoTurnoOrderByFechaTransaccionAsc(codigoTurno);

        return transacciones.stream()
                .map(transaccionTurnoMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TransaccionTurnoDto> obtenerTransaccionesPorCajero(String codigoCajero) {
        log.info("Obteniendo transacciones para cajero: {}", codigoCajero);

        List<TransaccionTurno> transacciones = transaccionTurnoRepository
                .findByCodigoCajeroOrderByFechaTransaccionDesc(codigoCajero);

        return transacciones.stream()
                .map(transaccionTurnoMapper::toDto)
                .collect(Collectors.toList());
    }

    public Double calcularMontoTotalTurno(String codigoTurno) {
        log.info("Calculando monto total para turno: {}", codigoTurno);

        List<TransaccionTurno> transacciones = transaccionTurnoRepository
                .findByCodigoTurnoOrderByFechaTransaccionAsc(codigoTurno);

        return transacciones.stream()
                .filter(t -> !"INICIO".equals(t.getTipoTransaccion()) && !"CIERRE".equals(t.getTipoTransaccion()))
                .mapToDouble(TransaccionTurno::getMontoTotal)
                .sum();
    }

    public List<TransaccionTurnoDto> obtenerTransaccionesPorTurnoYTipo(String codigoTurno, String tipoTransaccion) {
        log.info("Obteniendo transacciones tipo {} para turno: {}", tipoTransaccion, codigoTurno);

        List<TransaccionTurno> transacciones = transaccionTurnoRepository
                .findByCodigoTurnoAndTipoTransaccion(codigoTurno, tipoTransaccion);

        return transacciones.stream()
                .map(transaccionTurnoMapper::toDto)
                .collect(Collectors.toList());
    }

    public long contarTransaccionesPorTurno(String codigoTurno) {
        log.info("Contando transacciones para turno: {}", codigoTurno);
        return transaccionTurnoRepository.countByCodigoTurno(codigoTurno);
    }

    private Double calcularMontoTotal(List<ec.edu.espe.exam2.dto.DenominacionDto> denominaciones) {
        if (denominaciones == null || denominaciones.isEmpty()) {
            return 0.0;
        }

        return denominaciones.stream()
                .mapToDouble(d -> d.getBillete() * d.getCantidad())
                .sum();
    }

    private Double ajustarMontoPorTipo(Double monto, String tipoTransaccion) {
        // Para retiros, el monto es negativo (sale dinero de la caja)
        if ("RETIRO".equals(tipoTransaccion)) {
            return -Math.abs(monto);
        }
        // Para depósitos, el monto es positivo (entra dinero a la caja)
        else if ("DEPOSITO".equals(tipoTransaccion)) {
            return Math.abs(monto);
        }
        // Para inicio y cierre, el monto se mantiene como está
        else {
            return monto;
        }
    }
}

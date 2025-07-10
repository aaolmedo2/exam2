package ec.edu.espe.exam2.service;

import ec.edu.espe.exam2.dto.*;
import ec.edu.espe.exam2.exception.CreateException;
import ec.edu.espe.exam2.exception.EntityNotFoundException;
import ec.edu.espe.exam2.exception.UpdateException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VentanillaBancariaService {

    private final TurnoCajeroService turnoCajeroService;
    private final TransaccionTurnoService transaccionTurnoService;
    private final ValidacionService validacionService;

    public VentanillaBancariaService(TurnoCajeroService turnoCajeroService,
            TransaccionTurnoService transaccionTurnoService,
            ValidacionService validacionService) {
        this.turnoCajeroService = turnoCajeroService;
        this.transaccionTurnoService = transaccionTurnoService;
        this.validacionService = validacionService;
    }

    public ResponseDto<TurnoCajeroDto> iniciarTurno(IniciarTurnoDto iniciarTurnoDto) throws CreateException {
        try {
            log.info("Iniciando proceso de apertura de turno para cajero: {}", iniciarTurnoDto.getCodigoCajero());

            // Iniciar turno
            TurnoCajeroDto turno = turnoCajeroService.iniciarTurno(iniciarTurnoDto);

            // Crear transacción de inicio
            ProcesarTransaccionDto transaccionInicio = new ProcesarTransaccionDto();
            transaccionInicio.setCodigoTurno(turno.getCodigoTurno());
            transaccionInicio.setTipoTransaccion("INICIO");
            transaccionInicio.setDenominaciones(iniciarTurnoDto.getDenominacionesIniciales());
            transaccionInicio.setMontoTotal(iniciarTurnoDto.getMontoInicial());

            transaccionTurnoService.procesarTransaccion(transaccionInicio);

            log.info("Turno iniciado exitosamente: {}", turno.getCodigoTurno());
            return ResponseDto.success("Turno iniciado exitosamente", turno);

        } catch (Exception e) {
            log.error("Error al iniciar turno: {}", e.getMessage());
            throw new CreateException("Error al iniciar turno: " + e.getMessage());
        }
    }

    public ResponseDto<TransaccionTurnoDto> procesarTransaccion(ProcesarTransaccionDto procesarTransaccionDto)
            throws CreateException, EntityNotFoundException {
        try {
            log.info("Procesando transacción tipo: {} para turno: {}",
                    procesarTransaccionDto.getTipoTransaccion(), procesarTransaccionDto.getCodigoTurno());

            TransaccionTurnoDto transaccion = transaccionTurnoService.procesarTransaccion(procesarTransaccionDto);

            log.info("Transacción procesada exitosamente con ID: {}", transaccion.getId());
            return ResponseDto.success("Transacción procesada exitosamente", transaccion);

        } catch (Exception e) {
            log.error("Error al procesar transacción: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseDto<TurnoCajeroDto> cerrarTurno(CerrarTurnoDto cerrarTurnoDto)
            throws EntityNotFoundException, UpdateException {
        try {
            log.info("Iniciando proceso de cierre de turno: {}", cerrarTurnoDto.getCodigoTurno());

            // Crear transacción de cierre
            ProcesarTransaccionDto transaccionCierre = new ProcesarTransaccionDto();
            transaccionCierre.setCodigoTurno(cerrarTurnoDto.getCodigoTurno());
            transaccionCierre.setTipoTransaccion("CIERRE");
            transaccionCierre.setDenominaciones(cerrarTurnoDto.getDenominacionesFinales());
            transaccionCierre.setMontoTotal(cerrarTurnoDto.getMontoFinal());

            transaccionTurnoService.procesarTransaccion(transaccionCierre);

            // Cerrar turno
            TurnoCajeroDto turno = turnoCajeroService.cerrarTurno(cerrarTurnoDto);

            String mensaje = turno.getAlertaCierre()
                    ? "Turno cerrado con alerta de diferencia: $" + turno.getDiferenciaCierre()
                    : "Turno cerrado exitosamente";

            log.info("Turno cerrado: {} - {}", turno.getCodigoTurno(), mensaje);
            return ResponseDto.success(mensaje, turno);

        } catch (Exception e) {
            log.error("Error al cerrar turno: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseDto<ResumenTurnoDto> obtenerResumenTurno(String codigoTurno) throws EntityNotFoundException {
        try {
            log.info("Obteniendo resumen del turno: {}", codigoTurno);

            // Obtener turno
            TurnoCajeroDto turno = turnoCajeroService.obtenerTurnoPorCodigo(codigoTurno);

            // Obtener transacciones
            List<TransaccionTurnoDto> transacciones = transaccionTurnoService.obtenerTransaccionesPorTurno(codigoTurno);

            // Calcular monto teórico
            Double montoTeorico = turno.getMontoInicial() +
                    transaccionTurnoService.calcularMontoTotalTurno(codigoTurno);

            // Crear resumen
            ResumenTurnoDto resumen = new ResumenTurnoDto();
            resumen.setTurno(turno);
            resumen.setTransacciones(transacciones);
            resumen.setMontoTeorico(montoTeorico);
            resumen.setMontoDeclarado(turno.getMontoFinal());
            resumen.setDiferencia(turno.getDiferenciaCierre());
            resumen.setTotalTransacciones(transacciones.size());
            resumen.setEstadoTurno(turno.getEstado());

            log.info("Resumen generado para turno: {}", codigoTurno);
            return ResponseDto.success("Resumen obtenido exitosamente", resumen);

        } catch (Exception e) {
            log.error("Error al obtener resumen del turno: {}", e.getMessage());
            throw e;
        }
    }

    public ResponseDto<EstadoCajeroDto> obtenerEstadoCajero(String codigoCajero) {
        try {
            log.info("Obteniendo estado del cajero: {}", codigoCajero);

            // Verificar si tiene turno abierto
            boolean tieneTurnoAbierto = turnoCajeroService.existeTurnoAbierto(codigoCajero);
            TurnoCajeroDto turnoActivo = null;

            if (tieneTurnoAbierto) {
                List<TurnoCajeroDto> turnos = turnoCajeroService.obtenerTurnosPorCajero(codigoCajero);
                turnoActivo = turnos.stream()
                        .filter(t -> "ABIERTO".equals(t.getEstado()))
                        .findFirst()
                        .orElse(null);
            }

            // Obtener transacciones del cajero
            List<TransaccionTurnoDto> transacciones = transaccionTurnoService
                    .obtenerTransaccionesPorCajero(codigoCajero);

            // Crear estado
            EstadoCajeroDto estado = new EstadoCajeroDto();
            estado.setCodigoCajero(codigoCajero);
            estado.setTieneTurnoAbierto(tieneTurnoAbierto);
            estado.setTurnoActivo(turnoActivo);
            estado.setTotalTransaccionesHoy(transacciones.size());
            estado.setUltimaActividad(
                    transacciones.isEmpty() ? "Sin actividad" : transacciones.get(0).getFechaTransaccion().toString());

            log.info("Estado obtenido para cajero: {}", codigoCajero);
            return ResponseDto.success("Estado obtenido exitosamente", estado);

        } catch (Exception e) {
            log.error("Error al obtener estado del cajero: {}", e.getMessage());
            return ResponseDto.error("Error al obtener estado del cajero: " + e.getMessage());
        }
    }

    public ResponseDto<List<Integer>> obtenerDenominacionesValidas() {
        try {
            log.info("Obteniendo denominaciones válidas");

            List<Integer> denominaciones = validacionService.obtenerDenominacionesValidas();

            return ResponseDto.success("Denominaciones obtenidas exitosamente", denominaciones);

        } catch (Exception e) {
            log.error("Error al obtener denominaciones válidas: {}", e.getMessage());
            return ResponseDto.error("Error al obtener denominaciones válidas: " + e.getMessage());
        }
    }
}

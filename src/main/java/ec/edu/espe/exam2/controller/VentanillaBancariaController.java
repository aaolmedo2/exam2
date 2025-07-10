package ec.edu.espe.exam2.controller;

import ec.edu.espe.exam2.dto.*;
import ec.edu.espe.exam2.exception.CreateException;
import ec.edu.espe.exam2.exception.EntityNotFoundException;
import ec.edu.espe.exam2.exception.UpdateException;
import ec.edu.espe.exam2.service.VentanillaBancariaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ventanilla-bancaria")
@Slf4j
@Validated
@Tag(name = "Ventanilla Bancaria", description = "API para gestión de efectivo en ventanillas bancarias - Tres operaciones principales: iniciar turno, procesar transacción y finalizar turno")
public class VentanillaBancariaController {

    private final VentanillaBancariaService ventanillaBancariaService;

    public VentanillaBancariaController(VentanillaBancariaService ventanillaBancariaService) {
        this.ventanillaBancariaService = ventanillaBancariaService;
    }

    @Operation(summary = "Iniciar turno de cajero", description = "Inicia un nuevo turno para un cajero registrando el dinero recibido de la bóveda del banco, especificando la cantidad de billetes de cada denominación. Genera código de turno automático con formato CAJ##-USU##-YYYYMMDD")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turno iniciado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Turno iniciado exitosamente",
                        "data": {
                            "codigoTurno": "CAJ01-USU01-20250709",
                            "codigoCaja": "CAJ01",
                            "codigoCajero": "USU01",
                            "fechaInicio": "2025-07-09T08:00:00Z",
                            "fechaFin": null,
                            "montoInicial": 1000.00,
                            "estado": "ABIERTO"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El cajero ya tiene un turno activo"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/iniciar-turno")
    public ResponseEntity<ResponseDto<TurnoCajeroDto>> iniciarTurno(
            @Parameter(description = "Datos para iniciar el turno con denominaciones iniciales", required = true) @Valid @RequestBody IniciarTurnoDto iniciarTurnoDto) {

        try {
            log.info("Solicitud para iniciar turno: cajero {} en caja {}",
                    iniciarTurnoDto.getCodigoCajero(), iniciarTurnoDto.getCodigoCaja());
            ResponseDto<TurnoCajeroDto> response = ventanillaBancariaService.iniciarTurno(iniciarTurnoDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CreateException e) {
            log.error("Error al iniciar turno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al iniciar turno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error("Error interno del servidor"));
        }
    }

    @Operation(summary = "Procesar transacción", description = "Procesa una transacción de depósito (+) o retiro (-) registrando cuántos billetes y de qué denominación recibe o entrega el cajero")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacción procesada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Transacción procesada exitosamente",
                        "data": {
                            "codigoTransaccion": "TXN-001",
                            "codigoTurno": "CAJ01-USU01-20250709",
                            "tipoTransaccion": "DEPOSITO",
                            "montoTotal": 500.00,
                            "fechaTransaccion": "2025-07-09T10:30:00Z",
                            "denominaciones": [
                                {"valor": 100, "cantidad": 3},
                                {"valor": 50, "cantidad": 4}
                            ]
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/procesar-transaccion")
    public ResponseEntity<ResponseDto<TransaccionTurnoDto>> procesarTransaccion(
            @Parameter(description = "Datos de la transacción con denominaciones recibidas/entregadas", required = true) @Valid @RequestBody ProcesarTransaccionDto procesarTransaccionDto) {

        try {
            log.info("Solicitud para procesar transacción {} en caja: {} y cajero: {}",
                    procesarTransaccionDto.getTipoTransaccion(), procesarTransaccionDto.getCodigoCaja(),
                    procesarTransaccionDto.getCodigoCajero());
            ResponseDto<TransaccionTurnoDto> response = ventanillaBancariaService
                    .procesarTransaccion(procesarTransaccionDto);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Turno no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (UpdateException e) {
            log.error("Error al procesar transacción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al procesar transacción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error("Error interno del servidor"));
        }
    }

    @Operation(summary = "Finalizar turno", description = "Cierra el turno del cajero ingresando la cantidad de billetes de cada denominación que tiene. La aplicación compara el valor total con la cantidad calculada según las transacciones del día y genera una alerta si hay diferencias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno finalizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Turno cerrado exitosamente",
                        "data": {
                            "codigoTurno": "CAJ01-USU01-20250709",
                            "codigoCajero": "USU01",
                            "fechaInicio": "2025-07-09T08:00:00Z",
                            "fechaFin": "2025-07-09T16:00:00Z",
                            "montoInicial": 1000.00,
                            "montoFinal": 1300.00,
                            "estado": "CERRADO",
                            "alertaCierre": false,
                            "diferenciaCierre": 0.00
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/cerrar-turno")
    public ResponseEntity<ResponseDto<TurnoCajeroDto>> cerrarTurno(
            @Parameter(description = "Datos para cerrar el turno con denominaciones finales", required = true) @Valid @RequestBody CerrarTurnoDto cerrarTurnoDto) {

        try {
            log.info("Solicitud para cerrar turno: {}", cerrarTurnoDto.getCodigoTurno());
            ResponseDto<TurnoCajeroDto> response = ventanillaBancariaService.cerrarTurno(cerrarTurnoDto);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Turno no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (UpdateException e) {
            log.error("Error al cerrar turno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al cerrar turno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error("Error interno del servidor"));
        }
    }
}

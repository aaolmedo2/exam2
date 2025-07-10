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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventanilla-bancaria")
@Slf4j
@Validated
@Tag(name = "Ventanilla Bancaria", description = "API para gestión de efectivo en ventanillas bancarias")
public class VentanillaBancariaController {

    private final VentanillaBancariaService ventanillaBancariaService;

    public VentanillaBancariaController(VentanillaBancariaService ventanillaBancariaService) {
        this.ventanillaBancariaService = ventanillaBancariaService;
    }

    @Operation(summary = "Iniciar turno de cajero", description = "Inicia un nuevo turno para un cajero con las denominaciones y monto inicial especificados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turno iniciado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Turno iniciado exitosamente",
                        "data": {
                            "codigoTurno": "CAJ01-USU01-20250709",
                            "codigoCaja": "CAJ01",
                            "codigoCajero": "USU01",
                            "fechaInicio": "2024-01-15T08:00:00Z",
                            "fechaFin": null,
                            "montoInicial": 1000.00,
                            "montoFinal": 1000.00,
                            "estado": "ABIERTO"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "El cajero ya tiene un turno activo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
    })
    @PostMapping("/iniciar-turno")
    public ResponseEntity<ResponseDto<TurnoCajeroDto>> iniciarTurno(
            @Parameter(description = "Datos para iniciar el turno", required = true) @Valid @RequestBody IniciarTurnoDto iniciarTurnoDto) {

        try {
            log.info("Solicitud para iniciar turno: {}", iniciarTurnoDto.getCodigoCajero());
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

    @Operation(summary = "Procesar transacción", description = "Procesa una transacción de efectivo (depósito o retiro) en el turno especificado")
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
                            "fechaTransaccion": "2024-01-15T10:30:00Z",
                            "denominaciones": [
                                {"valor": 100.00, "cantidad": 3},
                                {"valor": 50.00, "cantidad": 4}
                            ]
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
    })
    @PostMapping("/procesar-transaccion")
    public ResponseEntity<ResponseDto<TransaccionTurnoDto>> procesarTransaccion(
            @Parameter(description = "Datos de la transacción a procesar", required = true) @Valid @RequestBody ProcesarTransaccionDto procesarTransaccionDto) {

        try {
            log.info("Solicitud para procesar transacción: {}", procesarTransaccionDto.getCodigoTurno());
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

    @Operation(summary = "Cerrar turno de cajero", description = "Cierra el turno activo del cajero especificado y genera el resumen final")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno cerrado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Turno cerrado exitosamente",
                        "data": {
                            "codigoTurno": "TURN-001",
                            "codigoCajero": "CAJ001",
                            "fechaInicio": "2024-01-15T08:00:00Z",
                            "fechaFin": "2024-01-15T16:00:00Z",
                            "montoInicial": 1000.00,
                            "montoFinal": 1500.00,
                            "estado": "CERRADO"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
    })
    @PostMapping("/cerrar-turno")
    public ResponseEntity<ResponseDto<TurnoCajeroDto>> cerrarTurno(
            @Parameter(description = "Datos para cerrar el turno", required = true) @Valid @RequestBody CerrarTurnoDto cerrarTurnoDto) {

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

    @Operation(summary = "Obtener resumen del turno", description = "Obtiene el resumen completo de un turno específico incluyendo todas las transacciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Resumen obtenido exitosamente",
                        "data": {
                            "turno": {
                                "codigoTurno": "TURN-001",
                                "codigoCajero": "CAJ001",
                                "fechaInicio": "2024-01-15T08:00:00Z",
                                "fechaFin": "2024-01-15T16:00:00Z",
                                "montoInicial": 1000.00,
                                "montoFinal": 1500.00,
                                "estado": "CERRADO"
                            },
                            "transacciones": [
                                {
                                    "codigoTransaccion": "TXN-001",
                                    "tipoTransaccion": "DEPOSITO",
                                    "montoTotal": 500.00,
                                    "fechaTransaccion": "2024-01-15T10:30:00Z"
                                }
                            ],
                            "totalTransacciones": 1,
                            "totalDepositos": 500.00,
                            "totalRetiros": 0.00,
                            "diferencia": 500.00
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
    })
    @GetMapping("/resumen-turno/{codigoTurno}")
    public ResponseEntity<ResponseDto<ResumenTurnoDto>> obtenerResumenTurno(
            @Parameter(description = "Código del turno a consultar", required = true) @PathVariable @NotBlank @Pattern(regexp = "^CAJ\\d{2}-USU\\d{2}-\\d{8}$", message = "Código de turno inválido") String codigoTurno) {

        try {
            log.info("Solicitud para obtener resumen del turno: {}", codigoTurno);
            ResponseDto<ResumenTurnoDto> response = ventanillaBancariaService.obtenerResumenTurno(codigoTurno);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Turno no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al obtener resumen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error("Error interno del servidor"));
        }
    }

    @Operation(summary = "Obtener estado del cajero", description = "Obtiene el estado actual del cajero (turno activo si existe)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado obtenido exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Estado obtenido exitosamente",
                        "data": {
                            "codigoCajero": "CAJ001",
                            "tieneturnoActivo": true,
                            "turnoActivo": {
                                "codigoTurno": "TURN-001",
                                "fechaInicio": "2024-01-15T08:00:00Z",
                                "montoInicial": 1000.00,
                                "montoFinal": 1500.00,
                                "estado": "ACTIVO"
                            },
                            "totalTransacciones": 5,
                            "totalDepositos": 800.00,
                            "totalRetiros": 300.00
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Cajero no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
    })
    @GetMapping("/estado-cajero/{codigoCajero}")
    public ResponseEntity<ResponseDto<EstadoCajeroDto>> obtenerEstadoCajero(
            @Parameter(description = "Código del cajero a consultar", required = true) @PathVariable @NotBlank @Pattern(regexp = "^USU\\d{2}$", message = "Código de cajero inválido") String codigoCajero) {

        try {
            log.info("Solicitud para obtener estado del cajero: {}", codigoCajero);
            ResponseDto<EstadoCajeroDto> response = ventanillaBancariaService.obtenerEstadoCajero(codigoCajero);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Cajero no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al obtener estado del cajero: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error("Error interno del servidor"));
        }
    }

    @Operation(summary = "Obtener denominaciones válidas", description = "Obtiene la lista de denominaciones válidas para transacciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Denominaciones obtenidas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Denominaciones obtenidas exitosamente",
                        "data": [100, 50, 20, 10, 5, 1]
                    }
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class)))
    })
    @GetMapping("/denominaciones")
    public ResponseEntity<ResponseDto<List<Integer>>> obtenerDenominaciones() {
        try {
            log.info("Solicitud para obtener denominaciones válidas");
            ResponseDto<List<Integer>> response = ventanillaBancariaService.obtenerDenominacionesValidas();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error inesperado al obtener denominaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.error("Error interno del servidor"));
        }
    }
}

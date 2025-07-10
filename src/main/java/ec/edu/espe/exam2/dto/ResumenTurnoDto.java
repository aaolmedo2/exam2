package ec.edu.espe.exam2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenTurnoDto {
    private TurnoCajeroDto turno;
    private List<TransaccionTurnoDto> transacciones;
    private Double montoTeorico;
    private Double montoDeclarado;
    private Double diferencia;
    private Integer totalTransacciones;
    private String estadoTurno;
}

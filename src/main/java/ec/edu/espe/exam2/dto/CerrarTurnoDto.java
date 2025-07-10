package ec.edu.espe.exam2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CerrarTurnoDto {
    private String codigoTurno;
    private List<DenominacionDto> denominacionesFinales;
    private Double montoFinal;
}

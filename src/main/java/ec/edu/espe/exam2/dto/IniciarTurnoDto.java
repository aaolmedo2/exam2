package ec.edu.espe.exam2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IniciarTurnoDto {
    private String codigoCaja;
    private String codigoCajero;
    private List<DenominacionDto> denominacionesIniciales;
    private Double montoInicial;
}

package ec.edu.espe.exam2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoCajeroDto {
    private String id;
    private String codigoCaja;
    private String codigoCajero;
    private String codigoTurno;
    private Date inicioTurno;
    private Double montoInicial;
    private List<DenominacionDto> denominacionesIniciales;
    private Date finTurno;
    private Double montoFinal;
    private List<DenominacionDto> denominacionesFinales;
    private String estado;
    private Boolean alertaCierre;
    private Double diferenciaCierre;
}

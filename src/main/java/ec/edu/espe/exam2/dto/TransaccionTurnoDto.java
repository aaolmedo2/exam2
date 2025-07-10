package ec.edu.espe.exam2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionTurnoDto {
    private String id;
    private String codigoCaja;
    private String codigoCajero;
    private String codigoTurno;
    private String tipoTransaccion;
    private Double montoTotal;
    private List<DenominacionDto> denominaciones;
    private Date fechaTransaccion;
}

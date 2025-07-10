package ec.edu.espe.exam2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarTransaccionDto {
    private String codigoCaja;
    private String codigoCajero;
    private String tipoTransaccion; // "RETIRO", "DEPOSITO"
    private List<DenominacionDto> denominaciones;
    private Double montoTotal;
}

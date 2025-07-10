package ec.edu.espe.exam2.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(collection = "transacciones_turno")
public class TransaccionTurno {

    @Id
    private String id;

    private String codigoCaja;
    private String codigoCajero;
    private String codigoTurno;

    private String tipoTransaccion; // "INICIO", "RETIRO", "DEPOSITO", "CIERRE"
    private Double montoTotal;
    private List<Denominacion> denominaciones;
    private Date fechaTransaccion;

    public TransaccionTurno(String codigoCaja, String codigoCajero, String codigoTurno, 
                           String tipoTransaccion, Double montoTotal, List<Denominacion> denominaciones) {
        this.codigoCaja = codigoCaja;
        this.codigoCajero = codigoCajero;
        this.codigoTurno = codigoTurno;
        this.tipoTransaccion = tipoTransaccion;
        this.montoTotal = montoTotal;
        this.denominaciones = denominaciones;
        this.fechaTransaccion = new Date();
    }
}

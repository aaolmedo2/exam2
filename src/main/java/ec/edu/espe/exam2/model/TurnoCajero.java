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
@Document(collection = "turnos_cajero")
public class TurnoCajero {

    @Id
    private String id;

    private String codigoCaja;
    private String codigoCajero;
    private String codigoTurno; // Formato: CAJ01-USE01-20250709

    private Date inicioTurno;
    private Double montoInicial;
    private List<Denominacion> denominacionesIniciales;

    private Date finTurno;
    private Double montoFinal;
    private List<Denominacion> denominacionesFinales;

    private String estado; // "ABIERTO" o "CERRADO"

    private Boolean alertaCierre = false;
    private Double diferenciaCierre;

}

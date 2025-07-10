package ec.edu.espe.exam2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Denominacion {
    private Integer billete; // 1, 5, 10, 20, 50, 100
    private Integer cantidad;
    private Double monto;

    public Double getSubtotal() {
        return billete * cantidad * 1.0;
    }

    public Denominacion(Integer billete, Integer cantidad) {
        this.billete = billete;
        this.cantidad = cantidad;
        this.monto = billete * cantidad * 1.0;
    }
}

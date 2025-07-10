package ec.edu.espe.exam2.service;

import ec.edu.espe.exam2.dto.DenominacionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ValidacionService {

    private static final List<Integer> DENOMINACIONES_VALIDAS = Arrays.asList(1, 5, 10, 20, 50, 100);
    private static final Pattern CODIGO_CAJA_PATTERN = Pattern.compile("^CAJ\\d{2}$");
    private static final Pattern CODIGO_CAJERO_PATTERN = Pattern.compile("^USU\\d{2}$");
    private static final Pattern CODIGO_TURNO_PATTERN = Pattern.compile("^CAJ\\d{2}-USU\\d{2}-\\d{8}$");

    public boolean validarDenominaciones(List<DenominacionDto> denominaciones) {
        if (denominaciones == null || denominaciones.isEmpty()) {
            log.warn("Lista de denominaciones vacía o nula");
            return false;
        }

        for (DenominacionDto denominacion : denominaciones) {
            if (!validarDenominacion(denominacion)) {
                return false;
            }
        }

        return true;
    }

    public boolean validarCodigoCaja(String codigoCaja) {
        if (codigoCaja == null || codigoCaja.trim().isEmpty()) {
            log.warn("Código de caja vacío o nulo");
            return false;
        }

        boolean esValido = CODIGO_CAJA_PATTERN.matcher(codigoCaja).matches();
        if (!esValido) {
            log.warn("Código de caja inválido: {}. Debe seguir el formato CAJ##", codigoCaja);
        }

        return esValido;
    }

    public boolean validarCodigoCajero(String codigoCajero) {
        if (codigoCajero == null || codigoCajero.trim().isEmpty()) {
            log.warn("Código de cajero vacío o nulo");
            return false;
        }

        boolean esValido = CODIGO_CAJERO_PATTERN.matcher(codigoCajero).matches();
        if (!esValido) {
            log.warn("Código de cajero inválido: {}. Debe seguir el formato USU##", codigoCajero);
        }

        return esValido;
    }

    public boolean validarCodigoTurno(String codigoTurno) {
        if (codigoTurno == null || codigoTurno.trim().isEmpty()) {
            log.warn("Código de turno vacío o nulo");
            return false;
        }

        boolean esValido = CODIGO_TURNO_PATTERN.matcher(codigoTurno).matches();
        if (!esValido) {
            log.warn("Código de turno inválido: {}. Debe seguir el formato CAJ##-USE##-YYYYMMDD", codigoTurno);
        }

        return esValido;
    }

    public List<Integer> obtenerDenominacionesValidas() {
        return DENOMINACIONES_VALIDAS;
    }

    private boolean validarDenominacion(DenominacionDto denominacion) {
        if (denominacion == null) {
            log.warn("Denominación nula");
            return false;
        }

        if (denominacion.getBillete() == null || denominacion.getCantidad() == null) {
            log.warn("Billete o cantidad nulos en denominación");
            return false;
        }

        if (!DENOMINACIONES_VALIDAS.contains(denominacion.getBillete())) {
            log.warn("Denominación de billete inválida: {}. Valores válidos: {}",
                    denominacion.getBillete(), DENOMINACIONES_VALIDAS);
            return false;
        }

        if (denominacion.getCantidad() < 0) {
            log.warn("Cantidad de billetes no puede ser negativa: {}", denominacion.getCantidad());
            return false;
        }

        return true;
    }

    public boolean validarTipoTransaccion(String tipoTransaccion) {
        if (tipoTransaccion == null || tipoTransaccion.trim().isEmpty()) {
            log.warn("Tipo de transacción vacío o nulo");
            return false;
        }

        List<String> tiposValidos = Arrays.asList("INICIO", "DEPOSITO", "RETIRO", "CIERRE");
        boolean esValido = tiposValidos.contains(tipoTransaccion.toUpperCase());

        if (!esValido) {
            log.warn("Tipo de transacción inválido: {}. Valores válidos: {}", tipoTransaccion, tiposValidos);
        }

        return esValido;
    }
}

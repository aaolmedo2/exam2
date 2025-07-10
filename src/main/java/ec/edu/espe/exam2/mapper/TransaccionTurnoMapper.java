package ec.edu.espe.exam2.mapper;

import ec.edu.espe.exam2.dto.TransaccionTurnoDto;
import ec.edu.espe.exam2.model.TransaccionTurno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransaccionTurnoMapper {

    @Autowired
    private DenominacionMapper denominacionMapper;

    public TransaccionTurnoDto toDto(TransaccionTurno transaccionTurno) {
        if (transaccionTurno == null) {
            return null;
        }

        TransaccionTurnoDto dto = new TransaccionTurnoDto();
        dto.setId(transaccionTurno.getId());
        dto.setCodigoCaja(transaccionTurno.getCodigoCaja());
        dto.setCodigoCajero(transaccionTurno.getCodigoCajero());
        dto.setCodigoTurno(transaccionTurno.getCodigoTurno());
        dto.setTipoTransaccion(transaccionTurno.getTipoTransaccion());
        dto.setMontoTotal(transaccionTurno.getMontoTotal());
        dto.setDenominaciones(denominacionMapper.toDtoList(transaccionTurno.getDenominaciones()));
        dto.setFechaTransaccion(transaccionTurno.getFechaTransaccion());

        return dto;
    }

    public TransaccionTurno toEntity(TransaccionTurnoDto transaccionTurnoDto) {
        if (transaccionTurnoDto == null) {
            return null;
        }

        TransaccionTurno entity = new TransaccionTurno();
        entity.setId(transaccionTurnoDto.getId());
        entity.setCodigoCaja(transaccionTurnoDto.getCodigoCaja());
        entity.setCodigoCajero(transaccionTurnoDto.getCodigoCajero());
        entity.setCodigoTurno(transaccionTurnoDto.getCodigoTurno());
        entity.setTipoTransaccion(transaccionTurnoDto.getTipoTransaccion());
        entity.setMontoTotal(transaccionTurnoDto.getMontoTotal());
        entity.setDenominaciones(denominacionMapper.toEntityList(transaccionTurnoDto.getDenominaciones()));
        entity.setFechaTransaccion(transaccionTurnoDto.getFechaTransaccion());

        return entity;
    }
}

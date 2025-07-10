package ec.edu.espe.exam2.mapper;

import ec.edu.espe.exam2.dto.TurnoCajeroDto;
import ec.edu.espe.exam2.model.TurnoCajero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TurnoCajeroMapper {

    @Autowired
    private DenominacionMapper denominacionMapper;

    public TurnoCajeroDto toDto(TurnoCajero turnoCajero) {
        if (turnoCajero == null) {
            return null;
        }

        TurnoCajeroDto dto = new TurnoCajeroDto();
        dto.setId(turnoCajero.getId());
        dto.setCodigoCaja(turnoCajero.getCodigoCaja());
        dto.setCodigoCajero(turnoCajero.getCodigoCajero());
        dto.setCodigoTurno(turnoCajero.getCodigoTurno());
        dto.setInicioTurno(turnoCajero.getInicioTurno());
        dto.setMontoInicial(turnoCajero.getMontoInicial());
        dto.setDenominacionesIniciales(denominacionMapper.toDtoList(turnoCajero.getDenominacionesIniciales()));
        dto.setFinTurno(turnoCajero.getFinTurno());
        dto.setMontoFinal(turnoCajero.getMontoFinal());
        dto.setDenominacionesFinales(denominacionMapper.toDtoList(turnoCajero.getDenominacionesFinales()));
        dto.setEstado(turnoCajero.getEstado());
        dto.setAlertaCierre(turnoCajero.getAlertaCierre());
        dto.setDiferenciaCierre(turnoCajero.getDiferenciaCierre());

        return dto;
    }

    public TurnoCajero toEntity(TurnoCajeroDto turnoCajeroDto) {
        if (turnoCajeroDto == null) {
            return null;
        }

        TurnoCajero entity = new TurnoCajero();
        entity.setId(turnoCajeroDto.getId());
        entity.setCodigoCaja(turnoCajeroDto.getCodigoCaja());
        entity.setCodigoCajero(turnoCajeroDto.getCodigoCajero());
        entity.setCodigoTurno(turnoCajeroDto.getCodigoTurno());
        entity.setInicioTurno(turnoCajeroDto.getInicioTurno());
        entity.setMontoInicial(turnoCajeroDto.getMontoInicial());
        entity.setDenominacionesIniciales(denominacionMapper.toEntityList(turnoCajeroDto.getDenominacionesIniciales()));
        entity.setFinTurno(turnoCajeroDto.getFinTurno());
        entity.setMontoFinal(turnoCajeroDto.getMontoFinal());
        entity.setDenominacionesFinales(denominacionMapper.toEntityList(turnoCajeroDto.getDenominacionesFinales()));
        entity.setEstado(turnoCajeroDto.getEstado());
        entity.setAlertaCierre(turnoCajeroDto.getAlertaCierre());
        entity.setDiferenciaCierre(turnoCajeroDto.getDiferenciaCierre());

        return entity;
    }
}

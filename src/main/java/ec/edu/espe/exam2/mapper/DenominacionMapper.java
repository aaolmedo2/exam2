package ec.edu.espe.exam2.mapper;

import ec.edu.espe.exam2.dto.DenominacionDto;
import ec.edu.espe.exam2.model.Denominacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DenominacionMapper {

    public DenominacionDto toDto(Denominacion denominacion) {
        if (denominacion == null) {
            return null;
        }
        return new DenominacionDto(
                denominacion.getBillete(),
                denominacion.getCantidad(),
                denominacion.getMonto());
    }

    public Denominacion toEntity(DenominacionDto denominacionDto) {
        if (denominacionDto == null) {
            return null;
        }
        return new Denominacion(
                denominacionDto.getBillete(),
                denominacionDto.getCantidad(),
                denominacionDto.getMonto());
    }

    public List<DenominacionDto> toDtoList(List<Denominacion> denominaciones) {
        if (denominaciones == null) {
            return null;
        }
        return denominaciones.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Denominacion> toEntityList(List<DenominacionDto> denominacionesDto) {
        if (denominacionesDto == null) {
            return null;
        }
        return denominacionesDto.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

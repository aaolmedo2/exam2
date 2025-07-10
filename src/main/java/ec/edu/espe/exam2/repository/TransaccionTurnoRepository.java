package ec.edu.espe.exam2.repository;

import ec.edu.espe.exam2.model.TransaccionTurno;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionTurnoRepository extends MongoRepository<TransaccionTurno, String> {

    List<TransaccionTurno> findByCodigoTurnoOrderByFechaTransaccionAsc(String codigoTurno);

    List<TransaccionTurno> findByCodigoCajeroOrderByFechaTransaccionDesc(String codigoCajero);

    List<TransaccionTurno> findByCodigoTurnoAndTipoTransaccion(String codigoTurno, String tipoTransaccion);

    long countByCodigoTurno(String codigoTurno);
}

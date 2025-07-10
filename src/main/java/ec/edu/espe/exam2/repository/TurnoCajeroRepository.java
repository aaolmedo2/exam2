package ec.edu.espe.exam2.repository;

import ec.edu.espe.exam2.model.TurnoCajero;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TurnoCajeroRepository extends MongoRepository<TurnoCajero, String> {

    Optional<TurnoCajero> findByCodigoTurno(String codigoTurno);

    List<TurnoCajero> findByCodigoCajeroAndEstado(String codigoCajero, String estado);

    List<TurnoCajero> findByCodigoCajaAndEstado(String codigoCaja, String estado);

    boolean existsByCodigoTurnoAndEstado(String codigoTurno, String estado);

    Optional<TurnoCajero> findFirstByCodigoCajeroAndEstado(String codigoCajero, String estado);
}

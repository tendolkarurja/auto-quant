package auto_quant.portfolio.repository;
import org.springframework.stereotype.Repository;
import java.util.*;
import auto_quant.portfolio.model.Holding;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
@Repository
public class HoldingRepositoryCustomImpl implements HoldingRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;
    public List<Holding> findByUserId(Long userId) {
        String query = "SELECT h FROM Holding h WHERE h.userId = :userId";
        return entityManager.createQuery(query, Holding.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}

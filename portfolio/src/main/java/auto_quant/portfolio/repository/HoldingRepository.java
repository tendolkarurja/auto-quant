package auto_quant.portfolio.repository;

import auto_quant.portfolio.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long>, HoldingRepositoryCustom {
    // All custom methods are defined in HoldingRepositoryCustom
    List<Holding> findByUserId(Long userId);
}
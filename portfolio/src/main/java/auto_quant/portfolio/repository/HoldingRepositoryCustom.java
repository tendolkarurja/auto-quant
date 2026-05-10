package auto_quant.portfolio.repository;

import org.springframework.stereotype.Repository;
import java.util.*;
import auto_quant.portfolio.model.Holding;

@Repository
public interface HoldingRepositoryCustom{
    public List<Holding> findByUserId(Long userId);
}
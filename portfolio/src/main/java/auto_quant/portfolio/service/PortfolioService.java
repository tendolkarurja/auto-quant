package auto_quant.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import auto_quant.portfolio.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import auto_quant.portfolio.model.Holding;
import java.util.List;

@Service
public class PortfolioService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired   
    private HoldingRepository holdingRepository;

    public List<Holding> getHoldingsByUserId(Long userId) { 
        return holdingRepository.findByUserId(userId);    
    }

    public Holding saveHolding(Holding holding) {
        return holdingRepository.save(holding);
    }

    public Holding updateHolding(Long holdingId, Holding holding) {
        // Implement logic to update holding based on holdingId       
        Holding existingHolding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + holdingId));
        existingHolding.setQuantity(holding.getQuantity());
        existingHolding.setAveragePrice(holding.getAveragePrice());        
        
        // 3. Save the "Managed" entity
        return holdingRepository.save(existingHolding);
    }

    public void deleteHolding(Long holdingId) {
        holdingRepository.deleteById(holdingId);
    }
}

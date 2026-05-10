package auto_quant.portfolio.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import auto_quant.portfolio.service.PortfolioService;
import auto_quant.portfolio.model.Holding;

@RestController
@RequestMapping("/api/v1/holdings")
public class HoldingController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Holdings API is working!");
    }

    // 1. Get Portfolio for a User
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Holding>> getUserHoldings(@PathVariable Long userId) throws Exception {
        List<Holding> holdings = portfolioService.getHoldingsByUserId(userId);

        if (holdings.isEmpty()) {
            throw new Exception("No holdings found for user ID: " + userId);        }
        return ResponseEntity.ok(holdings);
    }

    // 2. Add a new Holding
    @PostMapping("/")
    public ResponseEntity<Holding> createHolding(@RequestBody Holding holding) {
        Holding savedHolding = portfolioService.saveHolding(holding);
        return new ResponseEntity<>(savedHolding, HttpStatus.CREATED);
    }

    @PatchMapping("/{holdingId}")
    public ResponseEntity<Holding> updateHolding(@PathVariable Long holdingId, @RequestBody Holding holding) {
        Holding updatedHolding = portfolioService.updateHolding(holdingId, holding);
        return ResponseEntity.ok(updatedHolding);
    }

    @DeleteMapping("/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long holdingId) {
        portfolioService.deleteHolding(holdingId);
        return ResponseEntity.noContent().build();
    }
}

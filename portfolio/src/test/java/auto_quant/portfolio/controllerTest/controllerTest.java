package auto_quant.portfolio.controllerTest;
import auto_quant.portfolio.controller.*;

import auto_quant.portfolio.model.Holding;
import auto_quant.portfolio.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingControllerTest {

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private HoldingController holdingController;

    private Holding sampleHolding;

    @BeforeEach
    void setUp() {
        sampleHolding = new Holding();
        sampleHolding.setId(1L);
        sampleHolding.setUserId(100L);
        sampleHolding.setSymbol("AAPL");
        sampleHolding.setQuantity(10);
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/holdings/
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Test endpoint returns 200 OK with message")
    void testEndpoint_returnsOk() {
        ResponseEntity<String> response = holdingController.testEndpoint();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Holdings API is working!", response.getBody());
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/holdings/user/{userId}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Get holdings: valid userId with results returns 200 OK")
    void getUserHoldings_validUserId_returnsHoldings() throws Exception {
        List<Holding> holdings = Arrays.asList(sampleHolding);
        when(portfolioService.getHoldingsByUserId(100L)).thenReturn(holdings);

        ResponseEntity<List<Holding>> response = holdingController.getUserHoldings(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("AAPL", response.getBody().get(0).getSymbol());
        verify(portfolioService, times(1)).getHoldingsByUserId(100L);
    }

    @Test
    @DisplayName("Get holdings: userId with multiple holdings returns full list")
    void getUserHoldings_multipleHoldings_returnsAll() throws Exception {
        Holding h2 = new Holding();
        h2.setId(2L);
        h2.setUserId(100L);
        h2.setSymbol("TSLA");

        List<Holding> holdings = Arrays.asList(sampleHolding, h2);
        when(portfolioService.getHoldingsByUserId(100L)).thenReturn(holdings);

        ResponseEntity<List<Holding>> response = holdingController.getUserHoldings(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Get holdings: empty list throws Exception (no holdings found)")
    void getUserHoldings_emptyList_throwsException() {
        when(portfolioService.getHoldingsByUserId(999L)).thenReturn(Collections.emptyList());

        Exception ex = assertThrows(Exception.class,
                () -> holdingController.getUserHoldings(999L));

        assertEquals("No holdings found for user ID: 999", ex.getMessage());
        verify(portfolioService, times(1)).getHoldingsByUserId(999L);
    }

    @Test
    @DisplayName("Get holdings: null userId causes NullPointerException from service")
    void getUserHoldings_nullUserId_throwsNullPointerException() {
        when(portfolioService.getHoldingsByUserId(null))
                .thenThrow(new NullPointerException("userId must not be null"));

        assertThrows(NullPointerException.class,
                () -> holdingController.getUserHoldings(null));
    }

    @Test
    @DisplayName("Get holdings: service throws RuntimeException propagates")
    void getUserHoldings_serviceThrowsRuntimeException_propagates() {
        when(portfolioService.getHoldingsByUserId(100L))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class,
                () -> holdingController.getUserHoldings(100L));
    }

    @Test
    @DisplayName("Get holdings: negative userId is treated as valid by service, returns empty")
    void getUserHoldings_negativeUserId_throwsException() {
        when(portfolioService.getHoldingsByUserId(-1L)).thenReturn(Collections.emptyList());

        Exception ex = assertThrows(Exception.class,
                () -> holdingController.getUserHoldings(-1L));

        assertTrue(ex.getMessage().contains("-1"));
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/holdings/
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Create holding: valid holding returns 201 CREATED")
    void createHolding_validHolding_returns201() {
        when(portfolioService.saveHolding(any(Holding.class))).thenReturn(sampleHolding);

        ResponseEntity<Holding> response = holdingController.createHolding(sampleHolding);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(portfolioService, times(1)).saveHolding(sampleHolding);
    }

    @Test
    @DisplayName("Create holding: null body causes NullPointerException from service")
    void createHolding_nullBody_throwsNullPointerException() {
        when(portfolioService.saveHolding(null))
                .thenThrow(new NullPointerException("Holding body must not be null"));

        assertThrows(NullPointerException.class,
                () -> holdingController.createHolding(null));
    }

    @Test
    @DisplayName("Create holding: holding with missing required fields saved by service (validation is service concern)")
    void createHolding_missingFields_serviceDeterminesOutcome() {
        Holding emptyHolding = new Holding(); // no fields set
        when(portfolioService.saveHolding(emptyHolding)).thenReturn(emptyHolding);

        ResponseEntity<Holding> response = holdingController.createHolding(emptyHolding);

        // Controller simply delegates; 201 returned if service succeeds
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Create holding: service throws exception propagates to caller")
    void createHolding_serviceThrows_propagates() {
        when(portfolioService.saveHolding(any(Holding.class)))
                .thenThrow(new RuntimeException("Constraint violation"));

        assertThrows(RuntimeException.class,
                () -> holdingController.createHolding(sampleHolding));
    }

    @Test
    @DisplayName("Create holding: duplicate holding save returns 201 if service allows it")
    void createHolding_duplicate_serviceDecides() {
        Holding duplicate = new Holding();
        duplicate.setId(99L);
        when(portfolioService.saveHolding(sampleHolding)).thenReturn(duplicate);

        ResponseEntity<Holding> response = holdingController.createHolding(sampleHolding);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(99L, response.getBody().getId());
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/holdings/{holdingId}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Update holding: valid ID and body returns 200 OK")
    void updateHolding_validIdAndBody_returns200() {
        Holding updated = new Holding();
        updated.setId(1L);
        updated.setSymbol("MSFT");

        when(portfolioService.updateHolding(eq(1L), any(Holding.class))).thenReturn(updated);

        ResponseEntity<Holding> response = holdingController.updateHolding(1L, sampleHolding);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("MSFT", response.getBody().getSymbol());
        verify(portfolioService, times(1)).updateHolding(1L, sampleHolding);
    }

    @Test
    @DisplayName("Update holding: non-existent ID throws exception from service")
    void updateHolding_nonExistentId_throwsException() {
        when(portfolioService.updateHolding(eq(999L), any(Holding.class)))
                .thenThrow(new RuntimeException("Holding not found with ID: 999"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> holdingController.updateHolding(999L, sampleHolding));

        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("Update holding: null body throws NullPointerException")
    void updateHolding_nullBody_throwsNullPointerException() {
        when(portfolioService.updateHolding(eq(1L), eq(null)))
                .thenThrow(new NullPointerException("Request body must not be null"));

        assertThrows(NullPointerException.class,
                () -> holdingController.updateHolding(1L, null));
    }

    @Test
    @DisplayName("Update holding: null holdingId throws NullPointerException")
    void updateHolding_nullHoldingId_throwsNullPointerException() {
        when(portfolioService.updateHolding(eq(null), any(Holding.class)))
                .thenThrow(new NullPointerException("holdingId must not be null"));

        assertThrows(NullPointerException.class,
                () -> holdingController.updateHolding(null, sampleHolding));
    }

    @Test
    @DisplayName("Update holding: negative ID is passed to service as-is")
    void updateHolding_negativeId_serviceReceivesIt() {
        when(portfolioService.updateHolding(eq(-5L), any(Holding.class)))
                .thenThrow(new IllegalArgumentException("Invalid holdingId: -5"));

        assertThrows(IllegalArgumentException.class,
                () -> holdingController.updateHolding(-5L, sampleHolding));
    }

    @Test
    @DisplayName("Update holding: empty body holding updates with defaults")
    void updateHolding_emptyHoldingBody_serviceHandlesIt() {
        Holding emptyBody = new Holding();
        when(portfolioService.updateHolding(eq(1L), eq(emptyBody))).thenReturn(emptyBody);

        ResponseEntity<Holding> response = holdingController.updateHolding(1L, emptyBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/holdings/{holdingId}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Delete holding: valid ID returns 204 NO CONTENT")
    void deleteHolding_validId_returns204() {
        doNothing().when(portfolioService).deleteHolding(1L);

        ResponseEntity<Void> response = holdingController.deleteHolding(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(portfolioService, times(1)).deleteHolding(1L);
    }

    @Test
    @DisplayName("Delete holding: non-existent ID throws exception from service")
    void deleteHolding_nonExistentId_throwsException() {
        doThrow(new RuntimeException("Holding not found with ID: 999"))
                .when(portfolioService).deleteHolding(999L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> holdingController.deleteHolding(999L));

        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("Delete holding: null ID throws NullPointerException")
    void deleteHolding_nullId_throwsNullPointerException() {
        doThrow(new NullPointerException("holdingId must not be null"))
                .when(portfolioService).deleteHolding(null);

        assertThrows(NullPointerException.class,
                () -> holdingController.deleteHolding(null));
    }

    @Test
    @DisplayName("Delete holding: negative ID throws IllegalArgumentException")
    void deleteHolding_negativeId_throwsException() {
        doThrow(new IllegalArgumentException("Invalid holdingId: -1"))
                .when(portfolioService).deleteHolding(-1L);

        assertThrows(IllegalArgumentException.class,
                () -> holdingController.deleteHolding(-1L));
    }

    @Test
    @DisplayName("Delete holding: service throws DB exception propagates")
    void deleteHolding_dbException_propagates() {
        doThrow(new RuntimeException("DB error during delete"))
                .when(portfolioService).deleteHolding(1L);

        assertThrows(RuntimeException.class,
                () -> holdingController.deleteHolding(1L));
        verify(portfolioService, times(1)).deleteHolding(1L);
    }
}
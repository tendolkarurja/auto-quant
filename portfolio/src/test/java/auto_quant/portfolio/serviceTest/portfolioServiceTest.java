package auto_quant.portfolio.serviceTest;
import auto_quant.portfolio.service.PortfolioService;

import auto_quant.portfolio.model.Holding;
import auto_quant.portfolio.repository.HoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private Holding existingHolding;
    private Holding updatePayload;

    @BeforeEach
    void setUp() {
        existingHolding = new Holding();
        existingHolding.setId(1L);
        existingHolding.setUserId(100L);
        existingHolding.setQuantity(10);
        existingHolding.setAveragePrice(150.00);

        updatePayload = new Holding();
        updatePayload.setQuantity(25);
        updatePayload.setAveragePrice(175.00);
    }

    // ─────────────────────────────────────────────
    // getHoldingsByUserId
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getHoldingsByUserId: valid userId returns list of holdings")
    void getHoldingsByUserId_validUserId_returnsList() {
        List<Holding> expected = Arrays.asList(existingHolding);
        when(holdingRepository.findByUserId(100L)).thenReturn(expected);

        List<Holding> result = portfolioService.getHoldingsByUserId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0));
        verify(holdingRepository, times(1)).findByUserId(100L);
    }

    @Test
    @DisplayName("getHoldingsByUserId: userId with multiple holdings returns all")
    void getHoldingsByUserId_multipleHoldings_returnsAll() {
        Holding h2 = new Holding();
        h2.setId(2L);

        when(holdingRepository.findByUserId(100L)).thenReturn(Arrays.asList(existingHolding, h2));

        List<Holding> result = portfolioService.getHoldingsByUserId(100L);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getHoldingsByUserId: userId with no holdings returns empty list")
    void getHoldingsByUserId_noHoldings_returnsEmptyList() {
        when(holdingRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        List<Holding> result = portfolioService.getHoldingsByUserId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getHoldingsByUserId: null userId — repo throws NullPointerException")
    void getHoldingsByUserId_nullUserId_throwsNullPointerException() {
        when(holdingRepository.findByUserId(null))
                .thenThrow(new NullPointerException("userId must not be null"));

        assertThrows(NullPointerException.class,
                () -> portfolioService.getHoldingsByUserId(null));
    }

    @Test
    @DisplayName("getHoldingsByUserId: negative userId is passed through to repo")
    void getHoldingsByUserId_negativeUserId_delegatesToRepo() {
        when(holdingRepository.findByUserId(-1L)).thenReturn(Collections.emptyList());

        List<Holding> result = portfolioService.getHoldingsByUserId(-1L);

        assertTrue(result.isEmpty());
        verify(holdingRepository).findByUserId(-1L);
    }

    @Test
    @DisplayName("getHoldingsByUserId: repo throws RuntimeException — propagates")
    void getHoldingsByUserId_repoThrows_propagates() {
        when(holdingRepository.findByUserId(100L))
                .thenThrow(new RuntimeException("DB connection lost"));

        assertThrows(RuntimeException.class,
                () -> portfolioService.getHoldingsByUserId(100L));
    }

    // ─────────────────────────────────────────────
    // saveHolding
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("saveHolding: valid holding is saved and returned")
    void saveHolding_validHolding_returnsSavedHolding() {
        when(holdingRepository.save(existingHolding)).thenReturn(existingHolding);

        Holding result = portfolioService.saveHolding(existingHolding);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AAPL", result);
        verify(holdingRepository, times(1)).save(existingHolding);
    }

    @Test
    @DisplayName("saveHolding: holding with only required fields is saved")
    void saveHolding_minimalHolding_saved() {
        Holding minimal = new Holding();
        minimal.setUserId(200L);
        minimal.setQuantity(5);
        minimal.setAveragePrice(250.00);
        when(holdingRepository.save(minimal)).thenReturn(minimal);

        Holding result = portfolioService.saveHolding(minimal);

        assertEquals("MSFT", result);
    }

    @Test
    @DisplayName("saveHolding: null holding — repo throws NullPointerException")
    void saveHolding_nullHolding_throwsNullPointerException() {
        when(holdingRepository.save(null))
                .thenThrow(new NullPointerException("Holding must not be null"));

        assertThrows(NullPointerException.class,
                () -> portfolioService.saveHolding(null));
    }

    @Test
    @DisplayName("saveHolding: empty Holding object is passed to repo as-is")
    void saveHolding_emptyHolding_delegatesToRepo() {
        Holding empty = new Holding();
        when(holdingRepository.save(empty)).thenReturn(empty);

        Holding result = portfolioService.saveHolding(empty);

        assertNotNull(result);
        verify(holdingRepository).save(empty);
    }

    @Test
    @DisplayName("saveHolding: repo throws constraint violation — propagates")
    void saveHolding_repoThrowsConstraintViolation_propagates() {
        when(holdingRepository.save(any(Holding.class)))
                .thenThrow(new RuntimeException("Unique constraint violated"));

        assertThrows(RuntimeException.class,
                () -> portfolioService.saveHolding(existingHolding));
    }

    // ─────────────────────────────────────────────
    // updateHolding
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("updateHolding: existing holding is fetched, fields updated, and saved")
    void updateHolding_existingId_updatesAndSaves() {
        when(holdingRepository.findById(1L)).thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(existingHolding)).thenReturn(existingHolding);

        Holding result = portfolioService.updateHolding(1L, updatePayload);

        // Fields from updatePayload are applied to existingHolding
        assertEquals(25, result.getQuantity());
        assertEquals(175.00, result.getAveragePrice());

        // Symbol is NOT overwritten (service doesn't map it)
        verify(holdingRepository).findById(1L);
        verify(holdingRepository).save(existingHolding);
    }

    @Test
    @DisplayName("updateHolding: non-existent holdingId throws RuntimeException")
    void updateHolding_holdingNotFound_throwsRuntimeException() {
        when(holdingRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> portfolioService.updateHolding(999L, updatePayload));

        assertEquals("Holding not found with ID: 999", ex.getMessage());
        verify(holdingRepository).findById(999L);
        verify(holdingRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateHolding: null holdingId — repo throws IllegalArgumentException")
    void updateHolding_nullHoldingId_throwsException() {
        when(holdingRepository.findById(null))
                .thenThrow(new IllegalArgumentException("ID must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.updateHolding(null, updatePayload));

        verify(holdingRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateHolding: update payload with zero quantity — zero is applied")
    void updateHolding_zeroQuantityInPayload_appliesZero() {
        updatePayload.setQuantity(0);
        updatePayload.setAveragePrice(0.0);

        when(holdingRepository.findById(1L)).thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(existingHolding)).thenReturn(existingHolding);

        Holding result = portfolioService.updateHolding(1L, updatePayload);

        assertEquals(0, result.getQuantity());
        assertEquals(0.0, result.getAveragePrice());
    }

    @Test
    @DisplayName("updateHolding: update payload with negative values — negative is applied (no validation in service)")
    void updateHolding_negativeValuesInPayload_appliedAsIs() {
        updatePayload.setQuantity(-5);
        updatePayload.setAveragePrice(-100.00);

        when(holdingRepository.findById(1L)).thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(existingHolding)).thenReturn(existingHolding);

        Holding result = portfolioService.updateHolding(1L, updatePayload);

        // Documents that service has no guard against negative values
        assertEquals(-5, result.getQuantity());
        assertEquals(-100.00, result.getAveragePrice());
    }

    @Test
    @DisplayName("updateHolding: null payload — NullPointerException when accessing fields")
    void updateHolding_nullPayload_throwsNullPointerException() {
        when(holdingRepository.findById(1L)).thenReturn(Optional.of(existingHolding));

        // service calls holding.getQuantity() on null payload → NPE
        assertThrows(NullPointerException.class,
                () -> portfolioService.updateHolding(1L, null));

        verify(holdingRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateHolding: save after update throws — exception propagates, no silent swallow")
    void updateHolding_saveThrows_propagates() {
        when(holdingRepository.findById(1L)).thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(existingHolding))
                .thenThrow(new RuntimeException("DB write failed"));

        assertThrows(RuntimeException.class,
                () -> portfolioService.updateHolding(1L, updatePayload));
    }

    @Test
    @DisplayName("updateHolding: findById called once, save called once — no extra DB trips")
    void updateHolding_interactionCount_exactlyOneEach() {
        when(holdingRepository.findById(1L)).thenReturn(Optional.of(existingHolding));
        when(holdingRepository.save(existingHolding)).thenReturn(existingHolding);

        portfolioService.updateHolding(1L, updatePayload);

        verify(holdingRepository, times(1)).findById(1L);
        verify(holdingRepository, times(1)).save(existingHolding);
        verifyNoMoreInteractions(holdingRepository);
    }

    // ─────────────────────────────────────────────
    // deleteHolding
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("deleteHolding: valid holdingId — deleteById called once")
    void deleteHolding_validId_deletesSuccessfully() {
        doNothing().when(holdingRepository).deleteById(1L);

        portfolioService.deleteHolding(1L);

        verify(holdingRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteHolding: non-existent holdingId — repo throws, propagates")
    void deleteHolding_nonExistentId_throwsException() {
        doThrow(new RuntimeException("Holding not found with ID: 999"))
                .when(holdingRepository).deleteById(999L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> portfolioService.deleteHolding(999L));

        assertTrue(ex.getMessage().contains("999"));
        verify(holdingRepository).deleteById(999L);
    }

    @Test
    @DisplayName("deleteHolding: null holdingId — repo throws IllegalArgumentException")
    void deleteHolding_nullId_throwsIllegalArgumentException() {
        doThrow(new IllegalArgumentException("ID must not be null"))
                .when(holdingRepository).deleteById(null);

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.deleteHolding(null));
    }

    @Test
    @DisplayName("deleteHolding: negative holdingId is passed through to repo")
    void deleteHolding_negativeId_delegatesToRepo() {
        doThrow(new IllegalArgumentException("Invalid ID: -1"))
                .when(holdingRepository).deleteById(-1L);

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.deleteHolding(-1L));

        verify(holdingRepository).deleteById(-1L);
    }

    @Test
    @DisplayName("deleteHolding: no extra repo interactions beyond deleteById")
    void deleteHolding_noExtraInteractions() {
        doNothing().when(holdingRepository).deleteById(1L);

        portfolioService.deleteHolding(1L);

        verifyNoMoreInteractions(holdingRepository);
    }
}
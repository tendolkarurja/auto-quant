package auto_quant.portfolio.repositoryTest;

import auto_quant.portfolio.model.Holding;
import auto_quant.portfolio.repository.HoldingRepositoryCustomImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingRepositoryCustomImplTest {

    @Mock
    private EntityManager entityManager;

    // TypedQuery is generic, so we suppress the unchecked warning
    @SuppressWarnings("unchecked")
    @Mock
    private TypedQuery<Holding> typedQuery;

    @InjectMocks
    private HoldingRepositoryCustomImpl holdingRepository;

    private static final String EXPECTED_QUERY =
            "SELECT h FROM Holding h WHERE h.userId = :userId";

    private Holding sampleHolding;

    @BeforeEach
    void setUp() {
        sampleHolding = new Holding();
        sampleHolding.setId(1L);
        sampleHolding.setUserId(100L);
        sampleHolding.setQuantity(10);
    }

    // ─────────────────────────────────────────────
    // findByUserId — Happy paths
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId: valid userId returns list of holdings")
    void findByUserId_validUserId_returnsHoldings() {
        List<Holding> expected = Arrays.asList(sampleHolding);

        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 100L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<Holding> result = holdingRepository.findByUserId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleHolding, result.get(0));

        verify(entityManager, times(1)).createQuery(EXPECTED_QUERY, Holding.class);
        verify(typedQuery, times(1)).setParameter("userId", 100L);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    @DisplayName("findByUserId: valid userId with multiple holdings returns all")
    void findByUserId_multipleHoldings_returnsAll() {
        Holding h2 = new Holding();
        h2.setId(2L);
        h2.setUserId(100L);
        h2.setQuantity(20); 
        List<Holding> expected = Arrays.asList(sampleHolding, h2);

        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 100L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<Holding> result = holdingRepository.findByUserId(100L);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByUserId: userId with no holdings returns empty list")
    void findByUserId_noHoldings_returnsEmptyList() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 999L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Holding> result = holdingRepository.findByUserId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────
    // findByUserId — Edge cases: null / invalid inputs
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId: null userId causes IllegalArgumentException from EntityManager")
    void findByUserId_nullUserId_throwsException() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", (Long) null))
                .thenThrow(new IllegalArgumentException("Parameter value must not be null"));

        assertThrows(IllegalArgumentException.class,
                () -> holdingRepository.findByUserId(null));

        verify(typedQuery, times(1)).setParameter("userId", (Long) null);
        verify(typedQuery, never()).getResultList();
    }

    @Test
    @DisplayName("findByUserId: negative userId is passed to JPA as-is")
    void findByUserId_negativeUserId_passedToJpa() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", -1L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Holding> result = holdingRepository.findByUserId(-1L);

        // Repo doesn't validate sign — returns whatever JPA gives back
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(typedQuery, times(1)).setParameter("userId", -1L);
    }

    @Test
    @DisplayName("findByUserId: zero userId is passed to JPA as-is")
    void findByUserId_zeroUserId_passedToJpa() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 0L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Holding> result = holdingRepository.findByUserId(0L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUserId: very large (Long.MAX_VALUE) userId is handled")
    void findByUserId_maxLongUserId_handledGracefully() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", Long.MAX_VALUE)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        List<Holding> result = holdingRepository.findByUserId(Long.MAX_VALUE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────
    // findByUserId — EntityManager / DB failure paths
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId: EntityManager createQuery throws RuntimeException")
    void findByUserId_entityManagerThrows_propagates() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class))
                .thenThrow(new RuntimeException("EntityManager not available"));

        assertThrows(RuntimeException.class,
                () -> holdingRepository.findByUserId(100L));

        verify(typedQuery, never()).setParameter(anyString(), any());
        verify(typedQuery, never()).getResultList();
    }

    @Test
    @DisplayName("findByUserId: getResultList throws PersistenceException (DB down)")
    void findByUserId_dbDown_throwsPersistenceException() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 100L)).thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenThrow(new jakarta.persistence.PersistenceException("Connection refused"));

        assertThrows(jakarta.persistence.PersistenceException.class,
                () -> holdingRepository.findByUserId(100L));
    }

    @Test
    @DisplayName("findByUserId: getResultList returns null — caller gets null (defensive note)")
    void findByUserId_getResultListReturnsNull_returnsNull() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 100L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(null);

        // JPA spec guarantees non-null, but guard against misbehaving implementations
        List<Holding> result = holdingRepository.findByUserId(100L);
        assertNull(result); // Repo doesn't null-check; document this behaviour
    }

    // ─────────────────────────────────────────────
    // Interaction / call-count verification
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("findByUserId: EntityManager.createQuery called exactly once per invocation")
    void findByUserId_createQueryCalledExactlyOnce() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        holdingRepository.findByUserId(100L);
        holdingRepository.findByUserId(200L);

        verify(entityManager, times(2)).createQuery(EXPECTED_QUERY, Holding.class);
    }

    @Test
    @DisplayName("findByUserId: correct JPQL string is always used")
    void findByUserId_correctJpqlUsed() {
        when(entityManager.createQuery(EXPECTED_QUERY, Holding.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 100L)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        holdingRepository.findByUserId(100L);

        // Ensures a typo in the query string doesn't silently slip through
        verify(entityManager).createQuery(
                "SELECT h FROM Holding h WHERE h.userId = :userId",
                Holding.class
        );
    }
}
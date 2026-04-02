package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.axonsoft.eval.minibank.model.Transaction;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountIdOrderByTimestampAsc(Long accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId " +
    "AND t.type = 'TRANSFER_OUT' " +
    "AND t.timestamp >= :startOfDay AND t.timestamp <= :endOfDay")
    List<Transaction> findOutgoingTransactionsForDay(Long accountId, Instant startOfDay, Instant endOfDay);
}

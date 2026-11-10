package com.USWCicrcleLink.server.notification.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.Collection;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    @Query(value = """
            SELECT * FROM notification_outbox
            WHERE (status IN ('PENDING','RETRY') AND next_attempt_at <= :now)
               OR (status = 'PROCESSING' AND lease_until <= :now)
            ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<NotificationOutbox> lockNext(@Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NotificationOutbox n where n.id = :id")
    Optional<NotificationOutbox> lockById(@Param("id") Long id);

    @Query("select min(n.createdAt) from NotificationOutbox n where n.status in :statuses")
    Optional<Instant> oldestUnfinished(@Param("statuses") Collection<NotificationOutbox.Status> statuses);
}

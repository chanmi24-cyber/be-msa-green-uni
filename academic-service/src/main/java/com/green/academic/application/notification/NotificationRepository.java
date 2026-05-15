package com.green.academic.application.notification;

import com.green.academic.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByMemberCodeAndIsReadFalse(Long memberCode);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.memberCode = :memberCode")
    void readAllByMemberCode(@Param("memberCode") Long memberCode);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.memberCode = :memberCode")
    void deleteAllByMemberCode(@Param("memberCode") Long memberCode);

}
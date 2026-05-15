package com.green.academic.application.notification;

import com.green.academic.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByMemberCodeAndIsReadFalse(Long memberCode);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.memberCode = :memberCode OR n.targetRole = :targetRole) AND n.isRead = false")
    long countUnreadByMemberCodeOrRole(@Param("memberCode") Long memberCode, @Param("targetRole") String targetRole);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.memberCode = :memberCode")
    void readAllByMemberCode(@Param("memberCode") Long memberCode);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.memberCode = :memberCode")
    void deleteAllByMemberCode(@Param("memberCode") Long memberCode);

}
package com.green.core.entity.tuition;
import com.green.common.entity.CreatedUpdatedAt;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "tuition_mail_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TuitionMailLog extends CreatedUpdatedAt {

    @Id @Tsid
    @Column(name = "mail_log_id")
    private Long mailLogId; // TSID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tuition_id", nullable = false)
    private Tuition tuition;

    @Column(name = "recipient_email", nullable = false, length = 50)
    private String recipientEmail;

    @Builder.Default
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess = true;

    @Column(name = "sender_code", nullable = false)
    private Long senderCode;
}

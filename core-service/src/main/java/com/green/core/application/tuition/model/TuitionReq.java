package com.green.core.application.tuition.model;

import com.green.core.enumcode.EnumTuitionStatus;
import lombok.Getter;
import lombok.Setter;

public class TuitionReq {
    @Getter @Setter
    public static class PaymentRequest {
        private Integer year;
        private Integer semester;
    }

    @Getter @Setter
    public static class UpdateStatusRequest {
        private EnumTuitionStatus status;
    }

    @Getter @Setter
    public static class MailSendRequest {
        private Integer year;
        private Integer semester;
    }

    @Getter @Setter
    public static class UpdatePolicyRequest {
        private Long baseAmount;
    }
}
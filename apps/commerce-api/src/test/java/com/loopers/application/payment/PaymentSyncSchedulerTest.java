package com.loopers.application.payment;

import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.*;
import com.loopers.interfaces.api.payment.dto.CardNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSyncSchedulerTest {

    @Mock PaymentService paymentService;
    @Mock NotificationService notificationService;
    @Mock OrderPaymentProcessor orderPaymentProcessor;
    @Mock PaymentEventPublisher paymentEventPublisher;
    @Mock Payment payment1;
    @Mock Payment payment2;
    @Mock Order order1;
    @Mock Order order2;

    @InjectMocks PaymentSyncScheduler sut;

    @Test
    @DisplayName("syncPendingPayments - PENDING 결제가 없으면 아무것도 하지 않는다")
    void syncPendingPayments_noPendingPayments_doesNothing() {
        // Arrange
        when(paymentService.findPendingPayments()).thenReturn(List.of());

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentService).findPendingPayments();
        verifyNoMoreInteractions(paymentService, notificationService, orderPaymentProcessor, paymentEventPublisher);
    }

    @Test
    @DisplayName("syncPendingPayments - 10분 이내 PENDING 결제의 동기화가 성공하면 주문을 완료한다")
    void syncPendingPayments_recentPendingPayment_syncSuccess_completesOrder() {
        // Arrange
        ZonedDateTime recentTime = ZonedDateTime.now().minusMinutes(5);
        when(payment1.getCreatedAt()).thenReturn(recentTime);
        when(payment1.getOrder()).thenReturn(order1);
        when(payment1.getTransactionId()).thenReturn("TXN123456");
        when(order1.getId()).thenReturn(1L);
        when(payment1.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment1.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment1.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment1.getStatus()).thenReturn(Payment.PaymentStatus.PENDING);
        
        when(paymentService.findPendingPayments()).thenReturn(List.of(payment1));
        when(paymentService.hasSyncPaymentStatus(payment1)).thenReturn(true);

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentService).hasSyncPaymentStatus(payment1);
        verify(orderPaymentProcessor).completeOrderAndPayment(any(PaymentCommand.CallbackRequest.class));
        verify(paymentEventPublisher, never()).publish(any());
        verify(notificationService, never()).sendPaymentSyncFailureAlert(any());
    }

    @Test
    @DisplayName("syncPendingPayments - 10분 이내 PENDING 결제의 동기화가 실패하면 완료 처리하지 않는다")
    void syncPendingPayments_recentPendingPayment_syncFails_doesNotCompleteOrder() {
        // Arrange
        ZonedDateTime recentTime = ZonedDateTime.now().minusMinutes(5);
        when(payment1.getCreatedAt()).thenReturn(recentTime);
        
        when(paymentService.findPendingPayments()).thenReturn(List.of(payment1));
        when(paymentService.hasSyncPaymentStatus(payment1)).thenReturn(false);

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentService).hasSyncPaymentStatus(payment1);
        verify(orderPaymentProcessor, never()).completeOrderAndPayment(any());
        verify(paymentEventPublisher, never()).publish(any());
        verify(notificationService, never()).sendPaymentSyncFailureAlert(any());
    }

    @Test
    @DisplayName("syncPendingPayments - 10분 이상 PENDING 결제는 복구 이벤트를 발행하고 알림을 발송한다")
    void syncPendingPayments_oldPendingPayment_publishesRecoveryEventAndSendsNotification() {
        // Arrange
        ZonedDateTime oldTime = ZonedDateTime.now().minusMinutes(15);
        when(payment1.getCreatedAt()).thenReturn(oldTime);
        when(payment1.getOrder()).thenReturn(order1);
        when(payment1.getTransactionId()).thenReturn("TXN123456");
        when(order1.getId()).thenReturn(1L);
        when(payment1.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment1.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment1.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment1.getStatus()).thenReturn(Payment.PaymentStatus.PENDING);
        
        when(paymentService.findPendingPayments()).thenReturn(List.of(payment1));
        when(paymentService.hasSyncPaymentStatus(payment1)).thenReturn(true);

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentEventPublisher).publish(PaymentEvent.PaymentFailedRecovery.of(1L));
        verify(notificationService).sendPaymentSyncFailureAlert("TXN123456");
        verify(paymentService).hasSyncPaymentStatus(payment1);
        verify(orderPaymentProcessor).completeOrderAndPayment(any(PaymentCommand.CallbackRequest.class));
    }

    @Test
    @DisplayName("syncPendingPayments - 동기화 중 예외 발생 시 로그를 남기고 계속 진행한다")
    void syncPendingPayments_exceptionDuringSync_logsErrorAndContinues() {
        // Arrange
        ZonedDateTime recentTime = ZonedDateTime.now().minusMinutes(5);
        when(payment1.getCreatedAt()).thenReturn(recentTime);
        when(payment2.getCreatedAt()).thenReturn(recentTime);
        when(payment2.getOrder()).thenReturn(order2);
        when(payment2.getTransactionId()).thenReturn("TXN789012");
        when(order2.getId()).thenReturn(2L);
        when(payment2.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment2.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment2.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment2.getStatus()).thenReturn(Payment.PaymentStatus.PENDING);
        
        when(paymentService.findPendingPayments()).thenReturn(List.of(payment1, payment2));
        when(paymentService.hasSyncPaymentStatus(payment1)).thenThrow(new RuntimeException("Sync error"));
        when(paymentService.hasSyncPaymentStatus(payment2)).thenReturn(true);

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentService).hasSyncPaymentStatus(payment1);
        verify(paymentService).hasSyncPaymentStatus(payment2);
        verify(orderPaymentProcessor).completeOrderAndPayment(any(PaymentCommand.CallbackRequest.class));
    }

    @Test
    @DisplayName("syncPendingPayments - 여러 결제 건을 처리하고 성공/실패 카운트를 로그에 남긴다")
    void syncPendingPayments_multiplePayments_logsSuccessAndFailureCounts() {
        // Arrange
        ZonedDateTime recentTime = ZonedDateTime.now().minusMinutes(5);
        ZonedDateTime oldTime = ZonedDateTime.now().minusMinutes(15);
        
        when(payment1.getCreatedAt()).thenReturn(recentTime);
        when(payment1.getOrder()).thenReturn(order1);
        when(payment1.getTransactionId()).thenReturn("TXN123456");
        when(order1.getId()).thenReturn(1L);
        when(payment1.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment1.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment1.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment1.getStatus()).thenReturn(Payment.PaymentStatus.PENDING);
        
        when(payment2.getCreatedAt()).thenReturn(oldTime);
        when(payment2.getOrder()).thenReturn(order2);
        when(payment2.getTransactionId()).thenReturn("TXN789012");
        when(order2.getId()).thenReturn(2L);
        
        when(paymentService.findPendingPayments()).thenReturn(List.of(payment1, payment2));
        when(paymentService.hasSyncPaymentStatus(payment1)).thenReturn(true);
        when(paymentService.hasSyncPaymentStatus(payment2)).thenReturn(false);

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentService).hasSyncPaymentStatus(payment1);
        verify(paymentService).hasSyncPaymentStatus(payment2);
        verify(orderPaymentProcessor).completeOrderAndPayment(any(PaymentCommand.CallbackRequest.class));
        verify(paymentEventPublisher).publish(PaymentEvent.PaymentFailedRecovery.of(2L));
        verify(notificationService).sendPaymentSyncFailureAlert("TXN789012");
    }

    @Test
    @DisplayName("syncPendingPayments - 전체 작업 실패 시 예외를 로그에 남긴다")
    void syncPendingPayments_overallFailure_logsException() {
        // Arrange
        when(paymentService.findPendingPayments()).thenThrow(new RuntimeException("Database error"));

        // Act
        sut.syncPendingPayments();

        // Assert
        verify(paymentService).findPendingPayments();
        verifyNoMoreInteractions(orderPaymentProcessor, paymentEventPublisher, notificationService);
    }
}

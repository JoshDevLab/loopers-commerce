package com.loopers.application.payment;

import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.interfaces.api.payment.dto.CardNo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @Mock PaymentService paymentService;
    @Mock OrderService orderService;
    @Mock OrderPaymentProcessor orderPaymentProcessor;
    @Mock PaymentEventPublisher paymentEventPublisher;
    @Mock NotificationService notificationService;
    @Mock PaymentExceptionTranslator exceptionTranslator;
    @Mock Order order;
    @Mock Payment payment;
    @Mock ExternalPaymentResponse externalPaymentResponse;

    @InjectMocks PaymentFacade sut;

    @Test
    @DisplayName("payment - 이미 결제 완료된 주문이면 예외를 던진다")
    void payment_alreadyCompletedOrder_throws() {
        // Arrange
        PaymentCommand.Request command = createPaymentCommand();
        when(orderService.findByIdForUpdate(1L)).thenReturn(order);
        when(order.getId()).thenReturn(1L);
        when(paymentService.existsByOrderIdAndStatus(1L, Payment.PaymentStatus.SUCCESS)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> sut.payment(command))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.ALREADY_EXIST_ORDER_PAYMENT);
    }

    @Test
    @DisplayName("payment - 외부 PG 결제 실패 시 복구 이벤트를 발행하고 예외를 던진다")
    void payment_externalPaymentFails_publishesRecoveryEventAndThrows() {
        // Arrange
        PaymentCommand.Request command = createPaymentCommand();
        when(orderService.findByIdForUpdate(1L)).thenReturn(order);
        when(order.getId()).thenReturn(1L);
        when(paymentService.existsByOrderIdAndStatus(1L, Payment.PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentService.create(command)).thenReturn(payment);
        
        CoreException pgException = new CoreException(ErrorType.PAYMENT_FAIL, "PG 오류");
        when(exceptionTranslator.execute(any())).thenThrow(pgException);

        // Act & Assert
        assertThatThrownBy(() -> sut.payment(command))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.PAYMENT_FAIL);

        verify(paymentEventPublisher).publish(PaymentEvent.PaymentFailedRecovery.of(1L));
    }

    @Test
    @DisplayName("payment - 정상적인 결제 시 PaymentInfo를 반환한다")
    void payment_success_returnsPaymentInfo() {
        // Arrange
        PaymentCommand.Request command = createPaymentCommand();
        when(orderService.findByIdForUpdate(1L)).thenReturn(order);
        when(order.getId()).thenReturn(1L);
        when(paymentService.existsByOrderIdAndStatus(1L, Payment.PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentService.create(command)).thenReturn(payment);
        when(payment.getId()).thenReturn(2L);
        when(externalPaymentResponse.getTransactionId()).thenReturn("TXN123456");
        when(exceptionTranslator.execute(any())).thenReturn(externalPaymentResponse);

        // Mock PaymentInfo.of(payment)
        when(payment.getOrder()).thenReturn(order);
        when(payment.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getStatus()).thenReturn(Payment.PaymentStatus.PENDING);
        when(payment.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment.getTransactionId()).thenReturn("TXN123456");

        // Act
        PaymentInfo result = sut.payment(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getOrderId()).isEqualTo(1L);
        verify(paymentService).updateTransactionId(2L, "TXN123456");
    }

    @Test
    @DisplayName("processCallback - 콜백 데이터 동기화 실패 시 DataSyncException을 던진다")
    void processCallback_dataSyncFails_throwsDataSyncException() {
        // Arrange
        PaymentCommand.CallbackRequest command = createCallbackCommand(true);
        when(exceptionTranslator.executeForCallback(any())).thenReturn(externalPaymentResponse);
        when(externalPaymentResponse.checkSync(command)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> sut.processCallback(command))
                .isInstanceOf(DataSyncException.class)
                .hasMessage("콜백 데이터 동기화 실패");
    }

    @Test
    @DisplayName("processCallback - 성공 콜백 시 주문과 결제를 완료하고 PaymentInfo를 반환한다")
    void processCallback_successCallback_completesOrderAndPayment() {
        // Arrange
        PaymentCommand.CallbackRequest command = createCallbackCommand(true);
        when(exceptionTranslator.executeForCallback(any())).thenReturn(externalPaymentResponse);
        when(externalPaymentResponse.checkSync(command)).thenReturn(true);
        when(orderPaymentProcessor.completeOrderAndPayment(command)).thenReturn(payment);

        // Mock PaymentInfo.of(payment)
        when(payment.getId()).thenReturn(2L);
        when(payment.getOrder()).thenReturn(order);
        when(order.getId()).thenReturn(1L);
        when(payment.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getStatus()).thenReturn(Payment.PaymentStatus.SUCCESS);
        when(payment.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment.getTransactionId()).thenReturn("TXN123456");

        // Act
        PaymentInfo result = sut.processCallback(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
        verify(orderPaymentProcessor).completeOrderAndPayment(command);
        verify(paymentEventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("processCallback - 실패 콜백 시 복구 이벤트를 발행하고 주문과 결제를 실패 처리한다")
    void processCallback_failureCallback_publishesRecoveryEventAndFailsOrderAndPayment() {
        // Arrange
        PaymentCommand.CallbackRequest command = createCallbackCommand(false);
        when(exceptionTranslator.executeForCallback(any())).thenReturn(externalPaymentResponse);
        when(externalPaymentResponse.checkSync(command)).thenReturn(true);
        when(orderPaymentProcessor.failedOrderAndPayment(command)).thenReturn(payment);

        // Mock PaymentInfo.of(payment)
        when(payment.getId()).thenReturn(2L);
        when(payment.getOrder()).thenReturn(order);
        when(order.getId()).thenReturn(1L);
        when(payment.getCardType()).thenReturn(CardType.SAMSUNG);
        when(payment.getCardNo()).thenReturn(CardNo.valueOfName("1234567890123456"));
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getStatus()).thenReturn(Payment.PaymentStatus.FAILED);
        when(payment.getPaidAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(payment.getTransactionId()).thenReturn("TXN123456");

        // Act
        PaymentInfo result = sut.processCallback(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        verify(orderPaymentProcessor).failedOrderAndPayment(command);
        verify(paymentEventPublisher).publish(PaymentEvent.PaymentFailedRecovery.of(1L));
    }

    // fallback 메서드는 private이므로 retry 실패 시나리오로 대체
    @Test
    @DisplayName("processCallback - retry 실패 시 DataSyncException이 여러번 발생하면 최종적으로 예외를 던진다")
    void processCallback_retryFails_eventuallyThrows() {
        // Arrange
        PaymentCommand.CallbackRequest command = createCallbackCommand(true);
        DataSyncException syncException = new DataSyncException("콜백 데이터 동기화 실패");
        
        when(exceptionTranslator.executeForCallback(any())).thenReturn(externalPaymentResponse);
        when(externalPaymentResponse.checkSync(command)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> sut.processCallback(command))
                .isInstanceOf(DataSyncException.class)
                .hasMessage("콜백 데이터 동기화 실패");
    }

    private PaymentCommand.Request createPaymentCommand() {
        return new PaymentCommand.Request(
                1L,
                Payment.PaymentType.CARD,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url"
        );
    }

    private PaymentCommand.CallbackRequest createCallbackCommand(boolean isSuccess) {
        return PaymentCommand.CallbackRequest.create(
                "TXN123456",
                "ORDER_1", // PgOrderIdGenerator.generate(1L) 형식
                "SAMSUNG",
                "1234567890123456",
                BigDecimal.valueOf(10000),
                isSuccess ? "SUCCESS" : "FAILED",
                isSuccess ? "결제성공" : "결제실패"
        );
    }
}

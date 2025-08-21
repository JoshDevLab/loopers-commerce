package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PaymentProcessorManager paymentProcessorManager;
    @Mock PaymentProcessor paymentProcessor;
    @Mock Order order;
    @Mock Payment payment;

    @InjectMocks PaymentService sut;

    @Test
    @DisplayName("payment - 주문을 찾을 수 없으면 예외를 던진다")
    void payment_orderNotFound_throws() {
        // Arrange
        PaymentCommand.Request command = new PaymentCommand.Request(
                1L,
                Payment.PaymentType.CARD,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url"
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sut.payment(command))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("payment - 결제금액이 0원 이하면 예외를 던진다")
    void payment_invalidPaidAmount_throws() {
        // Arrange
        PaymentCommand.Request command = new PaymentCommand.Request(
                1L,
                Payment.PaymentType.CARD,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url"
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(order.getPaidAmount()).thenReturn(BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> sut.payment(command))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.INVALID_PAID_AMOUNT);
    }

    @Test
    @DisplayName("payment - 정상적인 경우 외부 결제 응답을 반환한다")
    void payment_success() {
        // Arrange
        PaymentCommand.Request command = new PaymentCommand.Request(
                1L,
                Payment.PaymentType.CARD,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url"
        );
        BigDecimal paidAmount = BigDecimal.valueOf(10000);
        ExternalPaymentRequest externalRequest = mock(ExternalPaymentRequest.class);
        ExternalPaymentResponse expectedResponse = mock(ExternalPaymentResponse.class);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(order.getPaidAmount()).thenReturn(paidAmount);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.createRequest(command, paidAmount)).thenReturn(externalRequest);
        when(paymentProcessor.payment(externalRequest)).thenReturn(expectedResponse);

        // Act
        ExternalPaymentResponse result = sut.payment(command);

        // Assert
        assertThat(result).isSameAs(expectedResponse);
        verify(paymentProcessor).payment(externalRequest);
    }

    @Test
    @DisplayName("create - 결제를 생성한다")
    void create_success() {
        // Arrange
        PaymentCommand.Request command = new PaymentCommand.Request(
                1L,
                Payment.PaymentType.CARD,
                CardType.SAMSUNG,
                CardNo.valueOfName("1234567890123456"),
                "http://callback.url"
        );
        BigDecimal paidAmount = BigDecimal.valueOf(10000);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(order.getPaidAmount()).thenReturn(paidAmount);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = sut.create(command);

        // Assert
        assertThat(result).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("updateSuccessStatus - 결제를 찾을 수 없으면 예외를 던진다")
    void updateSuccessStatus_paymentNotFound_throws() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sut.updateSuccessStatus(1L))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("updateSuccessStatus - 결제 상태를 성공으로 업데이트한다")
    void updateSuccessStatus_success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        Payment result = sut.updateSuccessStatus(1L);

        // Assert
        assertThat(result).isSameAs(payment);
        verify(payment).success();
    }

    @Test
    @DisplayName("updateFailedStatus - 결제 상태를 실패로 업데이트한다")
    void updateFailedStatus_success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        Payment result = sut.updateFailedStatus(1L);

        // Assert
        assertThat(result).isSameAs(payment);
        verify(payment).failed();
    }

    @Test
    @DisplayName("updateTransactionId - 트랜잭션 ID를 업데이트한다")
    void updateTransactionId_success() {
        // Arrange
        String transactionId = "TXN123456";
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        sut.updateTransactionId(1L, transactionId);

        // Assert
        verify(payment).updateTransactionId(transactionId);
    }

    @Test
    @DisplayName("findByTransactionId - 트랜잭션 ID로 결제를 찾는다")
    void findByTransactionId_success() {
        // Arrange
        String transactionKey = "TXN123456";
        when(paymentRepository.findByTransactionId(transactionKey)).thenReturn(Optional.of(payment));

        // Act
        Payment result = sut.findByTransactionId(transactionKey);

        // Assert
        assertThat(result).isSameAs(payment);
    }

    @Test
    @DisplayName("findByTransactionId - 트랜잭션 ID로 결제를 찾을 수 없으면 예외를 던진다")
    void findByTransactionId_notFound_throws() {
        // Arrange
        String transactionKey = "TXN123456";
        when(paymentRepository.findByTransactionId(transactionKey)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sut.findByTransactionId(transactionKey))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("getTransactionIdFromExternal - 외부 결제 정보를 조회한다")
    void getTransactionIdFromExternal_success() {
        // Arrange
        String transactionKey = "TXN123456";
        ExternalPaymentResponse expectedResponse = mock(ExternalPaymentResponse.class);
        
        when(paymentRepository.findByTransactionId(transactionKey)).thenReturn(Optional.of(payment));
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey(transactionKey)).thenReturn(expectedResponse);

        // Act
        ExternalPaymentResponse result = sut.getTransactionIdFromExternal(transactionKey);

        // Assert
        assertThat(result).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("findPendingPayments - 대기 중인 결제 목록을 조회한다")
    void findPendingPayments_success() {
        // Arrange
        List<Payment> expectedPayments = List.of(payment);
        when(paymentRepository.findByStatus(Payment.PaymentStatus.PENDING)).thenReturn(expectedPayments);

        // Act
        List<Payment> result = sut.findPendingPayments();

        // Assert
        assertThat(result).isSameAs(expectedPayments);
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - 외부 응답이 null이면 false를 반환한다")
    void hasSyncPaymentStatus_externalResponseNull_returnsFalse() {
        // Arrange
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(null);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - 외부 응답이 실패면 false를 반환한다")
    void hasSyncPaymentStatus_externalResponseNotSuccess_returnsFalse() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(false);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - PG 상태가 PENDING이면 true를 반환한다")
    void hasSyncPaymentStatus_pgStatusPending_returnsTrue() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("PENDING");

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isTrue();
        verify(paymentRepository, never()).updatePaymentStatus(any(), any(), any());
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - PG 상태가 SUCCESS이고 업데이트 성공하면 true를 반환한다")
    void hasSyncPaymentStatus_pgStatusSuccess_updateSuccess_returnsTrue() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("SUCCESS");
        when(paymentRepository.updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.SUCCESS), any(LocalDateTime.class)))
                .thenReturn(1);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isTrue();
        verify(paymentRepository).updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.SUCCESS), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - PG 상태가 FAILED이고 업데이트 성공하면 false를 반환한다")
    void hasSyncPaymentStatus_pgStatusFailed_updateSuccess_returnsFalse() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("FAILED");
        when(paymentRepository.updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.FAILED), any(LocalDateTime.class)))
                .thenReturn(1);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isFalse();
        verify(paymentRepository).updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.FAILED), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - PG 상태가 CANCELED이고 업데이트 성공하면 true를 반환한다")
    void hasSyncPaymentStatus_pgStatusCanceled_updateSuccess_returnsTrue() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("CANCELED");
        when(paymentRepository.updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.CANCELED), any(LocalDateTime.class)))
                .thenReturn(1);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isTrue();
        verify(paymentRepository).updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.CANCELED), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - 업데이트된 행이 0개면 false를 반환한다")
    void hasSyncPaymentStatus_updateRowsZero_returnsFalse() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("SUCCESS");
        when(paymentRepository.updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.SUCCESS), any(LocalDateTime.class)))
                .thenReturn(0);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - 알 수 없는 PG 상태는 PENDING으로 변환하고 true를 반환한다")
    void hasSyncPaymentStatus_unknownPgStatus_convertsToPending_returnsTrue() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("UNKNOWN_STATUS");

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isTrue();
        verify(paymentRepository, never()).updatePaymentStatus(any(), any(), any());
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - 예외 발생 시 false를 반환한다")
    void hasSyncPaymentStatus_exceptionThrown_returnsFalse() {
        // Arrange
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenThrow(new RuntimeException("External API Error"));

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - SUCCESS 상태를 SUCCESS로 변환한다")
    void hasSyncPaymentStatus_successStatus_convertsToSuccess() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("SUCCESS");
        when(paymentRepository.updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.SUCCESS), any(LocalDateTime.class)))
                .thenReturn(1);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isTrue();
        verify(paymentRepository).updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.SUCCESS), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("hasSyncPaymentStatus - FAILED 상태를 FAILED로 변환한다")
    void hasSyncPaymentStatus_failedStatus_convertsToFailed() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(payment.getId()).thenReturn(1L);
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("FAILED");
        when(paymentRepository.updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.FAILED), any(LocalDateTime.class)))
                .thenReturn(1);

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isFalse();
        verify(paymentRepository).updatePaymentStatus(eq(1L), eq(Payment.PaymentStatus.FAILED), any(LocalDateTime.class));
    }



    @Test
    @DisplayName("hasSyncPaymentStatus - PENDING 상태는 업데이트하지 않고 true를 반환한다")
    void hasSyncPaymentStatus_pendingStatus_returnsTrue() {
        // Arrange
        ExternalPaymentResponse response = mock(ExternalPaymentResponse.class);
        when(payment.getPaymentType()).thenReturn(Payment.PaymentType.CARD);
        when(payment.getTransactionId()).thenReturn("TXN123456");
        when(paymentProcessorManager.getProcessor(Payment.PaymentType.CARD)).thenReturn(paymentProcessor);
        when(paymentProcessor.getByTransactionKey("TXN123456")).thenReturn(response);
        when(response.isSuccess()).thenReturn(true);
        when(response.getStatus()).thenReturn("PROCESSING");

        // Act
        boolean result = sut.hasSyncPaymentStatus(payment);

        // Assert
        assertThat(result).isTrue();
        verify(paymentRepository, never()).updatePaymentStatus(any(), any(), any());
    }
}

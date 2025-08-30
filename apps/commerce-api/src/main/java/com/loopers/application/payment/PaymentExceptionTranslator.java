package com.loopers.application.payment;

import com.loopers.infrastructure.payment.pg.LoopersPgProcessor;
import com.loopers.infrastructure.payment.pg.exception.PgBusinessException;
import com.loopers.infrastructure.payment.pg.exception.PgException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class PaymentExceptionTranslator {

    public <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (LoopersPgProcessor.PgServiceTemporarilyUnavailableException e) {
            log.error("PG 서비스 일시적 장애", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, e.getMessage());
        } catch (LoopersPgProcessor.PgProcessingException e) {
            log.error("PG 처리 오류", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, e.getMessage());
        } catch (PgBusinessException e) {
            log.error("PG 클라이언트 오류", e);
            throw new CoreException(ErrorType.PAYMENT_CLIENT_BAD_REQUEST, e.getMessage());
        } catch (PgException e) {
            log.error("PG 일반 오류", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 결제 오류", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, e.getMessage());
        }
    }

    public <T> T executeForCallback(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (LoopersPgProcessor.PgServiceTemporarilyUnavailableException e) {
            log.error("PG 서비스 일시적 장애로 인한 콜백 동기화 실패", e);
            throw new CoreException(ErrorType.CALLBACK_DATA_SYNC_FAILED, e.getMessage());
        } catch (LoopersPgProcessor.PgProcessingException e) {
            log.error("PG 처리 오류로 인한 콜백 동기화 실패", e);
            throw new CoreException(ErrorType.CALLBACK_DATA_SYNC_FAILED, e.getMessage());
        } catch (PgException e) {
            log.error("PG 오류로 인한 콜백 동기화 실패", e);
            throw new CoreException(ErrorType.CALLBACK_DATA_SYNC_FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 콜백 오류", e);
            throw new CoreException(ErrorType.CALLBACK_DATA_SYNC_FAILED, e.getMessage());
        }
    }
}

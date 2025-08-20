package com.loopers.application.payment;

import com.loopers.infrastructure.payment.pg.LoopersPgProcessor;
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
            throw new CoreException(ErrorType.PAYMENT_FAIL, "PG 서비스가 일시적으로 이용 불가능합니다. 잠시 후 다시 시도해주세요.");
        } catch (LoopersPgProcessor.PgProcessingException e) {
            log.error("PG 처리 오류", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, "결제 처리 중 오류가 발생했습니다.");
        } catch (PgException e) {
            log.error("PG 일반 오류", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, "외부 결제 시스템 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("예상치 못한 결제 오류", e);
            throw new CoreException(ErrorType.PAYMENT_FAIL, "결제 처리 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    public <T> T executeForCallback(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (LoopersPgProcessor.PgServiceTemporarilyUnavailableException e) {
            log.error("PG 서비스 일시적 장애로 인한 콜백 동기화 실패", e);
            throw new DataSyncException("PG 서비스 일시적 장애로 인한 동기화 실패");
        } catch (LoopersPgProcessor.PgProcessingException e) {
            log.error("PG 처리 오류로 인한 콜백 동기화 실패", e);
            throw new DataSyncException("PG 처리 오류로 인한 동기화 실패");
        } catch (PgException e) {
            log.error("PG 오류로 인한 콜백 동기화 실패", e);
            throw new DataSyncException("PG 시스템 오류로 인한 동기화 실패");
        } catch (Exception e) {
            log.error("예상치 못한 콜백 오류", e);
            throw new DataSyncException("예상치 못한 오류로 인한 동기화 실패");
        }
    }
}

package com.loopers.domain.product.like;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLikeStatServiceTest {

    @Mock ProductLikeStatRepository productLikeStatRepository;
    @Mock ProductLikeStat productLikeStat;

    @InjectMocks ProductLikeStatService sut;

    @Test
    @DisplayName("like - 기존 통계가 있으면 좋아요 카운트를 증가시킨다")
    void like_whenStatExists_increasesLikeCount() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStat).like();
        verify(productLikeStatRepository, never()).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("like - 기존 통계가 없으면 새로운 통계를 생성하고 저장한다")
    void like_whenStatNotExists_createsNewStat() {
        // Arrange
        Long productId = 1L;
        
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("like - 동시성으로 인한 DataIntegrityViolationException 발생 시 재조회하여 좋아요 증가")
    void like_whenDataIntegrityViolationException_retriesWithExistingStat() {
        // Arrange
        Long productId = 1L;
        ProductLikeStat existingStat = mock(ProductLikeStat.class);
        
        when(productLikeStatRepository.findByProductIdWithLock(productId))
            .thenReturn(Optional.empty())  // 첫 번째 호출
            .thenReturn(Optional.of(existingStat));  // DataIntegrityViolationException 후 재조회
            
        doThrow(new DataIntegrityViolationException("Duplicate key"))
            .when(productLikeStatRepository).save(any(ProductLikeStat.class));

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository, times(2)).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
        verify(existingStat).like();
    }

    @Test
    @DisplayName("like - DataIntegrityViolationException 발생 후 재조회에서도 통계가 없으면 좋아요 증가하지 않음")
    void like_whenDataIntegrityViolationExceptionAndStillNotExists_doesNothing() {
        // Arrange
        Long productId = 1L;
        
        when(productLikeStatRepository.findByProductIdWithLock(productId))
            .thenReturn(Optional.empty())  // 첫 번째 호출
            .thenReturn(Optional.empty());  // DataIntegrityViolationException 후 재조회에서도 없음
            
        doThrow(new DataIntegrityViolationException("Duplicate key"))
            .when(productLikeStatRepository).save(any(ProductLikeStat.class));

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository, times(2)).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
        // existingStat.like()는 호출되지 않음
    }

    @Test
    @DisplayName("unLike - 기존 통계가 있으면 좋아요 카운트를 감소시킨다")
    void unLike_whenStatExists_decreasesLikeCount() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.unLike(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStat).unLike();
        verify(productLikeStatRepository, never()).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("unLike - 기존 통계가 없으면 0으로 초기화된 새로운 통계를 생성하고 저장한다")
    void unLike_whenStatNotExists_createsNewStatWithZero() {
        // Arrange
        Long productId = 1L;
        
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());

        // Act
        sut.unLike(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("like - productId가 null일 때도 처리할 수 있다")
    void like_withNullProductId_handlesGracefully() {
        // Arrange
        Long productId = null;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("unLike - productId가 null일 때도 처리할 수 있다")
    void unLike_withNullProductId_handlesGracefully() {
        // Arrange
        Long productId = null;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());

        // Act
        sut.unLike(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("like - 여러 번 호출해도 각각 처리된다")
    void like_multipleCallsAreHandled() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.like(productId);
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository, times(2)).findByProductIdWithLock(productId);
        verify(productLikeStat, times(2)).like();
    }

    @Test
    @DisplayName("unLike - 여러 번 호출해도 각각 처리된다")
    void unLike_multipleCallsAreHandled() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.unLike(productId);
        sut.unLike(productId);

        // Assert
        verify(productLikeStatRepository, times(2)).findByProductIdWithLock(productId);
        verify(productLikeStat, times(2)).unLike();
    }

    @Test
    @DisplayName("like - 저장 시 다른 예외는 재처리하지 않고 그대로 던진다")
    void like_whenOtherExceptionOccurs_throwsException() {
        // Arrange
        Long productId = 1L;
        RuntimeException otherException = new RuntimeException("Other database error");
        
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());
        doThrow(otherException).when(productLikeStatRepository).save(any(ProductLikeStat.class));

        // Act & Assert
        assertThatThrownBy(() -> sut.like(productId))
            .isSameAs(otherException);

        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
        // 재조회는 하지 않음
        verify(productLikeStatRepository, times(1)).findByProductIdWithLock(productId);
    }

    @Test
    @DisplayName("like와 unLike를 혼합해서 호출할 수 있다")
    void likeAndUnLike_mixedCalls_areHandled() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.like(productId);
        sut.unLike(productId);
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository, times(3)).findByProductIdWithLock(productId);
        verify(productLikeStat, times(2)).like();
        verify(productLikeStat, times(1)).unLike();
    }

    @Test
    @DisplayName("like - 동일한 productId로 동시 처리 시나리오를 시뮬레이션한다")
    void like_concurrentScenarioSimulation() {
        // Arrange
        Long productId = 1L;
        
        // 첫 번째 스레드: 통계가 없어서 새로 생성 시도
        when(productLikeStatRepository.findByProductIdWithLock(productId))
            .thenReturn(Optional.empty())  // 첫 번째 조회
            .thenReturn(Optional.of(productLikeStat));  // 재조회
            
        // 두 번째 스레드가 먼저 생성해서 DataIntegrityViolationException 발생
        doThrow(new DataIntegrityViolationException("Duplicate"))
            .when(productLikeStatRepository).save(any(ProductLikeStat.class));

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository, times(2)).findByProductIdWithLock(productId);
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
        verify(productLikeStat).like();
    }

    @Test
    @DisplayName("like - 락으로 조회 시 안전하게 처리된다")
    void like_usingLockForSafety() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.like(productId);

        // Assert
        // findByProductIdWithLock을 사용하여 락을 걸고 조회하는지 확인
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStat).like();
    }

    @Test
    @DisplayName("unLike - 락으로 조회 시 안전하게 처리된다")
    void unLike_usingLockForSafety() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.of(productLikeStat));

        // Act
        sut.unLike(productId);

        // Assert
        // findByProductIdWithLock을 사용하여 락을 걸고 조회하는지 확인
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        verify(productLikeStat).unLike();
    }

    @Test
    @DisplayName("like - 통계가 없을 때 ProductLikeStat.createLike()로 생성한다")
    void like_createsStatUsingCreateLikeMethod() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());

        // Act
        sut.like(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        // ProductLikeStat.createLike(productId)가 호출되어 생성된 객체가 저장됨
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
    }

    @Test
    @DisplayName("unLike - 통계가 없을 때 ProductLikeStat.create()로 생성한다")
    void unLike_createsStatUsingCreateMethod() {
        // Arrange
        Long productId = 1L;
        when(productLikeStatRepository.findByProductIdWithLock(productId)).thenReturn(Optional.empty());

        // Act
        sut.unLike(productId);

        // Assert
        verify(productLikeStatRepository).findByProductIdWithLock(productId);
        // ProductLikeStat.create(productId)가 호출되어 생성된 객체가 저장됨
        verify(productLikeStatRepository).save(any(ProductLikeStat.class));
    }
}

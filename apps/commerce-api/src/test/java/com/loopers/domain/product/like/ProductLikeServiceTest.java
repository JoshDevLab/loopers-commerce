package com.loopers.domain.product.like;

import com.loopers.domain.product.ProductChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @Mock ProductLikeRepository productLikeRepository;
    @Mock ProductChangedEventPublisher publisher;
    @Mock ProductLikeEventPublisher productLikeEventPublisher;
    @Mock ProductLike productLike;

    @InjectMocks ProductLikeService sut;

    @Test
    @DisplayName("existsByProductAndUser - 상품과 사용자로 좋아요 존재 여부를 확인한다")
    void existsByProductAndUser_returnsTrue() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        boolean result = sut.existsByProductAndUser(productId, userPk);

        // Assert
        assertThat(result).isTrue();
        verify(productLikeRepository).existsByProductIdAndUserPk(productId, userPk);
    }

    @Test
    @DisplayName("like - 이미 좋아요가 되어있지 않으면 좋아요를 생성하고 이벤트를 발행한다")
    void like_whenNotAlreadyLiked_createsLikeAndPublishesEvents() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);
        when(productLikeRepository.save(any(ProductLike.class))).thenReturn(productLike);

        // Act
        sut.like(productId, userPk);

        // Assert
        verify(productLikeRepository).existsByProductIdAndUserPk(productId, userPk);
        verify(productLikeRepository).save(any(ProductLike.class));
        verify(productLikeEventPublisher).publish(any(ProductLikeEvent.class));
        verify(publisher).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("like - 이미 좋아요가 되어있으면 아무것도 하지 않는다")
    void like_whenAlreadyLiked_doesNothing() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.like(productId, userPk);

        // Assert
        verify(productLikeRepository).existsByProductIdAndUserPk(productId, userPk);
        verify(productLikeRepository, never()).save(any(ProductLike.class));
        verify(productLikeEventPublisher, never()).publish(any(ProductLikeEvent.class));
        verify(productLikeEventPublisher, never()).publish(any(ProductUnLikeEvent.class));
        verify(publisher, never()).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 좋아요가 되어있으면 좋아요를 삭제하고 이벤트를 발행한다")
    void unLike_whenAlreadyLiked_deletesLikeAndPublishesEvents() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        verify(productLikeRepository).existsByProductIdAndUserPk(productId, userPk);
        verify(productLikeRepository).deleteByProductIdAndUserPk(productId, userPk);
        verify(productLikeEventPublisher).publish(any(ProductUnLikeEvent.class));
        verify(publisher).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 좋아요가 되어있지 않으면 아무것도 하지 않는다")
    void unLike_whenNotAlreadyLiked_doesNothing() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        verify(productLikeRepository).existsByProductIdAndUserPk(productId, userPk);
        verify(productLikeRepository, never()).deleteByProductIdAndUserPk(any(), any());
        verify(productLikeEventPublisher, never()).publish(any(ProductLikeEvent.class));
        verify(productLikeEventPublisher, never()).publish(any(ProductUnLikeEvent.class));
        verify(publisher, never()).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("getProductLikeByUser - 사용자의 좋아요 목록을 조회한다")
    void getProductLikeByUser_returnsUserLikes() {
        // Arrange
        Long userPk = 2L;
        List<ProductLike> expectedLikes = List.of(productLike);
        when(productLikeRepository.findByUserPk(userPk)).thenReturn(expectedLikes);

        // Act
        List<ProductLike> result = sut.getProductLikeByUser(userPk);

        // Assert
        assertThat(result).isSameAs(expectedLikes);
        verify(productLikeRepository).findByUserPk(userPk);
    }

    @Test
    @DisplayName("like - 좋아요 생성 시 ProductLike.create가 올바른 파라미터로 호출된다")
    void like_callsProductLikeCreateWithCorrectParameters() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);

        // Act
        sut.like(productId, userPk);

        // Assert
        verify(productLikeRepository).save(argThat(savedProductLike -> 
            savedProductLike.getProductId().equals(productId) && savedProductLike.getUserPk().equals(userPk)
        ));
    }

    @Test
    @DisplayName("unLike - 좋아요 삭제 시 올바른 파라미터로 deleteByProductIdAndUserPk가 호출된다")
    void unLike_callsDeleteByProductIdAndUserPkWithCorrectParameters() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        verify(productLikeRepository).deleteByProductIdAndUserPk(productId, userPk);
    }

    @Test
    @DisplayName("like - 이벤트 발행 순서 확인: ProductLikeEvent -> ProductChangedEvent")
    void like_publishesEventsInCorrectOrder() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);

        // Act
        sut.like(productId, userPk);

        // Assert
        var inOrder = inOrder(productLikeEventPublisher, publisher);
        inOrder.verify(productLikeEventPublisher).publish(any(ProductLikeEvent.class));
        inOrder.verify(publisher).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 이벤트 발행 순서 확인: ProductUnLikeEvent -> ProductChangedEvent")
    void unLike_publishesEventsInCorrectOrder() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        var inOrder = inOrder(productLikeEventPublisher, publisher);
        inOrder.verify(productLikeEventPublisher).publish(any(ProductUnLikeEvent.class));
        inOrder.verify(publisher).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("like - ProductLikeEvent가 올바른 productId로 발행된다")
    void like_publishesProductLikeEventWithCorrectProductId() {
        // Arrange
        Long productId = 123L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);

        // Act
        sut.like(productId, userPk);

        // Assert
        verify(productLikeEventPublisher).publish(argThat((ProductLikeEvent event) -> 
            event.productId().equals(123L)
        ));
    }

    @Test
    @DisplayName("unLike - ProductUnLikeEvent가 올바른 productId로 발행된다")
    void unLike_publishesProductUnLikeEventWithCorrectProductId() {
        // Arrange
        Long productId = 456L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        verify(productLikeEventPublisher).publish(argThat((ProductUnLikeEvent event) -> 
            event.productId().equals(456L)
        ));
    }

    @Test
    @DisplayName("like - ProductChangedEvent가 올바른 productId로 발행된다")
    void like_publishesProductChangedEventWithCorrectProductId() {
        // Arrange
        Long productId = 789L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);

        // Act
        sut.like(productId, userPk);

        // Assert
        verify(publisher).publish(argThat((ProductChangedEvent event) ->
            event.productId().equals(789L)
        ));
    }

    @Test
    @DisplayName("unLike - ProductChangedEvent가 올바른 productId로 발행된다")
    void unLike_publishesProductChangedEventWithCorrectProductId() {
        // Arrange
        Long productId = 101L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        verify(publisher).publish(argThat((ProductChangedEvent event) ->
            event.productId().equals(101L)
        ));
    }

    @Test
    @DisplayName("like - 저장소에 저장 후 이벤트를 발행한다")
    void like_publishesEventsAfterSaving() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(false);

        // Act
        sut.like(productId, userPk);

        // Assert
        var inOrder = inOrder(productLikeRepository, productLikeEventPublisher, publisher);
        inOrder.verify(productLikeRepository).save(any(ProductLike.class));
        inOrder.verify(productLikeEventPublisher).publish(any(ProductLikeEvent.class));
        inOrder.verify(publisher).publish(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 저장소에서 삭제 후 이벤트를 발행한다")
    void unLike_publishesEventsAfterDeleting() {
        // Arrange
        Long productId = 1L;
        Long userPk = 2L;
        when(productLikeRepository.existsByProductIdAndUserPk(productId, userPk)).thenReturn(true);

        // Act
        sut.unLike(productId, userPk);

        // Assert
        var inOrder = inOrder(productLikeRepository, productLikeEventPublisher, publisher);
        inOrder.verify(productLikeRepository).deleteByProductIdAndUserPk(productId, userPk);
        inOrder.verify(productLikeEventPublisher).publish(any(ProductUnLikeEvent.class));
        inOrder.verify(publisher).publish(any(ProductChangedEvent.class));
    }
}

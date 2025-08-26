package com.loopers.domain.product.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.user.User;
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
    @Mock ApplicationEventPublisher publisher;
    @Mock ProductLikeEventPublisher productLikeEventPublisher;
    @Mock Product product;
    @Mock User user;
    @Mock ProductLike productLike;

    @InjectMocks ProductLikeService sut;

    @Test
    @DisplayName("existsByProductAndUser - 상품과 사용자로 좋아요 존재 여부를 확인한다")
    void existsByProductAndUser_returnsTrue() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);

        // Act
        boolean result = sut.existsByProductAndUser(product, user);

        // Assert
        assertThat(result).isTrue();
        verify(productLikeRepository).existsByProductAndUser(product, user);
    }

    @Test
    @DisplayName("like - 이미 좋아요가 되어있지 않으면 좋아요를 생성하고 이벤트를 발행한다")
    void like_whenNotAlreadyLiked_createsLikeAndPublishesEvents() {
        // Arrange
        when(product.getId()).thenReturn(1L);
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(productLikeRepository.save(any(ProductLike.class))).thenReturn(productLike);

        // Act
        sut.like(product, user);

        // Assert
        verify(productLikeRepository).existsByProductAndUser(product, user);
        verify(productLikeRepository).save(any(ProductLike.class));
        verify(productLikeEventPublisher).publish(any(ProductLikeEvent.class));
        verify(publisher).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("like - 이미 좋아요가 되어있으면 아무것도 하지 않는다")
    void like_whenAlreadyLiked_doesNothing() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);

        // Act
        sut.like(product, user);

        // Assert
        verify(productLikeRepository).existsByProductAndUser(product, user);
        verify(productLikeRepository, never()).save(any(ProductLike.class));
        verify(productLikeEventPublisher, never()).publish(any(ProductLikeEvent.class));
        verify(productLikeEventPublisher, never()).publish(any(ProductUnLikeEvent.class));
        verify(publisher, never()).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 좋아요가 되어있으면 좋아요를 삭제하고 이벤트를 발행한다")
    void unLike_whenAlreadyLiked_deletesLikeAndPublishesEvents() {
        // Arrange
        when(product.getId()).thenReturn(1L);
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);

        // Act
        sut.unLike(product, user);

        // Assert
        verify(productLikeRepository).existsByProductAndUser(product, user);
        verify(productLikeRepository).deleteByProductAndUser(product, user);
        verify(productLikeEventPublisher).publish(any(ProductUnLikeEvent.class));
        verify(publisher).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 좋아요가 되어있지 않으면 아무것도 하지 않는다")
    void unLike_whenNotAlreadyLiked_doesNothing() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);

        // Act
        sut.unLike(product, user);

        // Assert
        verify(productLikeRepository).existsByProductAndUser(product, user);
        verify(productLikeRepository, never()).deleteByProductAndUser(any(), any());
        verify(productLikeEventPublisher, never()).publish(any(ProductLikeEvent.class));
        verify(productLikeEventPublisher, never()).publish(any(ProductUnLikeEvent.class));
        verify(publisher, never()).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("getProductLikeByUser - 사용자의 좋아요 목록을 조회한다")
    void getProductLikeByUser_returnsUserLikes() {
        // Arrange
        List<ProductLike> expectedLikes = List.of(productLike);
        when(productLikeRepository.findByUser(user)).thenReturn(expectedLikes);

        // Act
        List<ProductLike> result = sut.getProductLikeByUser(user);

        // Assert
        assertThat(result).isSameAs(expectedLikes);
        verify(productLikeRepository).findByUser(user);
    }

    @Test
    @DisplayName("like - 좋아요 생성 시 ProductLike.create가 올바른 파라미터로 호출된다")
    void like_callsProductLikeCreateWithCorrectParameters() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(product.getId()).thenReturn(1L);

        // Act
        sut.like(product, user);

        // Assert
        verify(productLikeRepository).save(argThat(savedProductLike -> 
            savedProductLike != null
        ));
    }

    @Test
    @DisplayName("unLike - 좋아요 삭제 시 올바른 파라미터로 deleteByProductAndUser가 호출된다")
    void unLike_callsDeleteByProductAndUserWithCorrectParameters() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);
        when(product.getId()).thenReturn(1L);

        // Act
        sut.unLike(product, user);

        // Assert
        verify(productLikeRepository).deleteByProductAndUser(product, user);
    }

    @Test
    @DisplayName("like - 이벤트 발행 순서 확인: ProductLikeEvent -> ProductChangedEvent")
    void like_publishesEventsInCorrectOrder() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(product.getId()).thenReturn(1L);

        // Act
        sut.like(product, user);

        // Assert
        var inOrder = inOrder(productLikeEventPublisher, publisher);
        inOrder.verify(productLikeEventPublisher).publish(any(ProductLikeEvent.class));
        inOrder.verify(publisher).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 이벤트 발행 순서 확인: ProductUnLikeEvent -> ProductChangedEvent")
    void unLike_publishesEventsInCorrectOrder() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);
        when(product.getId()).thenReturn(1L);

        // Act
        sut.unLike(product, user);

        // Assert
        var inOrder = inOrder(productLikeEventPublisher, publisher);
        inOrder.verify(productLikeEventPublisher).publish(any(ProductUnLikeEvent.class));
        inOrder.verify(publisher).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("like - ProductLikeEvent가 올바른 productId로 발행된다")
    void like_publishesProductLikeEventWithCorrectProductId() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(product.getId()).thenReturn(123L);

        // Act
        sut.like(product, user);

        // Assert
        verify(productLikeEventPublisher).publish(argThat((ProductLikeEvent event) -> 
            event.productId().equals(123L)
        ));
    }

    @Test
    @DisplayName("unLike - ProductUnLikeEvent가 올바른 productId로 발행된다")
    void unLike_publishesProductUnLikeEventWithCorrectProductId() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);
        when(product.getId()).thenReturn(456L);

        // Act
        sut.unLike(product, user);

        // Assert
        verify(productLikeEventPublisher).publish(argThat((ProductUnLikeEvent event) -> 
            event.productId().equals(456L)
        ));
    }

    @Test
    @DisplayName("like - ProductChangedEvent가 올바른 productId로 발행된다")
    void like_publishesProductChangedEventWithCorrectProductId() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(product.getId()).thenReturn(789L);

        // Act
        sut.like(product, user);

        // Assert
        verify(publisher).publishEvent(argThat((ProductChangedEvent event) -> 
            event.productId().equals(789L)
        ));
    }

    @Test
    @DisplayName("unLike - ProductChangedEvent가 올바른 productId로 발행된다")
    void unLike_publishesProductChangedEventWithCorrectProductId() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);
        when(product.getId()).thenReturn(101L);

        // Act
        sut.unLike(product, user);

        // Assert
        verify(publisher).publishEvent(argThat((ProductChangedEvent event) -> 
            event.productId().equals(101L)
        ));
    }

    @Test
    @DisplayName("like - 저장소에 저장 후 이벤트를 발행한다")
    void like_publishesEventsAfterSaving() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(product.getId()).thenReturn(1L);

        // Act
        sut.like(product, user);

        // Assert
        var inOrder = inOrder(productLikeRepository, productLikeEventPublisher, publisher);
        inOrder.verify(productLikeRepository).save(any(ProductLike.class));
        inOrder.verify(productLikeEventPublisher).publish(any(ProductLikeEvent.class));
        inOrder.verify(publisher).publishEvent(any(ProductChangedEvent.class));
    }

    @Test
    @DisplayName("unLike - 저장소에서 삭제 후 이벤트를 발행한다")
    void unLike_publishesEventsAfterDeleting() {
        // Arrange
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);
        when(product.getId()).thenReturn(1L);

        // Act
        sut.unLike(product, user);

        // Assert
        var inOrder = inOrder(productLikeRepository, productLikeEventPublisher, publisher);
        inOrder.verify(productLikeRepository).deleteByProductAndUser(product, user);
        inOrder.verify(productLikeEventPublisher).publish(any(ProductUnLikeEvent.class));
        inOrder.verify(publisher).publishEvent(any(ProductChangedEvent.class));
    }
}

package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    @Transactional(readOnly = true)
    public List<ProductOption> getProductOptionsByProductId(Long productId) {
        List<ProductOption> productOptions = productOptionRepository.findByProductId(productId);
        if (productOptions.isEmpty()) {
            throw new CoreException(ErrorType.PRODUCT_OPTION_NOT_FOUND, "상품을 찾을 수 없습니다. product id:" + productId);
        }
        return productOptions;
    }

    public ProductOption getOnSalesProductOption(Long productOptionId) {
        ProductOption productOption = productOptionRepository.findByIdWithLock(productOptionId)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_OPTION_NOT_FOUND,
                        "상품을 찾을 수 없습니다. product option id:" + productOptionId));
        productOption.isOnSales();
        return productOption;
    }

}

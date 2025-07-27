package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductCategoryTest {

    @Test
    void valueOfName_invalid() {
        CoreException exception = assertThrows(CoreException.class, () -> ProductCategory.valueOfName("INVALID_CATEGORY"));
        assertEquals("존재하지 않는 카테고리입니다: INVALID_CATEGORY", exception.getMessage());
        assertEquals(ErrorType.INVALID_PRODUCT_CATEGORY, exception.getErrorType());
    }

}

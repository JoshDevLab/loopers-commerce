package com.loopers.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TxReadHelper {

    @Transactional
    public <T> T inTx(Supplier<T> supplier) {
        return supplier.get();
    }
}

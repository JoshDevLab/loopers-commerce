package com.loopers.support;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.util.Map;

public class InMemoryDbSupport {
    private static Object getTargetObject(Object proxy) throws Exception {
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }

        if (proxy instanceof Advised) {
            Advised advised = (Advised) proxy;
            return advised.getTargetSource().getTarget();
        }

        throw new IllegalArgumentException();
    }

    public static void clearInMemoryStorage(Object possiblyProxiedRepo) throws Exception {
        Object realRepo = getTargetObject(possiblyProxiedRepo);
        Field field = realRepo.getClass().getDeclaredField("storage");
        field.setAccessible(true);
        Map<?, ?> storage = (Map<?, ?>) field.get(realRepo);
        if (storage == null) {
            throw new IllegalStateException();
        }
        storage.clear();
    }

}

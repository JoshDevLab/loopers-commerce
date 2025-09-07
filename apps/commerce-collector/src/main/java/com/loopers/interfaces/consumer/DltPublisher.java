package com.loopers.interfaces.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DltPublisher {

    private final KafkaTemplate<String, byte[]> dltKafkaTemplate;

    public void publish(String srcTopic, String key, Object payload, Map<String,String> meta,
                        Integer partition, Long offset) {
        final String dltTopic = srcTopic + ".DLT";

        RecordHeaders headers = new RecordHeaders();
        if (meta != null) meta.forEach((k, v) -> headers.add(k, v.getBytes(StandardCharsets.UTF_8)));
        if (partition != null) headers.add("x-src-partition", String.valueOf(partition).getBytes(StandardCharsets.UTF_8));
        if (offset != null) headers.add("x-src-offset", String.valueOf(offset).getBytes(StandardCharsets.UTF_8));

        byte[] value = payload == null ? new byte[0]
                : payload instanceof byte[] b ? b
                : payload instanceof String s ? s.getBytes(StandardCharsets.UTF_8)
                : payload.toString().getBytes(StandardCharsets.UTF_8);

        ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(dltTopic, null, key, value, headers);

        dltKafkaTemplate.send(record)
                .orTimeout(2, TimeUnit.SECONDS)
                .whenComplete((res, ex) -> {
                    if (ex != null) log.error("DLT publish failed: topic={}, key={}", dltTopic, key, ex);
                    else log.warn("DLT publish ok: {} key={}, meta(partition={}, offset={})",
                            dltTopic, key, partition, offset);
                });
    }
}

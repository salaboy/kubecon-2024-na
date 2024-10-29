package io.dapr.kubecon.examples.producer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackgroundAllocator {

    public static volatile Object memoryDrain;


    @Scheduled(fixedDelay=10)
    public void doAllocate() {
        // Let's give the GC some work
        for (int i=0; i<2000; i++) {
            memoryDrain = new byte[32*1024];
        }
    }
}

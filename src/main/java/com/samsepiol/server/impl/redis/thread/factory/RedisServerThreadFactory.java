package com.samsepiol.server.impl.redis.thread.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisServerThreadFactory implements ThreadFactory {
    private static final String POOL_NAME = "redis-server-pool";
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private static final String THREAD_NAME = "thread";
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private static final RedisServerThreadFactory INSTANCE = new RedisServerThreadFactory();
    private static final String PREFIX = String.format("%s-%s-%s-", POOL_NAME, poolNumber.getAndIncrement(), THREAD_NAME);

    @Override
    public Thread newThread(Runnable r) {
        return Thread.ofVirtual().name(String.format("%s-%s", PREFIX, threadNumber.getAndIncrement())).unstarted(r);
    }

    public static RedisServerThreadFactory getInstance() {
        return INSTANCE;
    }
}

package it.argosoft.poc.ratelimit.service;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveTransactionalHashCommands;
import io.quarkus.redis.datasource.keys.ReactiveTransactionalKeyCommands;
import io.quarkus.redis.datasource.keys.TransactionalKeyCommands;
import io.quarkus.redis.datasource.sortedset.*;
import io.quarkus.redis.datasource.transactions.TransactionResult;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TokenBucketRateLimitingService {

    private final static Logger log = LoggerFactory.getLogger(TokenBucketRateLimitingService.class);

    public static final int INTERVAL_SECONDS = 60;
    public static final int MAX_REQUESTS_ALLOWED = 10;
    private final ValueCommands<String, Integer> rateLimitCounter;
    private final ValueCommands<String, Long> lastResetTimeCommand;

    private final SortedSetCommands<String, Long> sortedSetCommands;

    private final RedisDataSource redisDataSource;
    private final ReactiveRedisDataSource reactiveRedisDataSource;

    public TokenBucketRateLimitingService(RedisDataSource ds, ReactiveRedisDataSource reactiveRedisDataSource) {
        rateLimitCounter = ds.value(Integer.class);
        lastResetTimeCommand = ds.value(Long.class);
        sortedSetCommands = ds.sortedSet(Long.class);

        this.reactiveRedisDataSource = reactiveRedisDataSource;

        this.redisDataSource = ds;
    }

    public Uni<Boolean> shouldAllowReactive(String key) {

        String _key = "rl_"+key;
        long now = ZonedDateTime.now().toEpochSecond();
        long interval = now - INTERVAL_SECONDS;

        Uni<TransactionResult> transactionResult = this.reactiveRedisDataSource.withTransaction(tx -> {
            ReactiveTransactionalKeyCommands<String> keys = tx.key(String.class);
            ReactiveTransactionalSortedSetCommands<String, Long> z = tx.sortedSet(String.class, Long.class);

            return z.zremrangebyscore(_key, ScoreRange.from(0, interval))
                    .chain(() -> {
                        log.debug("ZADD");
                        return z.zadd(_key, now, now);
                    })
                    .chain(() -> {
                        log.debug("ZRANGEWITHSCORE");
                        return z.zrangeWithScores(_key, 0, -1);
                    })
                    .chain(() -> {
                        log.debug("EXPIRE");
                        return keys.expire(_key, INTERVAL_SECONDS);
                    }).log();

        });

        return transactionResult.map(r -> {
            if (r.discarded()) return false;

            Response scoredValues = r.get(2);

            return scoredValues.size() < MAX_REQUESTS_ALLOWED;
        });
    }

    /**
     * @param k La chiave che si vuole controllare, può essere un apikey, access_token, username...
     * @return true if request should be allowed
     */
    public boolean shouldAllow(String key) {

        String _key = "rl_"+key;
        long now = ZonedDateTime.now().toEpochSecond();
        long interval = now - INTERVAL_SECONDS;

//        KeyCommands<String> keys = redisDataSource.key();
//        SortedSetCommands<String, Long> z = redisDataSource.sortedSet(String.class, Long.class);
//        // remove all element expired (not in current window)
//        z.zremrangebyscore(_key, ScoreRange.from(0, interval));
//        // add this request
//        z.zadd(_key, now, now);
//        // count all request
//        List<ScoredValue<Long>> scoredValues = z.zrangeWithScores(_key, 0, -1);
//        // expire
//        keys.expire(_key, INTERVAL_SECONDS);

        TransactionResult transactionResult = redisDataSource.withTransaction(tx -> {
            TransactionalKeyCommands<String> keys = tx.key();
            TransactionalSortedSetCommands<String, Long> z = tx.sortedSet(String.class, Long.class);
            // remove all element expired (not in current window)
            z.zremrangebyscore(_key, ScoreRange.from(0, interval));
            // add this request
            z.zadd(_key, now, now);
            // count all request
            z.zrangeWithScores(_key, 0, -1);
            // expire
            keys.expire(_key, INTERVAL_SECONDS);
        });

        if (transactionResult.discarded()) return false;

//         get result of zrangeWithScores
        List<ScoredValue<Long>> scoredValues = transactionResult.get(2);

        return scoredValues.size() < MAX_REQUESTS_ALLOWED;

//        String lastResetTimeKey = key + "_last_reset_time";
//        String counterKey = key + "_counter";
//
//        // get lastResetTime value
//        Long lastResetTime = Optional.ofNullable(lastResetTimeCommand.get(lastResetTimeKey)).orElse(0L);
//
//        // if the key is not available, i.e., this is the first request, lastResetTime will be set to 0 and counter be set to max requests allowed
//        // check if time window since last counter reset has elapsed
//        if (ZonedDateTime.now().toEpochSecond()-lastResetTime>= INTERVAL_SECONDS) {
//            // todo farlo in transazione
//            rateLimitCounter.set(counterKey, MAX_REQUESTS_ALLOWED);
//            lastResetTimeCommand.setex(lastResetTimeKey, INTERVAL_SECONDS+5, ZonedDateTime.now().toEpochSecond());
//        } else {
//            Integer requestLeft = rateLimitCounter.get(counterKey);
//            if (requestLeft <= 0) {
//                return false;
//            }
//        }
//
//        rateLimitCounter.decr(counterKey);
//        return true;
    }

}

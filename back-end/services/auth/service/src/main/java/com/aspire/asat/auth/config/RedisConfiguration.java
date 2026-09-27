package com.aspire.asat.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.util.StringUtils;

import java.time.Duration;

@EnableRedisRepositories
@Configuration
public class RedisConfiguration {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.pool.max.connection}")
    private Integer maxConnection;

    @Value("${redis.pool.max.idle.connection}")
    private Integer maxIdleConnection;

    @Value("${redis.pool.min.idle.connection}")
    private Integer minIdleConnection;

    @Value("${redis.database.index}")
    private int databaseIndex;

    /**
     * How long to wait for a Redis command to complete before treating the
     * instance as unavailable and falling back to MongoDB.
     * Default: 1 second — fast enough to not stall a login, long enough to
     * tolerate transient latency spikes.
     */
    @Value("${redis.command.timeout.seconds:1}")
    private long commandTimeoutSeconds;

    /**
     * TCP connect timeout, kept shorter than the command timeout to fail fast
     * when the Redis host is completely unreachable.
     */
    @Value("${redis.connect.timeout.seconds:1}")
    private long connectTimeoutSeconds;

    @Bean
    public GenericObjectPoolConfig<Void> genericObjectPoolConfig() {
        final GenericObjectPoolConfig<Void> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(maxConnection);
        genericObjectPoolConfig.setMaxIdle(maxIdleConnection);
        genericObjectPoolConfig.setMinIdle(minIdleConnection);
        return genericObjectPoolConfig;
    }

    @Bean
    public RedisConnectionFactory getConnectionFactory(GenericObjectPoolConfig<Void> genericObjectPoolConfig) {
        final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        if (StringUtils.hasLength(password)) {
            redisStandaloneConfiguration.setPassword(password);
        }
        redisStandaloneConfiguration.setDatabase(databaseIndex);

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();

        io.lettuce.core.ClientOptions clientOptions = io.lettuce.core.ClientOptions.builder()
                .socketOptions(socketOptions)
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(commandTimeoutSeconds)))
                .disconnectedBehavior(io.lettuce.core.ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
                .poolConfig(genericObjectPoolConfig)
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(commandTimeoutSeconds))
                .build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
    }

    @Primary
    @Bean(name = "redisTemplate")
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        final RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}

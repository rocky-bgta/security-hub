package com.aspire.asat.gateway.repository.redis;

import com.aspire.asat.gateway.entity.redis.RedisAccessToken;
import com.aspire.asat.gateway.util.JacksonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {
    public static final String FOLDER = "token:user-";

    private final RedisTemplate<String, String> template;


    public RedisAccessToken get(String tokenId) {
        return JacksonUtil.jsonToInstance(template.opsForValue().get(FOLDER.concat(tokenId)), RedisAccessToken.class);
    }

    public void delete(String tokenId) {
        template.delete(FOLDER.concat(tokenId));
    }
}

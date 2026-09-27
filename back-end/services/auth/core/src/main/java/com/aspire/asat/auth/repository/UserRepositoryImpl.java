package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.AspireUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Custom implementation for {@link UserRepositoryCustom}. Uses list-based lookup so that
 * when duplicate usernames exist (case-insensitive), we return the first match instead of
 * throwing "non unique result" from MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<AspireUser> findByUsernameIgnoreCase(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String trimmed = username.trim();
        Query query = new Query(
                Criteria.where("username").regex("^" + Pattern.quote(trimmed) + "$", "i")
        );
        List<AspireUser> users = mongoTemplate.find(query, AspireUser.class);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        if (users.size() > 1) {
            log.warn("Duplicate username found for '{}' ({} records). Consider adding a unique case-insensitive index on username and cleaning duplicates.",
                    username, users.size());
        }
        return Optional.of(users.get(0));
    }
}

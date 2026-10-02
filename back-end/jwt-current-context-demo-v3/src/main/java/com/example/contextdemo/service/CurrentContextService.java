package com.example.contextdemo.service;

import com.example.contextdemo.model.CurrentContext;
import com.example.contextdemo.repository.CurrentContextRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CurrentContextService {

    private final CurrentContextRepository repository;

    public CurrentContextService(CurrentContextRepository repository) {
        this.repository = repository;
    }

    public CurrentContext createContext(String userId, String jwtId, String roomId) {
        String normalizedRoomId = normalizeRoomId(roomId);
        CurrentContext context = new CurrentContext(
                userId,
                jwtId,
                normalizedRoomId,
                true,
                Instant.now());
        return repository.save(context);
    }

    public void revokeContext(String contextId, String userId) {
        CurrentContext context = repository.findById(contextId)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Context not found for this user."));
        context.revoke();
        repository.save(context);
    }

    public void revokeAllForJwt(String jwtId) {
        repository.findByJwtIdAndActiveTrue(jwtId).forEach(context -> {
            context.revoke();
            repository.save(context);
        });
    }

    private String normalizeRoomId(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is required.");
        }
        return roomId.trim().toUpperCase();
    }
}

package com.example.contextdemo.service;

import com.example.contextdemo.repository.CurrentContextRepository;
import org.springframework.stereotype.Service;

@Service
public class CurrentContextValidator {

    private final CurrentContextRepository repository;

    public CurrentContextValidator(CurrentContextRepository repository) {
        this.repository = repository;
    }

    public boolean isValidContext(String contextId, String userId, String roomId) {
        if (contextId == null || contextId.isBlank()
                || roomId == null || roomId.isBlank()) {
            return false;
        }
        return repository.findByIdAndUserIdAndRoomIdAndActiveTrue(
                contextId,
                userId,
                roomId.trim().toUpperCase()).isPresent();
    }
}

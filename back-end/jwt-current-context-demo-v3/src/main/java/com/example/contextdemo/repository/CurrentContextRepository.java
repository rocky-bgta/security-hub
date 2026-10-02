package com.example.contextdemo.repository;

import com.example.contextdemo.model.CurrentContext;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CurrentContextRepository
        extends MongoRepository<CurrentContext, String> {

    Optional<CurrentContext> findByIdAndUserIdAndRoomIdAndActiveTrue(
            String id,
            String userId,
            String roomId);

    List<CurrentContext> findByJwtIdAndActiveTrue(String jwtId);
}

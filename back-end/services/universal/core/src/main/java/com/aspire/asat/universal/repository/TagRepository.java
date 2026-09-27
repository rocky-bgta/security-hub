package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends MongoRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    List<Tag> findByIdIn(List<UUID> ids);
}

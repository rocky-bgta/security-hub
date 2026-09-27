package com.aspire.asat.registration.repository.impl;

import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.RoleRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryCustomImpl implements RoleRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Role> searchRoles(String search, String sortBy, Sort.Direction direction) {
        Query query = new Query();
        if(search != null && !search.isEmpty()){
            query.addCriteria(Criteria.where("roleName").regex(search, "i"));
        }
        query.with(Sort.by(direction, sortBy));
        return mongoTemplate.find(query, Role.class);
    }
}

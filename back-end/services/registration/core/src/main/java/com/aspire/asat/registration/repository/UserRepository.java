package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {

    @Query("{ $or: [ { 'firstName': { $regex: ?0, $options: 'i' } }, { 'lastName': { $regex: ?0, $options: 'i' } }, { 'email': { $regex: ?0, $options: 'i' } } ] }")
    List<User> findByTextSearch(String text);

    @Query(value = "{ 'companyName': ?0 }",fields = "{ 'firstName': 1, 'lastName': 1, 'email': 1, 'country': 1, 'role': 1, 'userStatus': 1 }")
    List<User> findUsersByCompanyName(String companyName);

}

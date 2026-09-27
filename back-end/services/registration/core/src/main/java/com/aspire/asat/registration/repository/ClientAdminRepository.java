package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.ClientAdmin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientAdminRepository extends MongoRepository<ClientAdmin, String> {

    Optional<ClientAdmin> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<ClientAdmin> findByClientAdminId(String clientAdminId);

    List<ClientAdmin> findByClientAdminId(String clientAdminId, Pageable pageable);

    long countByClientAdminId(String clientAdminId);

    @Query(value = "{ 'clientAdminId' : ?0 }", fields = "{ 'id' : 1 }")
    List<ClientAdmin> findIdsOnlyByClientAdminId(String clientAdminId);

    Optional<ClientAdmin> findByMspId(String mspId);

    List<ClientAdmin> findAllByMspId(String mspId);

    long countByMspId(String mspId);

    List<ClientAdmin> findByCountry(String country);

    @Query(value = "{ 'email': { $regex: ?0, $options: 'i' } }")
    List<ClientAdmin> findByEmailDomainRegex(String domainPattern);

    /**
     * True if another client admin (not {@code id}) already uses this organization name (case-insensitive).
     */
    boolean existsByOrganizationNameIgnoreCaseAndIdNot(String organizationName, String id);

}

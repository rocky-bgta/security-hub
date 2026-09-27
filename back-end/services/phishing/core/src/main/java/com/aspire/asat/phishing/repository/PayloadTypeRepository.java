package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.PayloadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for configurable {@link PayloadType} documents.
 */
@Repository
public interface PayloadTypeRepository extends MongoRepository<PayloadType, String> {

    @Query("{ 'isActive': { $ne: false }, $or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }")
    Page<PayloadType> findWhereEffectiveActiveByEmailChannel(Pageable pageable);

    @Query("{ 'isActive': false, $or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }")
    Page<PayloadType> findWhereInactiveByEmailChannel(Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false }, "
            + "$or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }")
    Page<PayloadType> searchByNameWhereEffectiveActiveByEmailChannel(String search, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false, "
            + "$or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }")
    Page<PayloadType> searchByNameWhereInactiveByEmailChannel(String search, Pageable pageable);

    @Query("{ 'isActive': { $ne: false }, channel: 'SMS' }")
    Page<PayloadType> findWhereEffectiveActiveBySmsChannel(Pageable pageable);

    @Query("{ 'isActive': false, channel: 'SMS' }")
    Page<PayloadType> findWhereInactiveBySmsChannel(Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false }, channel: 'SMS' }")
    Page<PayloadType> searchByNameWhereEffectiveActiveBySmsChannel(String search, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false, channel: 'SMS' }")
    Page<PayloadType> searchByNameWhereInactiveBySmsChannel(String search, Pageable pageable);

    @Query("{ 'isActive': { $ne: false }, channel: 'VOICE' }")
    Page<PayloadType> findWhereEffectiveActiveByVoiceChannel(Pageable pageable);

    @Query("{ 'isActive': false, channel: 'VOICE' }")
    Page<PayloadType> findWhereInactiveByVoiceChannel(Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false }, channel: 'VOICE' }")
    Page<PayloadType> searchByNameWhereEffectiveActiveByVoiceChannel(String search, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false, channel: 'VOICE' }")
    Page<PayloadType> searchByNameWhereInactiveByVoiceChannel(String search, Pageable pageable);

    @Query(value = "{ 'isActive': { $ne: false }, $or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }",
            count = true)
    long countWhereEffectiveActiveByEmailChannel();

    @Query(value = "{ 'isActive': false, $or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }",
            count = true)
    long countWhereInactiveByEmailChannel();

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false }, "
            + "$or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }", count = true)
    long countSearchByNameWhereEffectiveActiveByEmailChannel(String search);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false, "
            + "$or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }", count = true)
    long countSearchByNameWhereInactiveByEmailChannel(String search);

    @Query(value = "{ 'isActive': { $ne: false }, channel: 'SMS' }", count = true)
    long countWhereEffectiveActiveBySmsChannel();

    @Query(value = "{ 'isActive': false, channel: 'SMS' }", count = true)
    long countWhereInactiveBySmsChannel();

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false }, channel: 'SMS' }",
            count = true)
    long countSearchByNameWhereEffectiveActiveBySmsChannel(String search);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false, channel: 'SMS' }", count = true)
    long countSearchByNameWhereInactiveBySmsChannel(String search);

    @Query(value = "{ 'isActive': { $ne: false }, channel: 'VOICE' }", count = true)
    long countWhereEffectiveActiveByVoiceChannel();

    @Query(value = "{ 'isActive': false, channel: 'VOICE' }", count = true)
    long countWhereInactiveByVoiceChannel();

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false }, channel: 'VOICE' }",
            count = true)
    long countSearchByNameWhereEffectiveActiveByVoiceChannel(String search);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false, channel: 'VOICE' }", count = true)
    long countSearchByNameWhereInactiveByVoiceChannel(String search);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, "
            + "$or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }",
            exists = true)
    boolean existsByNameIgnoreCaseAndEmailChannel(String namePattern);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, channel: 'SMS' }", exists = true)
    boolean existsByNameIgnoreCaseAndSmsChannel(String namePattern);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, _id: { $ne: ?1 }, "
            + "$or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }",
            exists = true)
    boolean existsByNameIgnoreCaseAndEmailChannelAndIdNot(String namePattern, String id);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, _id: { $ne: ?1 }, channel: 'SMS' }",
            exists = true)
    boolean existsByNameIgnoreCaseAndSmsChannelAndIdNot(String namePattern, String id);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, channel: 'VOICE' }", exists = true)
    boolean existsByNameIgnoreCaseAndVoiceChannel(String namePattern);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, _id: { $ne: ?1 }, channel: 'VOICE' }",
            exists = true)
    boolean existsByNameIgnoreCaseAndVoiceChannelAndIdNot(String namePattern, String id);

    @Query("{ 'isDefault': true, $or: [ { channel: 'EMAIL' }, { channel: { $exists: false } }, { channel: null } ] }")
    List<PayloadType> findAllByIsDefaultTrueAndEmailChannel();

    @Query("{ 'isDefault': true, channel: 'SMS' }")
    List<PayloadType> findAllByIsDefaultTrueAndSmsChannel();

    @Query("{ 'isDefault': true, channel: 'VOICE' }")
    List<PayloadType> findAllByIsDefaultTrueAndVoiceChannel();
}

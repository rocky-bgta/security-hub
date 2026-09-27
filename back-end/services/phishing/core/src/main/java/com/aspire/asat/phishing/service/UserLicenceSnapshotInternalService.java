package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.UserLicenceSnapshotRequest;

public interface UserLicenceSnapshotInternalService {

    /**
     * Syncs Registration end-user profile fields onto all matching licence rows.
     *
     * @return matched document count (0 if user has no licences)
     */
    long syncUserSnapshot(UserLicenceSnapshotRequest request);
}

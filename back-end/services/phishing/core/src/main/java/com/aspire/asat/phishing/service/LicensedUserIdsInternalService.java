package com.aspire.asat.phishing.service;

import java.util.List;

public interface LicensedUserIdsInternalService {

    List<String> getLicensedUserIds(String clientId, String productPackageId);
}

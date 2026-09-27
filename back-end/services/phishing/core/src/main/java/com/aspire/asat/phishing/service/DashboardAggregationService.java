package com.aspire.asat.phishing.service;

public interface DashboardAggregationService {
    void aggregateForAllClients();

    /**
     * Rebuilds admin dashboard aggregate for one client.
     *
     * @return number of {@code user_risk_profiles} rows included in the rollup
     */
    int aggregateForClient(String clientId);
}

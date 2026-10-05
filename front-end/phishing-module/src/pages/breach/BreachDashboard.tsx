import { Card } from 'common/Card';
import SeverityBadge from 'features/breach/SeverityBadge';
import { useBreaches } from 'hooks/UseBreaches';
import {
  BreachSeverity,
  BreachStatus,
  IBreachRecord,
  IRecipientBreach,
  RecipientBreachStatus,
  getStatusColor,
  getStatusLabel,
} from 'models/Breach';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';

/**
 * Breach Detection Dashboard
 * Provides an overview of data breaches and affected recipients
 * Based on Task-08 requirements (AC-01, AC-03, AC-06, AC-07)
 */
const BreachDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { loading, fetchBreaches, fetchRecipientBreaches } = useBreaches();

  // Dashboard data state
  const [recentBreaches, setRecentBreaches] = useState<IBreachRecord[]>([]);
  const [pendingRecipients, setPendingRecipients] = useState<
    IRecipientBreach[]
  >([]);
  const [stats, setStats] = useState({
    totalBreaches: 0,
    activeBreaches: 0,
    resolvedBreaches: 0,
    totalRecipients: 0,
    pendingRecipients: 0,
    notifiedRecipients: 0,
    highSeverityCount: 0,
    mediumSeverityCount: 0,
    lowSeverityCount: 0,
  });

  // Load dashboard data
  const loadDashboardData = useCallback(async () => {
    try {
      // Fetch recent breaches
      const breachResult = await fetchBreaches({
        offset: 0,
        pageSize: 5,
      });

      if (breachResult) {
        setRecentBreaches(breachResult.items || []);

        // Calculate breach stats
        const items = breachResult.items || [];
        const total = breachResult.total || 0;
        const active = items.filter(
          b => b.status !== BreachStatus.RESOLVED,
        ).length;
        const resolved = items.filter(
          b => b.status === BreachStatus.RESOLVED,
        ).length;
        const high = items.filter(
          b => b.severity === BreachSeverity.HIGH,
        ).length;
        const medium = items.filter(
          b => b.severity === BreachSeverity.MEDIUM,
        ).length;
        const low = items.filter(b => b.severity === BreachSeverity.LOW).length;

        setStats(prev => ({
          ...prev,
          totalBreaches: total,
          activeBreaches: active,
          resolvedBreaches: resolved,
          highSeverityCount: high,
          mediumSeverityCount: medium,
          lowSeverityCount: low,
        }));
      }

      // Fetch pending recipients
      const recipientResult = await fetchRecipientBreaches({
        offset: 0,
        pageSize: 5,
        status: RecipientBreachStatus.PENDING,
      });

      if (recipientResult) {
        setPendingRecipients(recipientResult.items || []);
        setStats(prev => ({
          ...prev,
          totalRecipients: recipientResult.total || 0,
          pendingRecipients: (recipientResult.items || []).length,
        }));
      }
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      toast.error('Failed to load dashboard data');
    }
  }, [fetchBreaches, fetchRecipientBreaches]);

  useEffect(() => {
    setTimeout(() => {
      loadDashboardData();
    }, 0);
  }, [loadDashboardData]);

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  // Navigation handlers
  const handleViewAllBreaches = () => {
    navigate(routes.breachManagement.path);
  };

  const handleViewAllRecipients = () => {
    navigate(routes.recipientBreaches.path);
  };

  const handleBreachClick = (_breachId: string) => {
    navigate(routes.breachManagement.path);
  };

  return (
    <div className="min-h-screen p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-foreground">
          Breach Intelligence Dashboard
        </h1>
        <p className="text-muted-foreground">
          Monitor data breaches and manage affected users in your organization
        </p>
      </div>

      {/* Stats Cards */}
      <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
        {/* Total Breaches */}
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">
                Total Breaches
              </p>
              <p className="mt-1 text-3xl font-bold text-foreground">
                {stats.totalBreaches}
              </p>
            </div>
            <div className="rounded-full bg-primary/10 p-3">
              <svg
                className="size-6 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                />
              </svg>
            </div>
          </div>
          <div className="mt-4 flex items-center gap-4 text-sm">
            <span className="font-medium text-destructive">
              {stats.activeBreaches} active
            </span>
            <span className="font-medium text-green-600">
              {stats.resolvedBreaches} resolved
            </span>
          </div>
        </Card>

        {/* Affected Recipients */}
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">
                Affected Recipients
              </p>
              <p className="mt-1 text-3xl font-bold text-foreground">
                {stats.totalRecipients}
              </p>
            </div>
            <div className="rounded-full bg-primary/10 p-3">
              <svg
                className="size-6 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <span className="text-sm font-medium text-primary">
              {stats.pendingRecipients} pending action
            </span>
          </div>
        </Card>

        {/* High Severity */}
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">
                High Severity
              </p>
              <p className="mt-1 text-3xl font-bold text-destructive">
                {stats.highSeverityCount}
              </p>
            </div>
            <div className="rounded-full bg-destructive/10 p-3">
              <svg
                className="size-6 text-destructive"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                />
              </svg>
            </div>
          </div>
          <div className="mt-4 text-sm text-muted-foreground">
            Requires immediate attention
          </div>
        </Card>

        {/* Actions Required */}
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">
                Actions Required
              </p>
              <p className="mt-1 text-3xl font-bold text-foreground">
                {
                  recentBreaches.filter(
                    b => b.status === BreachStatus.ACTION_REQUIRED,
                  ).length
                }
              </p>
            </div>
            <div className="rounded-full bg-primary/10 p-3">
              <svg
                className="size-6 text-primary"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
                />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <button
              onClick={handleViewAllBreaches}
              className="text-sm font-medium text-primary hover:text-primary/80"
            >
              View all breaches →
            </button>
          </div>
        </Card>
      </div>

      {/* Severity Distribution */}
      <div className="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card className="p-6">
          <h3 className="mb-4 text-lg font-semibold text-foreground">
            Severity Distribution
          </h3>
          <div className="space-y-4">
            {/* High */}
            <div>
              <div className="mb-1 flex items-center justify-between">
                <span className="text-sm font-medium text-muted-foreground">
                  High
                </span>
                <span className="text-sm font-medium text-destructive">
                  {stats.highSeverityCount}
                </span>
              </div>
              <div className="h-2 w-full rounded-full bg-muted-foreground/10">
                <div
                  className="h-2 rounded-full bg-destructive transition-all duration-300"
                  style={{
                    width:
                      stats.totalBreaches > 0
                        ? `${(stats.highSeverityCount / stats.totalBreaches) * 100}%`
                        : '0%',
                  }}
                />
              </div>
            </div>

            {/* Medium */}
            <div>
              <div className="mb-1 flex items-center justify-between">
                <span className="text-sm font-medium text-muted-foreground">
                  Medium
                </span>
                <span className="text-sm font-medium text-primary">
                  {stats.mediumSeverityCount}
                </span>
              </div>
              <div className="h-2 w-full rounded-full bg-muted-foreground/10">
                <div
                  className="h-2 rounded-full bg-primary transition-all duration-300"
                  style={{
                    width:
                      stats.totalBreaches > 0
                        ? `${(stats.mediumSeverityCount / stats.totalBreaches) * 100}%`
                        : '0%',
                  }}
                />
              </div>
            </div>

            {/* Low */}
            <div>
              <div className="mb-1 flex items-center justify-between">
                <span className="text-sm font-medium text-muted-foreground">
                  Low
                </span>
                <span className="text-sm font-medium text-primary">
                  {stats.lowSeverityCount}
                </span>
              </div>
              <div className="h-2 w-full rounded-full bg-muted-foreground/10">
                <div
                  className="h-2 rounded-full bg-primary transition-all duration-300"
                  style={{
                    width:
                      stats.totalBreaches > 0
                        ? `${(stats.lowSeverityCount / stats.totalBreaches) * 100}%`
                        : '0%',
                  }}
                />
              </div>
            </div>
          </div>
        </Card>

        {/* Quick Actions */}
        <Card className="p-6 lg:col-span-2">
          <h3 className="mb-4 text-lg font-semibold text-foreground">
            Quick Actions
          </h3>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <button
              onClick={handleViewAllBreaches}
              className="flex items-center gap-3 rounded-lg p-4 transition-colors"
            >
              <div className="rounded-lg bg-primary/10 p-2">
                <svg
                  className="size-5 text-primary"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 10h16M4 14h16M4 18h16"
                  />
                </svg>
              </div>
              <div className="text-left">
                <p className="text-sm font-medium text-foreground">
                  View Breaches
                </p>
                <p className="text-xs text-muted-foreground">
                  Manage all breaches
                </p>
              </div>
            </button>

            <button
              onClick={handleViewAllRecipients}
              className="flex items-center gap-3 rounded-lg p-4 transition-colors"
            >
              <div className="rounded-lg bg-primary/10 p-2">
                <svg
                  className="size-5 text-primary"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                  />
                </svg>
              </div>
              <div className="text-left">
                <p className="text-sm font-medium text-foreground">
                  Recipients
                </p>
                <p className="text-xs text-muted-foreground">
                  Manage affected users
                </p>
              </div>
            </button>

            <button
              onClick={() =>
                navigate(routes.breachManagement.path + '?tab=settings')
              }
              className="flex items-center gap-3 rounded-lg p-4 transition-colors"
            >
              <div className="rounded-lg p-2">
                <svg
                  className="size-5 text-primary"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
              </div>
              <div className="text-left">
                <p className="text-sm font-medium text-foreground">Settings</p>
                <p className="text-xs text-muted-foreground">
                  Configure detection
                </p>
              </div>
            </button>
          </div>
        </Card>
      </div>

      {/* Recent Breaches & Pending Recipients */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent Breaches */}
        <Card className="p-6">
          <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
            <h3 className="text-lg font-semibold text-foreground">
              Recent Breaches
            </h3>
            <button
              onClick={handleViewAllBreaches}
              className="text-sm font-medium text-primary hover:text-primary/80"
            >
              View all
            </button>
          </div>

          {loading ? (
            <div className="space-y-3 p-6">
              {[...Array(3)].map((_, i) => (
                <div
                  key={i}
                  className="h-16 animate-pulse rounded bg-muted-foreground/10"
                />
              ))}
            </div>
          ) : recentBreaches.length === 0 ? (
            <div className="p-6 text-center text-muted-foreground">
              <svg
                className="mx-auto size-12 text-muted-foreground"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                />
              </svg>
              <p className="mt-2 text-sm">No breaches detected</p>
            </div>
          ) : (
            <div className="divide-y divide-muted-foreground/10">
              {recentBreaches.map(breach => (
                <div
                  key={breach.id}
                  onClick={() => handleBreachClick(breach.id)}
                  className="hover: cursor-pointer px-6 py-4 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <p className="truncate text-sm font-medium text-foreground">
                          {breach.domain}
                        </p>
                        <SeverityBadge severity={breach.severity} size="sm" />
                      </div>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {breach.breachName || 'Unnamed breach'} •{' '}
                        {formatDate(breach.dateOfBreach)}
                      </p>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-sm font-semibold text-foreground">
                          {breach.recipientCount}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          affected
                        </p>
                      </div>
                      <span
                        className="rounded px-2 py-1 text-xs font-medium"
                        style={{
                          backgroundColor: `${getStatusColor(breach.status)}15`,
                          color: getStatusColor(breach.status),
                        }}
                      >
                        {getStatusLabel(breach.status)}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* Pending Recipients */}
        <Card className="p-6">
          <div className="flex items-center justify-between px-6 py-4">
            <h3 className="text-lg font-semibold text-foreground">
              Pending Actions
            </h3>
            <button
              onClick={handleViewAllRecipients}
              className="text-sm font-medium text-primary hover:text-primary/80"
            >
              View all
            </button>
          </div>

          {loading ? (
            <div className="space-y-3 p-6">
              {[...Array(3)].map((_, i) => (
                <div
                  key={i}
                  className="h-16 animate-pulse rounded bg-muted-foreground/10"
                />
              ))}
            </div>
          ) : pendingRecipients.length === 0 ? (
            <div className="p-6 text-center text-muted-foreground">
              <svg
                className="mx-auto size-12 text-muted-foreground"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
              <p className="mt-2 text-sm">No pending actions</p>
            </div>
          ) : (
            <div className="divide-y divide-muted-foreground/10">
              {pendingRecipients.map(recipient => (
                <div
                  key={recipient.id}
                  onClick={handleViewAllRecipients}
                  className="hover: cursor-pointer px-6 py-4 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="flex size-10 items-center justify-center rounded-full bg-muted-foreground/10">
                        <span className="text-sm font-medium text-gray-600">
                          {recipient.firstName?.[0] ||
                            recipient.email?.[0]?.toUpperCase()}
                        </span>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {recipient.fullName ||
                            `${recipient.firstName} ${recipient.lastName}`}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {recipient.email}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      {recipient.breachCount > 1 && (
                        <span className="inline-flex items-center rounded bg-destructive/10 px-2 py-0.5 text-xs font-medium text-destructive">
                          {recipient.breachCount} breaches
                        </span>
                      )}
                      <p className="mt-1 text-xs font-medium text-primary">
                        Pending notification
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default BreachDashboard;

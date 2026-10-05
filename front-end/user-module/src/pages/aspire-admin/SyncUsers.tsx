import { safeRedirect } from 'home-module/security';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { useEffect, useState, useMemo } from 'react';

import {
  AlertCircle,
  CheckCircle,
  Database,
  RefreshCw,
  Search,
  Users,
  X,
  CheckCheck,
} from 'lucide-react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import { isSuccessResponse } from 'utils/Helper';
import { ISyncGroup } from 'models/Vat';
import { toast } from 'react-toastify';

interface IImportUsersResponse {
  message: string;
  statusCode: number;
  data: {
    totalUsersProcessed: number;
    usersCreated: number;
    usersSkipped: number;
    usersFailed: number;
    completedAt: string;
  };
}

const SyncUsers = () => {
  const apiClient = useAPI();
  const [syncMethod, setSyncMethod] = useState('ad');
  const [groups, setGroups] = useState<Array<ISyncGroup>>([]);
  const [selectedGroupIds, setSelectedGroupIds] = useState<Set<string>>(
    new Set(),
  );
  const [searchQuery, setSearchQuery] = useState('');
  const [syncStatus, setSyncStatus] = useState<
    'idle' | 'syncing' | 'success' | 'error'
  >('idle');
  const [importStatus, setImportStatus] = useState<
    'idle' | 'importing' | 'success' | 'error'
  >('idle');

  const [syncResults, setSyncResults] = useState<{
    total: number;
    synced: number;
    failed: number;
    errors: string[];
  } | null>(null);

  const [importResults, setImportResults] = useState<
    IImportUsersResponse['data'] | null
  >(null);

  const handleSync = async () => {
    try {
      setSyncStatus('syncing');
      const response = await apiClient.get(API_END_POINTS.SYNC_USERS);
      if (isSuccessResponse(response.statusCode)) {
        if (response.data.connected) {
          setSyncStatus('success');
          fetchGroups();
        } else {
          fetchAuthUrl();
        }
      } else {
        setSyncStatus('error');
      }
    } catch (error) {
      console.log(error);
      setSyncStatus('error');
    } finally {
      setSyncStatus('idle');
    }
  };

  const fetchAuthUrl = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.SYNC_USERS_AUTH_URL);
      if (isSuccessResponse(response.statusCode)) {
        safeRedirect(response.data);
      }
    } catch (error) {
      console.log(error);
    }
  };

  const fetchGroups = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.SYNC_USERS_GROUPS);
      if (isSuccessResponse(response.statusCode)) {
        setGroups(response.data);
      } else {
        setSyncStatus('error');
      }
    } catch (error) {
      console.log(error);
      toast.error('Failed to fetch groups');
    }
  };

  // Filter groups based on search query
  const filteredGroups = useMemo(() => {
    if (!searchQuery.trim()) {
      return groups;
    }
    const query = searchQuery.toLowerCase();
    return groups.filter(
      group =>
        group.displayName.toLowerCase().includes(query) ||
        group.description?.toLowerCase().includes(query) ||
        group.mailNickname?.toLowerCase().includes(query),
    );
  }, [groups, searchQuery]);

  // Handle individual group selection
  const handleGroupToggle = (groupId: string) => {
    setSelectedGroupIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(groupId)) {
        newSet.delete(groupId);
      } else {
        newSet.add(groupId);
      }
      return newSet;
    });
  };

  // Handle select all/none
  const handleSelectAll = () => {
    if (selectedGroupIds.size === filteredGroups.length) {
      // Deselect all filtered groups
      setSelectedGroupIds(prev => {
        const newSet = new Set(prev);
        filteredGroups.forEach(group => newSet.delete(group.id));
        return newSet;
      });
    } else {
      // Select all filtered groups
      setSelectedGroupIds(prev => {
        const newSet = new Set(prev);
        filteredGroups.forEach(group => newSet.add(group.id));
        return newSet;
      });
    }
  };

  // Clear all selections
  const handleClearAll = () => {
    setSelectedGroupIds(new Set());
  };

  // Check if all filtered groups are selected
  const allFilteredSelected =
    filteredGroups.length > 0 &&
    filteredGroups.every(group => selectedGroupIds.has(group.id));

  // Import users API call
  const handleImportUsers = async () => {
    if (selectedGroupIds.size === 0) {
      toast.warning('Please select at least one group to import');
      return;
    }

    try {
      setImportStatus('importing');
      const response = await apiClient.post(API_END_POINTS.IMPORT_USERS, {
        data: {
          groupIds: Array.from(selectedGroupIds),
        },
      });

      if (isSuccessResponse(response.statusCode)) {
        setImportStatus('success');
        setImportResults(response.data);
        toast.success(response.message || 'Users imported successfully');
        // Clear selections after successful import
        setSelectedGroupIds(new Set());
      } else {
        setImportStatus('error');
        toast.error(response.message || 'Failed to import users');
      }
    } catch (error: any) {
      console.error('Import error:', error);
      setImportStatus('error');
      toast.error(
        error?.response?.data?.message ||
          'An error occurred while importing users',
      );
    }
  };

  const getSyncMethodIcon = () => {
    switch (syncMethod) {
      case 'ad':
        return <Database className="size-6 text-blue-600" />;
      case 'okta':
        return <Database className="size-6 text-orange-600" />;
      case 'saml':
        return <Database className="size-6 text-green-600" />;
      default:
        return <RefreshCw className="size-6" />;
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Sync Users</h1>
        <p className="text-muted-foreground">
          Synchronize Client Users from external identity providers
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <RefreshCw className="size-5" />
            User Synchronization
          </CardTitle>
          <CardDescription>Sync users from Active Directory</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Sync Method Selection */}
          {/* <div className="space-y-2">
            <Label htmlFor="sync-method">Sync Method *</Label>
            <Select value={syncMethod} onValueChange={setSyncMethod}>
              <SelectTrigger>
                <SelectValue placeholder="Select synchronization method..." />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ad">Active Directory (AD)</SelectItem>
                <SelectItem value="okta">Okta</SelectItem>
                <SelectItem value="saml">SAML Provider</SelectItem>
              </SelectContent>
            </Select>
          </div> */}

          {/* Sync Configuration */}
          {syncMethod && (
            <div className="rounded-lg border border-card-border p-4">
              <div className="mb-4 flex items-center gap-3">
                {getSyncMethodIcon()}
                <div>
                  <h3 className="font-semibold text-foreground">
                    {syncMethod === 'ad' && 'Active Directory'}
                    {syncMethod === 'okta' && 'Okta'}
                    {syncMethod === 'saml' && 'SAML Provider'} Synchronization
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Ready to sync users from Active Directory
                  </p>
                </div>
              </div>

              <Button
                onClick={handleSync}
                disabled={syncStatus === 'syncing'}
                className="w-full md:w-auto"
              >
                {syncStatus === 'syncing' ? (
                  <>
                    <RefreshCw className="mr-2 size-4 animate-spin" />
                    Syncing Users...
                  </>
                ) : (
                  <>
                    <RefreshCw className="mr-2 size-4" />
                    Start Sync
                  </>
                )}
              </Button>
            </div>
          )}

          {/* Groups Selection Section */}
          {groups.length > 0 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-foreground">
                    Select Groups to Import
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Choose one or more groups to import users from Microsoft
                    Active Directory
                  </p>
                </div>
                {selectedGroupIds.size > 0 && (
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-muted-foreground">
                      {selectedGroupIds.size} group
                      {selectedGroupIds.size !== 1 ? 's' : ''} selected
                    </span>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleClearAll}
                      className="h-8"
                    >
                      <X className="mr-1 size-3" />
                      Clear
                    </Button>
                  </div>
                )}
              </div>

              {/* Search Bar */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  type="text"
                  placeholder="Search groups by name, description, or email..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>

              {/* Select All Toggle */}
              {filteredGroups.length > 0 && (
                <div className="flex items-center gap-3 rounded-lg border border-card-border bg-muted/30 p-3">
                  <Checkbox
                    checked={allFilteredSelected}
                    onCheckedChange={handleSelectAll}
                    id="select-all"
                  />
                  <Label
                    htmlFor="select-all"
                    className="flex-1 cursor-pointer font-medium"
                  >
                    Select All ({filteredGroups.length} group
                    {filteredGroups.length !== 1 ? 's' : ''})
                  </Label>
                </div>
              )}

              {/* Groups List */}
              <div className="max-h-[500px] space-y-2 overflow-y-auto rounded-lg border border-card-border p-4">
                {filteredGroups.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-center">
                    <Users className="mb-3 size-12 text-muted-foreground" />
                    <p className="text-sm font-medium text-foreground">
                      {searchQuery
                        ? 'No groups found matching your search'
                        : 'No groups available'}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {searchQuery
                        ? 'Try adjusting your search terms'
                        : 'Click "Start Sync" to fetch groups from Microsoft'}
                    </p>
                  </div>
                ) : (
                  filteredGroups.map(group => {
                    const isSelected = selectedGroupIds.has(group.id);
                    return (
                      <div
                        key={group.id}
                        className={`group flex items-start gap-3 rounded-lg border p-4 transition-all hover:bg-muted/50 ${
                          isSelected
                            ? 'border-primary bg-primary/5'
                            : 'border-card-border'
                        }`}
                      >
                        <div className="mt-1">
                          <Checkbox
                            checked={isSelected}
                            onCheckedChange={() => handleGroupToggle(group.id)}
                            id={`group-${group.id}`}
                          />
                        </div>
                        <label
                          htmlFor={`group-${group.id}`}
                          className="flex-1 cursor-pointer"
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2">
                                <h4 className="font-semibold text-foreground">
                                  {group.displayName}
                                </h4>
                                {isSelected && (
                                  <CheckCircle className="size-4 text-primary" />
                                )}
                              </div>
                              {group.description && (
                                <p className="mt-1 text-sm text-muted-foreground">
                                  {group.description}
                                </p>
                              )}
                              <div className="mt-2 flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
                                {group.mailNickname && (
                                  <span className="flex items-center gap-1">
                                    <span className="font-medium">Email:</span>
                                    {group.mailNickname}
                                  </span>
                                )}
                                <span className="flex items-center gap-1">
                                  <Users className="size-3" />
                                  <span className="font-medium">
                                    {group.memberCount || 0}
                                  </span>
                                  <span>
                                    member{group.memberCount !== 1 ? 's' : ''}
                                  </span>
                                </span>
                              </div>
                            </div>
                          </div>
                        </label>
                      </div>
                    );
                  })
                )}
              </div>

              {/* Import Button */}
              {selectedGroupIds.size > 0 && (
                <div className="flex justify-end">
                  <Button
                    onClick={handleImportUsers}
                    disabled={importStatus === 'importing'}
                    className="min-w-[150px]"
                  >
                    {importStatus === 'importing' ? (
                      <>
                        <RefreshCw className="mr-2 size-4 animate-spin" />
                        Importing...
                      </>
                    ) : (
                      <>
                        <CheckCheck className="mr-2 size-4" />
                        Import Selected Groups
                      </>
                    )}
                  </Button>
                </div>
              )}
            </div>
          )}

          {/* Import Results */}
          {importResults && importStatus === 'success' && (
            <div className="space-y-4">
              <div className="rounded-lg border border-green-200 bg-green-50 p-4 dark:border-primary/10 dark:bg-primary/10">
                <div className="mb-4 flex items-center gap-3">
                  <CheckCircle className="size-6 text-primary" />
                  <div>
                    <h3 className="font-semibold text-primary">
                      Import Completed Successfully
                    </h3>
                    <p className="text-sm text-primary">
                      User import has been completed at{' '}
                      {new Date(importResults.completedAt).toLocaleString()}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                  <div className="rounded-lg bg-white p-3 text-center dark:bg-gray-900">
                    <div className="text-2xl font-bold text-green-600">
                      {importResults.totalUsersProcessed}
                    </div>
                    <div className="text-xs text-green-700">
                      Total Processed
                    </div>
                  </div>
                  <div className="rounded-lg bg-white p-3 text-center dark:bg-gray-900">
                    <div className="text-2xl font-bold text-green-600">
                      {importResults.usersCreated}
                    </div>
                    <div className="text-xs text-green-700">Users Created</div>
                  </div>
                  <div className="rounded-lg bg-white p-3 text-center dark:bg-gray-900">
                    <div className="text-2xl font-bold text-yellow-600">
                      {importResults.usersSkipped}
                    </div>
                    <div className="text-xs text-yellow-700">Users Skipped</div>
                  </div>
                  <div className="rounded-lg bg-white p-3 text-center dark:bg-gray-900">
                    <div className="text-2xl font-bold text-red-600">
                      {importResults.usersFailed}
                    </div>
                    <div className="text-xs text-red-700">Users Failed</div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Sync Results */}
          {syncResults && syncStatus === 'success' && (
            <div className="space-y-4">
              <div className="rounded-lg border border-card-border p-4">
                <div className="mb-4 flex items-center gap-3">
                  <CheckCircle className="size-6 text-green-600" />
                  <div>
                    <h3 className="font-semibold text-green-600">
                      Sync Completed
                    </h3>
                    <p className="text-sm text-green-700">
                      User synchronization has been completed
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-4 text-center">
                  <div>
                    <div className="text-2xl font-bold text-green-600">
                      {syncResults.total}
                    </div>
                    <div className="text-sm text-green-700">Total Users</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold text-green-600">
                      {syncResults.synced}
                    </div>
                    <div className="text-sm text-green-700">Synced</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold text-red-600">
                      {syncResults.failed}
                    </div>
                    <div className="text-sm text-red-700">Failed</div>
                  </div>
                </div>
              </div>

              {syncResults.failed > 0 && (
                <div className="rounded-lg border border-card-border p-4">
                  <div className="mb-3 flex items-center gap-3">
                    <AlertCircle className="size-5 text-yellow-600" />
                    <h4 className="font-semibold text-yellow-800">
                      Sync Errors
                    </h4>
                  </div>
                  <ul className="space-y-2 text-sm text-yellow-700">
                    {syncResults.errors.map((error, index) => (
                      <li key={index} className="flex items-start gap-2">
                        <span>•</span>
                        <span>{error}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              <div className="flex justify-end">
                <Button variant="outline">Download Sync Report</Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default SyncUsers;

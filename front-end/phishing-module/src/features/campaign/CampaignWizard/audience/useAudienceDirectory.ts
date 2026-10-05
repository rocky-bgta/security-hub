import { useCampaigns } from 'hooks/UseCampaigns';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IList, IResponse } from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { useCallback, useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import {
  buildQueryString,
  normalizeLicensedDepartmentCounts,
  normalizeLicensedGroupCounts,
  normalizeLicensedUserList,
} from './helpers';
import {
  EndUserStatus,
  IAudienceDirectory,
  IDepartmentOption,
  IDepartmentUserCount,
  IEndUser,
  IEndUserQueryParams,
  IRiskGroupUserCount,
  RiskGroup,
} from './types';

interface UseAudienceDirectoryOptions {
  clientAdminId: string;
  enabled?: boolean;
  fetchUsersEnabled?: boolean;
  source?: 'registration' | 'licensed';
  licensedCampaignId?: string;
  productPackageId?: string;
}

export const useAudienceDirectory = ({
  clientAdminId,
  enabled = true,
  fetchUsersEnabled = true,
  source = 'registration',
  licensedCampaignId,
  productPackageId = '',
}: UseAudienceDirectoryOptions): IAudienceDirectory => {
  const apiClient = useAPI();
  const {
    getLicensedUsers,
    getLicensedUserDepartmentCounts,
    getLicensedUserGroupCounts,
  } = useCampaigns();

  const licensedCampaignIdRef = useRef<string | null>(null);
  const licensedRefreshGeneration = useRef(0);
  const skipNextLicensedSync = useRef(false);
  const isLicensedSource = source === 'licensed';

  if (licensedCampaignId) {
    licensedCampaignIdRef.current = licensedCampaignId;
  }

  const [usersLoading, setUsersLoading] = useState(false);
  const [users, setUsers] = useState<IList<IEndUser>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });

  const [departmentList, setDepartmentList] = useState<IDepartmentOption[]>([]);
  const [departmentUserCounts, setDepartmentUserCounts] = useState<
    IDepartmentUserCount[]
  >([]);
  const [riskGroupUserCounts, setRiskGroupUserCounts] = useState<
    IRiskGroupUserCount[]
  >([]);
  const [departmentFilter, setDepartmentFilter] = useState<ISelectOption[]>([]);
  const [riskGroupFilter, setRiskGroupFilter] = useState('ALL');
  const [searchInput, setSearchInput] = useState('');

  const [queryParams, setQueryParams] = useState<IEndUserQueryParams>({
    clientAdminId,
    search: '',
    status: EndUserStatus.ACTIVE,
    departments: [],
    riskGroup: RiskGroup.ALL,
    offset: 0,
    pageSize: 10,
    productPackageId,
  });
  const queryParamsRef = useRef(queryParams);
  queryParamsRef.current = queryParams;

  const debouncedSearch = useDebounce(searchInput, 500);

  useEffect(() => {
    setQueryParams(prev =>
      prev.clientAdminId === clientAdminId ? prev : { ...prev, clientAdminId },
    );
  }, [clientAdminId]);

  useEffect(() => {
    setQueryParams(prev =>
      prev.productPackageId === productPackageId
        ? prev
        : { ...prev, productPackageId, offset: 0 },
    );
  }, [productPackageId]);

  useEffect(() => {
    setQueryParams(prev => {
      if (prev.search === debouncedSearch) return prev;
      return { ...prev, search: debouncedSearch, offset: 0 };
    });
  }, [debouncedSearch]);

  const fetchDepartmentList = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.DEPARTMENT_LIST +
          'active=true&isSystemDefined=true&pageSize=1000&clientAdminId=' +
          encodeURIComponent(queryParams.clientAdminId),
      );
      if (isSuccessResponse(response.statusCode)) {
        setDepartmentList(
          response.data.items.map((dept: IDepartmentOption) => ({
            id: dept.name,
            name: dept.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching department list:', error);
    }
  }, [apiClient, queryParams.clientAdminId]);

  const fetchUsers = useCallback(async () => {
    const params = queryParamsRef.current;

    if (isLicensedSource) {
      const licensedId = licensedCampaignIdRef.current;
      if (!licensedId) return;
      setUsersLoading(true);
      try {
        const data = await getLicensedUsers(licensedId, params);
        if (data == null) {
          toast.error('Failed to load licensed users');
          return;
        }
        setUsers(normalizeLicensedUserList(data, params));
      } catch (error) {
        console.error('Error fetching licensed users:', error);
        toast.error('Failed to load licensed users');
      } finally {
        setUsersLoading(false);
      }
      return;
    }

    if (!clientAdminId) return;
    setUsersLoading(true);
    try {
      const response: IResponse<IList<IEndUser>> = await apiClient.get(
        API_END_POINTS.END_USER_LIST + buildQueryString(params),
      );
      if (isSuccessResponse(response.statusCode)) {
        setUsers({
          offset: params.offset,
          pageSize: params.pageSize,
          total: response.data.total,
          items: response.data.items,
        });
      }
    } catch (error) {
      console.error('Error fetching users:', error);
    } finally {
      setUsersLoading(false);
    }
  }, [
    isLicensedSource,
    getLicensedUsers,
    clientAdminId,
    queryParams,
    apiClient,
  ]);

  const fetchDepartmentUserCounts = useCallback(async () => {
    if (isLicensedSource) {
      const licensedId = licensedCampaignIdRef.current;
      if (!licensedId) return;
      try {
        const data = await getLicensedUserDepartmentCounts(licensedId);
        if (data == null) {
          toast.error('Failed to load licensed department counts');
          return;
        }
        setDepartmentUserCounts(normalizeLicensedDepartmentCounts(data));
      } catch (error) {
        console.error('Error fetching licensed department counts:', error);
        toast.error('Failed to load licensed department counts');
      }
      return;
    }

    if (!queryParams.clientAdminId) return;
    try {
      const response: IResponse<IDepartmentUserCount[]> = await apiClient.get(
        API_END_POINTS.DEPARTMENT_USER_COUNTS +
          `clientAdminId=${encodeURIComponent(queryParams.clientAdminId)}`,
      );
      if (isSuccessResponse(response.statusCode))
        setDepartmentUserCounts(response.data || []);
    } catch (error) {
      console.error('Error fetching department user counts:', error);
      setDepartmentUserCounts([]);
    }
  }, [
    isLicensedSource,
    getLicensedUserDepartmentCounts,
    apiClient,
    queryParams.clientAdminId,
  ]);

  const fetchRiskGroupUserCounts = useCallback(async () => {
    if (isLicensedSource) {
      const licensedId = licensedCampaignIdRef.current;
      if (!licensedId) return;
      try {
        const data = await getLicensedUserGroupCounts(licensedId);
        if (data == null) {
          toast.error('Failed to load licensed group counts');
          return;
        }
        setRiskGroupUserCounts(normalizeLicensedGroupCounts(data));
      } catch (error) {
        console.error('Error fetching licensed group counts:', error);
        toast.error('Failed to load licensed group counts');
      }
      return;
    }

    if (!queryParams.clientAdminId) return;
    try {
      const response: IResponse<IRiskGroupUserCount[]> = await apiClient.get(
        API_END_POINTS.RISK_GROUP_USER_COUNTS +
          `clientAdminId=${encodeURIComponent(queryParams.clientAdminId)}`,
      );
      if (isSuccessResponse(response.statusCode))
        setRiskGroupUserCounts(response.data || []);
    } catch (error) {
      console.error('Error fetching risk group user counts:', error);
      setRiskGroupUserCounts([]);
    }
  }, [
    isLicensedSource,
    getLicensedUserGroupCounts,
    apiClient,
    queryParams.clientAdminId,
  ]);

  const refreshDirectory = useCallback(async () => {
    await Promise.all([
      fetchUsers(),
      fetchDepartmentList(),
      fetchDepartmentUserCounts(),
      fetchRiskGroupUserCounts(),
    ]);
  }, [
    fetchUsers,
    fetchDepartmentList,
    fetchDepartmentUserCounts,
    fetchRiskGroupUserCounts,
  ]);

  const loadLicensedAudience = useCallback(
    async (campaignId: string) => {
      if (!campaignId) return;

      const generation = licensedRefreshGeneration.current + 1;
      licensedRefreshGeneration.current = generation;
      skipNextLicensedSync.current = true;
      licensedCampaignIdRef.current = campaignId;
      setUsersLoading(true);

      const params = queryParamsRef.current;

      try {
        const [usersResult, departmentResult, groupResult] =
          await Promise.allSettled([
            getLicensedUsers(campaignId, params),
            getLicensedUserDepartmentCounts(campaignId),
            getLicensedUserGroupCounts(campaignId),
          ]);

        if (generation !== licensedRefreshGeneration.current) return;

        if (usersResult.status === 'fulfilled' && usersResult.value != null) {
          setUsers(normalizeLicensedUserList(usersResult.value, params));
        } else {
          toast.error('Failed to load licensed users');
        }

        if (
          departmentResult.status === 'fulfilled' &&
          departmentResult.value != null
        ) {
          setDepartmentUserCounts(
            normalizeLicensedDepartmentCounts(departmentResult.value),
          );
        } else {
          toast.error('Failed to load licensed department counts');
        }

        if (groupResult.status === 'fulfilled' && groupResult.value != null) {
          setRiskGroupUserCounts(
            normalizeLicensedGroupCounts(groupResult.value),
          );
        } else {
          toast.error('Failed to load licensed group counts');
        }
      } finally {
        if (generation === licensedRefreshGeneration.current) {
          skipNextLicensedSync.current = false;
          setUsersLoading(false);
        }
      }
    },
    [
      getLicensedUsers,
      getLicensedUserDepartmentCounts,
      getLicensedUserGroupCounts,
    ],
  );

  useEffect(() => {
    if (!enabled || !fetchUsersEnabled) return;
    if (isLicensedSource && !licensedCampaignId) return;
    if (skipNextLicensedSync.current) return;
    void fetchUsers();
  }, [
    enabled,
    fetchUsersEnabled,
    queryParams,
    fetchUsers,
    isLicensedSource,
    licensedCampaignId,
  ]);

  useEffect(() => {
    if (!enabled) return;
    void fetchDepartmentList();
  }, [enabled, fetchDepartmentList]);

  useEffect(() => {
    if (!enabled) return;
    if (isLicensedSource && !licensedCampaignId) return;
    if (skipNextLicensedSync.current) return;
    void fetchDepartmentUserCounts();
    void fetchRiskGroupUserCounts();
  }, [
    enabled,
    fetchDepartmentUserCounts,
    fetchRiskGroupUserCounts,
    isLicensedSource,
    licensedCampaignId,
  ]);

  const handlePageChange = (page: number) =>
    setQueryParams(prev => ({ ...prev, offset: page - 1 }));

  const handlePageSizeChange = (pageSize: number) =>
    setQueryParams(prev => ({ ...prev, pageSize, offset: 0 }));

  const handleRiskGroupChange = (value: string) => {
    setRiskGroupFilter(value);
    setQueryParams(prev => ({
      ...prev,
      riskGroup: value === 'ALL' ? RiskGroup.ALL : (value as RiskGroup),
      offset: 0,
    }));
  };

  const handleDepartmentFilterChange = (
    newValue: TMultiValue<ISelectOption> | TSingleValue<ISelectOption>,
  ) => {
    const selected = newValue as TMultiValue<ISelectOption>;
    const hasAll = selected?.some((i: ISelectOption) => i.value === 'ALL');
    if (hasAll) {
      setDepartmentFilter([]);
      setQueryParams(prev => ({ ...prev, departments: [], offset: 0 }));
    } else {
      setDepartmentFilter(Array.from(selected || []));
      setQueryParams(prev => ({
        ...prev,
        departments: selected?.map((i: ISelectOption) => i.value) || [],
        offset: 0,
      }));
    }
  };

  const handleResetFilters = () => {
    setSearchInput('');
    setRiskGroupFilter('ALL');
    setDepartmentFilter([]);
    setQueryParams(prev => ({
      ...prev,
      search: '',
      status: EndUserStatus.ALL,
      departments: [],
      riskGroup: RiskGroup.ALL,
      offset: 0,
    }));
  };

  return {
    users,
    usersLoading,
    departmentList,
    departmentUserCounts,
    riskGroupUserCounts,
    departmentFilter,
    riskGroupFilter,
    searchInput,
    setSearchInput,
    queryParams,
    clientAdminId,
    fetchUsers,
    refreshDirectory,
    loadLicensedAudience,
    handlePageChange,
    handlePageSizeChange,
    handleRiskGroupChange,
    handleDepartmentFilterChange,
    handleResetFilters,
  };
};

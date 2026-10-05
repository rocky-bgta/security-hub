import { AudienceType, IAllocateLicenceRequest } from 'models/Campaign';
import { IList } from 'models/Global';
import {
  EndUserStatus,
  IDepartmentUserCount,
  IEndUser,
  IEndUserQueryParams,
  IRiskGroupUserCount,
  RiskGroup,
} from './types';

export const buildQueryString = (params: IEndUserQueryParams): string => {
  const parts: string[] = [];

  if (params.clientAdminId)
    parts.push(`clientAdminId=${encodeURIComponent(params.clientAdminId)}`);
  if (params.search) parts.push(`search=${encodeURIComponent(params.search)}`);
  if (params.status && params.status !== EndUserStatus.ALL)
    parts.push(`status=${encodeURIComponent(params.status)}`);
  params.departments.forEach(d =>
    parts.push(`departments=${encodeURIComponent(d)}`),
  );
  if (params.riskGroup && params.riskGroup !== RiskGroup.ALL)
    parts.push(`riskGroup=${encodeURIComponent(params.riskGroup)}`);
  if (params.productPackageId)
    parts.push(
      `productPackageId=${encodeURIComponent(params.productPackageId)}`,
    );
  parts.push(`offset=${params.offset}`);
  parts.push(`pageSize=${params.pageSize}`);

  return parts.join('&');
};

export const formatEnum = (value: string) =>
  value
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, c => c.toUpperCase());

export const formatDate = (iso: string) => {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat(undefined, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
};

export const getRiskGroupBadgeVariant = (riskGroup: RiskGroup) => {
  switch (riskGroup) {
    case RiskGroup.HIGH_RISK:
      return 'destructive' as const;
    case RiskGroup.MEDIUM_RISK:
      return 'warning' as const;
    case RiskGroup.LOW_RISK:
      return 'default' as const;
    default:
      return 'secondary' as const;
  }
};

export const getStatusBadgeVariant = (status: EndUserStatus) => {
  switch (status) {
    case EndUserStatus.ACTIVE:
      return 'default' as const;
    case EndUserStatus.INACTIVE:
      return 'secondary' as const;
    case EndUserStatus.SUSPEND:
      return 'destructive' as const;
    default:
      return 'secondary' as const;
  }
};

export const calculateAudienceRecipientCount = ({
  audienceType,
  usersTotal,
  departmentUserCounts,
  departmentIds,
  riskGroupUserCounts,
  groupIds,
  userIds,
}: {
  audienceType: AudienceType;
  usersTotal: number;
  departmentUserCounts: IDepartmentUserCount[];
  departmentIds: string[];
  riskGroupUserCounts: IRiskGroupUserCount[];
  groupIds: string[];
  userIds: string[];
}): number => {
  switch (audienceType) {
    case AudienceType.ALL_USERS:
      return usersTotal;
    case AudienceType.DEPARTMENTS:
      return departmentUserCounts
        .filter(i => departmentIds.includes(i.departmentName))
        .reduce((sum, i) => sum + i.userCount, 0);
    case AudienceType.GROUPS:
      return riskGroupUserCounts
        .filter(i => groupIds.includes(i.riskGroup))
        .reduce((sum, i) => sum + i.userCount, 0);
    case AudienceType.INDIVIDUAL:
      return userIds.length;
    default:
      return 0;
  }
};

export const buildAllocateLicencePayload = (
  audienceType: AudienceType,
  selection: {
    departmentIds: string[];
    groupIds: string[];
    userIds: string[];
  },
  confirm: boolean,
): IAllocateLicenceRequest => ({
  audienceType,
  departmentIds:
    audienceType === AudienceType.DEPARTMENTS ? selection.departmentIds : [],
  groupIds: audienceType === AudienceType.GROUPS ? selection.groupIds : [],
  userIds: audienceType === AudienceType.INDIVIDUAL ? selection.userIds : [],
  confirm,
});

const asRecordArray = (data: unknown): Record<string, unknown>[] => {
  if (Array.isArray(data)) return data as Record<string, unknown>[];
  if (
    data &&
    typeof data === 'object' &&
    Array.isArray((data as IList<unknown>).items)
  ) {
    return (data as IList<unknown>).items as Record<string, unknown>[];
  }
  return [];
};

export const normalizeLicensedUserList = (
  data: unknown,
  queryParams: Pick<IEndUserQueryParams, 'offset' | 'pageSize'>,
): IList<IEndUser> => {
  if (
    data &&
    typeof data === 'object' &&
    Array.isArray((data as IList<IEndUser>).items)
  ) {
    const list = data as IList<IEndUser>;
    return {
      offset: queryParams.offset,
      pageSize: queryParams.pageSize,
      total: list.total ?? list.items.length,
      items: list.items ?? [],
    };
  }
  const items = Array.isArray(data) ? (data as IEndUser[]) : [];
  return {
    offset: queryParams.offset,
    pageSize: queryParams.pageSize,
    total: items.length,
    items,
  };
};

export const normalizeLicensedDepartmentCounts = (
  data: unknown,
): IDepartmentUserCount[] =>
  asRecordArray(data).map(item => ({
    departmentName: String(
      item.departmentName ?? item.department ?? item.name ?? '',
    ),
    userCount: Number(item.userCount ?? item.count ?? 0),
  }));

export const normalizeLicensedGroupCounts = (
  data: unknown,
): IRiskGroupUserCount[] =>
  asRecordArray(data).map(item => ({
    riskGroup: String(
      item.riskGroup ?? item.group ?? item.groupName ?? '',
    ) as RiskGroup,
    userCount: Number(item.userCount ?? item.count ?? 0),
  }));

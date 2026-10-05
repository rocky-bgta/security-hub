import { Pencil, PlusIcon, Search, Trash2Icon } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Switch } from 'common/Switch';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import ConfirmDialog from 'components/ConfirmDialog';
import { TableSkeleton } from 'components/LoadingSkeleton';
import ProviderCredentialModal from 'features/provider-credential/ProviderCredentialModal';
import useDebounce from 'hooks/UseDebounce';
import useProviderCredentials from 'hooks/UseProviderCredentials';
import {
  PROVIDER_CREDENTIAL_CATEGORIES,
  type IProviderCredential,
  type IProviderCredentialListParams,
} from 'models/ProviderCredential';

const DEFAULT_PAGE_SIZE = 10;

export interface ProviderConfigurationCopy {
  pageTitle: string;
  pageDescription: string;
  emptyStateHint: string;
}

const resolveCategoryLabel = (category: string): string =>
  PROVIDER_CREDENTIAL_CATEGORIES.find(option => option.value === category)
    ?.label || category;

const formatDate = (value?: string): string => {
  if (!value) return '—';
  const parsed = Date.parse(value);
  return Number.isNaN(parsed) ? value : new Date(parsed).toLocaleString();
};

interface ProviderConfigurationViewProps {
  copy: ProviderConfigurationCopy;
}

const ProviderConfigurationView = ({ copy }: ProviderConfigurationViewProps) => {
  const {
    isLoadingList,
    isSubmitting,
    getCredentialList,
    createCredential,
    updateCredential,
    deleteCredential,
    updateCredentialStatus,
  } = useProviderCredentials();

  const [items, setItems] = useState<IProviderCredential[]>([]);
  const [total, setTotal] = useState(0);
  const [queryParams, setQueryParams] = useState<IProviderCredentialListParams>({
    offset: 0,
    pageSize: DEFAULT_PAGE_SIZE,
    sortBy: 'createdAt',
    sortOrder: 'desc',
  });
  const [providerSearch, setProviderSearch] = useState('');
  const debouncedProviderSearch = useDebounce(providerSearch.trim(), 400);
  const [activeFilter, setActiveFilter] = useState<string>('ALL');
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<IProviderCredential | null>(null);
  const [deleteCandidate, setDeleteCandidate] =
    useState<IProviderCredential | null>(null);
  const [togglingId, setTogglingId] = useState<string | null>(null);

  const loadList = useCallback(
    async (options?: { silent?: boolean }) => {
      const result = await getCredentialList(
        {
          ...queryParams,
          providerName: debouncedProviderSearch || undefined,
          isActive:
            activeFilter === 'ALL' ? undefined : activeFilter === 'ACTIVE',
        },
        options,
      );

      if (!result.ok) {
        toast.error(result.message);
        setItems([]);
        setTotal(0);
        return;
      }

      setItems(result.data.items || []);
      setTotal(result.data.total || 0);
    },
    [activeFilter, debouncedProviderSearch, getCredentialList, queryParams],
  );

  useEffect(() => {
    void loadList();
  }, [loadList]);

  useEffect(() => {
    setQueryParams(prev =>
      prev.offset === 0 ? prev : { ...prev, offset: 0 },
    );
  }, [debouncedProviderSearch]);

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const openCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const openEdit = (item: IProviderCredential) => {
    setEditing(item);
    setModalOpen(true);
  };

  const handleDelete = async () => {
    if (!deleteCandidate) return;

    const result = await deleteCredential(deleteCandidate.id);
    if (!result.ok) {
      toast.error(result.message);
      return;
    }

    toast.success(result.message);
    setDeleteCandidate(null);
    void loadList({ silent: true });
  };

  const handleToggleStatus = async (
    item: IProviderCredential,
    active: boolean,
  ) => {
    if (item.isActive === active) return;

    setTogglingId(item.id);
    const result = await updateCredentialStatus(item.id, active);
    setTogglingId(null);

    if (!result.ok) {
      toast.error(result.message);
      return;
    }

    toast.success(result.message);
    void loadList({ silent: true });
  };

  const currentPage = (queryParams.offset || 0) + 1;

  return (
    <div className="mx-auto flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {copy.pageTitle}
          </h1>
          <p className="mt-1 text-muted-foreground">{copy.pageDescription}</p>
        </div>
        <Button onClick={openCreate}>
          <PlusIcon className="size-4" /> Add credential
        </Button>
      </div>

      <Card>
        <CardHeader className="gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <CardTitle>Credentials</CardTitle>
            <CardDescription>
              Secrets are masked after save. Only the last 4 characters of the
              API key are shown.
            </CardDescription>
          </div>
          <div className="flex flex-wrap gap-2">
            <div className="relative">
              <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
              <Input
                type="search"
                value={providerSearch}
                onChange={event => setProviderSearch(event.target.value)}
                placeholder="Search provider name..."
                className="w-[220px] pl-8"
              />
            </div>
            <Select
              value={activeFilter}
              onValueChange={value => {
                setActiveFilter(value);
                setQueryParams(prev => ({ ...prev, offset: 0 }));
              }}
            >
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All statuses</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="INACTIVE">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {isLoadingList ? (
            <TableSkeleton />
          ) : items.length === 0 ? (
            <div className="rounded-lg border border-dashed border-card-border px-6 py-12 text-center">
              <p className="text-sm font-medium">No provider credentials yet</p>
              <p className="mt-1 text-xs text-muted-foreground">
                {copy.emptyStateHint}
              </p>
              <Button className="mt-4" onClick={openCreate}>
                <PlusIcon className="size-4" /> Add credential
              </Button>
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Provider</TableHead>
                    <TableHead>Category</TableHead>
                    <TableHead>Model</TableHead>
                    <TableHead>API key</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Updated</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map(item => (
                    <TableRow key={item.id}>
                      <TableCell>
                        <div className="font-medium">
                          {item.providerName}
                          {item.isDefault ? ' (Default)' : ''}
                        </div>
                      </TableCell>
                      <TableCell>
                        {resolveCategoryLabel(item.category)}
                      </TableCell>
                      <TableCell>{item.modelName || '—'}</TableCell>
                      <TableCell className="font-mono text-xs">
                        {item.apiKeyLast4 ? `••••${item.apiKeyLast4}` : '—'}
                        {item.hasApiSecret ? (
                          <span className="ml-2 text-muted-foreground">
                            + secret
                          </span>
                        ) : null}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={item.isActive ? 'default' : 'secondary'}
                        >
                          {item.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {formatDate(item.updatedAt || item.createdAt)}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-3">
                          <div className="flex items-center gap-1.5">
                            <Label
                              htmlFor={`status-${item.id}`}
                              className="text-xs text-muted-foreground"
                            >
                              Active
                            </Label>
                            <Switch
                              id={`status-${item.id}`}
                              checked={item.isActive}
                              disabled={togglingId === item.id || isSubmitting}
                              onCheckedChange={checked =>
                                void handleToggleStatus(item, checked)
                              }
                              aria-label={`Toggle active status for ${item.providerName}`}
                            />
                          </div>
                          <Button
                            size="icon"
                            variant="ghost"
                            className="size-8"
                            aria-label={`Edit ${item.providerName}`}
                            onClick={() => openEdit(item)}
                          >
                            <Pencil className="size-4" />
                          </Button>
                          <Button
                            size="icon"
                            variant="ghost"
                            className="size-8 text-destructive hover:text-destructive"
                            aria-label={`Delete ${item.providerName}`}
                            onClick={() => setDeleteCandidate(item)}
                          >
                            <Trash2Icon className="size-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              <div className="mt-4 flex justify-end">
                <Pagination
                  total={total}
                  perPage={queryParams.pageSize || DEFAULT_PAGE_SIZE}
                  currentPage={currentPage}
                  onPageChange={handlePageChange}
                />
              </div>
            </>
          )}
        </CardContent>
      </Card>

      <ProviderCredentialModal
        isOpen={modalOpen}
        isSubmitting={isSubmitting}
        credential={editing}
        onClose={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        onCreate={createCredential}
        onUpdate={updateCredential}
        onSaved={() => void loadList({ silent: true })}
      />

      <ConfirmDialog
        isOpen={!!deleteCandidate}
        onClose={() => !isSubmitting && setDeleteCandidate(null)}
        onConfirm={handleDelete}
        message={`Are you sure you want to delete "${deleteCandidate?.providerName || ''}" credentials? This action cannot be undone.`}
        loading={isSubmitting}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
};

export default ProviderConfigurationView;

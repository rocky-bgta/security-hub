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
import Pagination from 'common/Pagination';
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
import AttackTemplateModal from 'features/vishing/AttackTemplateModal';
import useDebounce from 'hooks/UseDebounce';
import { useVishingAttackTemplates } from 'hooks/UseVishingAttackTemplates';
import type {
  IVishingAttackTemplate,
  IVishingAttackTemplateRequest,
} from 'models/Vishing';
import type { IGetListParams } from 'models/Global';

const DEFAULT_PAGE_SIZE = 10;

const formatDate = (value?: string): string => {
  if (!value) return '—';
  const parsed = Date.parse(value);
  return Number.isNaN(parsed) ? value : new Date(parsed).toLocaleString();
};

const AttackTemplateLibrary = () => {
  const {
    loading,
    saving,
    fetchAttackTemplates,
    createAttackTemplate,
    updateAttackTemplate,
    deleteAttackTemplate,
  } = useVishingAttackTemplates();

  const [items, setItems] = useState<IVishingAttackTemplate[]>([]);
  const [total, setTotal] = useState(0);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: DEFAULT_PAGE_SIZE,
    sortBy: 'createdAt',
    sortOrder: 'desc',
  });
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search.trim(), 400);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<IVishingAttackTemplate | null>(null);
  const [deleteCandidate, setDeleteCandidate] =
    useState<IVishingAttackTemplate | null>(null);

  const loadList = useCallback(async () => {
    const result = await fetchAttackTemplates({
      ...queryParams,
      searchParam: debouncedSearch || undefined,
    });
    setItems(Array.isArray(result.items) ? result.items : []);
    setTotal(result.total || 0);
  }, [debouncedSearch, fetchAttackTemplates, queryParams]);

  useEffect(() => {
    void loadList();
  }, [loadList]);

  useEffect(() => {
    setQueryParams(prev =>
      prev.offset === 0 ? prev : { ...prev, offset: 0 },
    );
  }, [debouncedSearch]);

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

  const openEdit = (item: IVishingAttackTemplate) => {
    setEditing(item);
    setModalOpen(true);
  };

  const handleSubmit = async (payload: IVishingAttackTemplateRequest) => {
    const result = editing
      ? await updateAttackTemplate(editing.id, payload)
      : await createAttackTemplate(payload);

    if (!result.ok) {
      toast.error(result.message);
      return null;
    }

    toast.success(result.message);
    void loadList();
    return result.data ?? null;
  };

  const handleDelete = async () => {
    if (!deleteCandidate) return;

    const result = await deleteAttackTemplate(deleteCandidate.id);
    if (!result.ok) {
      toast.error(result.message);
      return;
    }

    toast.success(result.message);
    setDeleteCandidate(null);

    const nextTotal = Math.max(0, total - 1);
    const pageSize = queryParams.pageSize || DEFAULT_PAGE_SIZE;
    const maxPageIndex = Math.max(0, Math.ceil(nextTotal / pageSize) - 1);
    const currentOffset = queryParams.offset || 0;
    if (currentOffset > maxPageIndex) {
      setQueryParams(prev => ({ ...prev, offset: maxPageIndex }));
      return;
    }

    void loadList();
  };

  const currentPage = (queryParams.offset || 0) + 1;

  return (
    <div className="mx-auto flex w-full flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Vishing Attack Templates
          </h1>
          <p className="mt-1 text-muted-foreground">
            Create and manage reusable call scripts for vishing simulations.
          </p>
        </div>
        <Button onClick={openCreate}>
          <PlusIcon className="size-4" /> Add template
        </Button>
      </div>

      <Card>
        <CardHeader className="gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <CardTitle>Templates</CardTitle>
            <CardDescription>
              These templates appear when creating a vishing scenario.
            </CardDescription>
          </div>
          <div className="relative">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              value={search}
              onChange={event => setSearch(event.target.value)}
              placeholder="Search templates..."
              className="w-[240px] pl-8"
            />
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableSkeleton />
          ) : items.length === 0 ? (
            <div className="rounded-lg border border-dashed border-card-border px-6 py-12 text-center">
              <p className="text-sm font-medium">No attack templates yet</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Add your first vishing attack template to get started.
              </p>
              <Button className="mt-4" onClick={openCreate}>
                <PlusIcon className="size-4" /> Add template
              </Button>
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Script preview</TableHead>
                    <TableHead>Variables</TableHead>
                    <TableHead>Updated</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map(item => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">{item.name}</TableCell>
                      <TableCell className="max-w-md">
                        <p className="line-clamp-2 text-sm text-muted-foreground">
                          {item.script || '—'}
                        </p>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1">
                          {(item.variables ?? []).length > 0 ? (
                            item.variables?.slice(0, 3).map(variable => (
                              <Badge
                                key={variable}
                                variant="outline"
                                className="text-[10px]"
                              >
                                {variable}
                              </Badge>
                            ))
                          ) : (
                            <span className="text-xs text-muted-foreground">
                              —
                            </span>
                          )}
                          {(item.variables?.length ?? 0) > 3 && (
                            <Badge variant="secondary" className="text-[10px]">
                              +{(item.variables?.length ?? 0) - 3}
                            </Badge>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {formatDate(item.updatedAt || item.createdAt)}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            size="icon"
                            variant="ghost"
                            className="size-8"
                            aria-label={`Edit ${item.name}`}
                            onClick={() => openEdit(item)}
                          >
                            <Pencil className="size-4" />
                          </Button>
                          <Button
                            size="icon"
                            variant="ghost"
                            className="size-8 text-destructive hover:text-destructive"
                            aria-label={`Delete ${item.name}`}
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

      <AttackTemplateModal
        isOpen={modalOpen}
        isSubmitting={saving}
        template={editing}
        onClose={() => {
          setModalOpen(false);
          setEditing(null);
        }}
        onSubmit={handleSubmit}
      />

      <ConfirmDialog
        isOpen={!!deleteCandidate}
        onClose={() => !saving && setDeleteCandidate(null)}
        onConfirm={() => void handleDelete()}
        message={`Are you sure you want to delete "${deleteCandidate?.name || ''}"? This action cannot be undone.`}
        loading={saving}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
};

export default AttackTemplateLibrary;

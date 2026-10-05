import { Fragment, useEffect, useState } from 'react';
import { IoAdd, IoSearchSharp } from 'react-icons/io5';
import { toast } from 'react-toastify';

import { DeleteIcon, EditIcon, ViewIcon } from 'assets/icons';
import clsx from 'clsx';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
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
import BulkAction from 'components/BulkAction';
import ConfirmDialog from 'components/ConfirmDialog';
import PackageModal from 'features/package/PackageModal';
import PackageViewModal from 'features/package/PackageViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IList, IResponse, Status } from 'models/Global';
import { IPackage } from 'models/Package';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { BASE_URL, InitGetListParams } from 'utils/Constants';
import {
  HumanizeDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

enum ModalType {
  General = 'general',
  View = 'view',
  None = 'none',
}

enum ConfirmDialogType {
  None = 'none',
  Delete = 'delete',
  BulkEnable = 'bulk_enable',
  BulkDisable = 'bulk_disable',
  BulkDelete = 'bulk_delete',
  BulkExport = 'bulk_export',
}

const SuperAdminPackageList = () => {
  const [data, setData] = useState<IList<IPackage>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [selectedPackages, setSelectedPackages] = useState<Array<string>>([]);
  const [showModal, setShowModal] = useState<ModalType>(ModalType.None);
  const [dialogType, setDialogType] = useState<ConfirmDialogType>(
    ConfirmDialogType.None,
  );
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [packageId, setPackageId] = useState<string>('');
  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchPackageData();
    }
  }, [searchDebounce]);

  const DialogMap = {
    [ConfirmDialogType.None]: {
      message: '',
      loadingText: '',
      onConfirm: () => {},
    },
    [ConfirmDialogType.Delete]: {
      message: 'Are you sure you want to delete this package?',
      loadingText: 'Deleting...',
      onConfirm: () => handleDeletePackage(),
    },
    [ConfirmDialogType.BulkEnable]: {
      message: 'Are you sure you want to enable the selected packages?',
      loadingText: 'Enabling...',
      onConfirm: () => handleBulkEnable(),
    },
    [ConfirmDialogType.BulkDisable]: {
      message: 'Are you sure you want to disable the selected packages?',
      loadingText: 'Disabling...',
      onConfirm: () => handleBulkDisable(),
    },
    [ConfirmDialogType.BulkDelete]: {
      message: 'Are you sure you want to delete the selected packages?',
      loadingText: 'Deleting...',
      onConfirm: () => handleBulkDelete(),
    },
    [ConfirmDialogType.BulkExport]: {
      message: 'Are you sure you want to export the selected packages?',
      loadingText: 'Exporting...',
      onConfirm: () => handleExportCSV(),
    },
  };

  const fetchPackageData = async () => {
    try {
      const response: IResponse<IList<IPackage>> = await apiClient.get(
        BASE_URL + API_END_POINTS.PACKAGE_LIST + queryString,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching package data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = () => {
    if (selectedPackages.length === data.items.length) {
      setSelectedPackages([]);
    } else {
      setSelectedPackages(data.items.map(packages => packages.id));
    }
  };

  const isAllSelected =
    selectedPackages.length === data.items.length && data.items.length > 0;

  const columns = [
    {
      key: 'select',
      header: (
        <Checkbox checked={isAllSelected} onCheckedChange={handleSelectAll} />
      ),
      render: (packages: any) => (
        <Checkbox
          checked={selectedPackages.includes(packages.id)}
          onCheckedChange={() => handleSelectPackage(packages.id)}
        />
      ),
    },
    {
      key: 'packageName',
      header: 'Name',
      tdClassName: 'content-w-[18%] content-text-left',
    },
    {
      key: 'packageDescription',
      header: 'Description',
      tdClassName: 'content-w-[24%] content-text-left',
      render: (packages: any) => {
        const description = packages.packageDescription || '';
        if (typeof description === 'object') {
          return '';
        }
        const words = description.split(' ');
        return words.length > 12
          ? words.slice(0, 12).join(' ') + '...'
          : description;
      },
    },
    {
      key: 'productStatus',
      header: 'Status',
      render: (packages: any) => (
        <span
          className={clsx(
            'content-rounded content-px-2 content-py-1 content-capitalize',
            packages?.packageStatus === Status.ENABLED
              ? 'content-bg-success content-bg-opacity-10 content-text-success'
              : 'content-bg-vibrant-red content-bg-opacity-10 content-text-vibrant-red',
          )}
        >
          {packages.packageStatus === Status.ENABLED ? 'Enabled' : 'Disabled'}
        </span>
      ),
    },
    {
      key: 'courseIds',
      header: 'Courses',
      render: (row: any) => row?.courseIds?.length,
    },
    { key: 'price', header: 'Price' },
    {
      key: 'createdAt',
      header: 'Created Date',
      render: (row: any) => HumanizeDate(row.createdAt),
    },
    {
      key: 'updatedAt',
      header: 'Updated Date',
      render: (row: any) => HumanizeDate(row.updatedAt),
    },
    {
      key: 'action',
      header: 'Action',
      render: (packages: any) => (
        <div className="content-flex content-justify-center content-gap-x-4">
          <Button
            onClick={() => handleOpenModal(ModalType.View, packages.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <ViewIcon width={24} height={24} />
          </Button>
          <Button
            onClick={() => handleOpenModal(ModalType.General, packages.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <EditIcon width={20} height={20} />
          </Button>
          <Button
            onClick={() => handleClickDelete(packages.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <DeleteIcon width={20} height={20} />
          </Button>
        </div>
      ),
    },
  ];

  const handleClickDelete = (id: string) => {
    setPackageId(id);
    setDialogType(ConfirmDialogType.Delete);
  };

  const handleDeletePackage = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.del(
        API_END_POINTS.PACKAGE_DELETE + packageId,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      fetchPackageData();
    } catch (error: any) {
      console.error('Error delete package:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
      setPackageId('');
    }
  };

  const handleSelectPackage = (id: string) => {
    setSelectedPackages(prevSelected =>
      prevSelected.includes(id)
        ? prevSelected.filter(userId => userId !== id)
        : [...prevSelected, id],
    );
  };

  const handleOpenModal = (content: ModalType, id?: string) => {
    setShowModal(content);
    if (id) setPackageId(id);
  };

  const handleHideModal = () => {
    setShowModal(ModalType.None);
    if (packageId) setPackageId('');
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleBulkEnable = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.put(API_END_POINTS.PACKAGE_BULK_UPDATE, {
        data: { ids: selectedPackages, status: Status.ENABLED },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedPackages([]);
      fetchPackageData();
    } catch (error: any) {
      console.error('Error enabling packages:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleBulkDisable = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.put(API_END_POINTS.PACKAGE_BULK_UPDATE, {
        data: { ids: selectedPackages, status: Status.DISABLED },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedPackages([]);
      fetchPackageData();
    } catch (error: any) {
      console.error('Error disabling packages:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleBulkDelete = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.del(API_END_POINTS.PACKAGE_BULK_DELETE, {
        data: {
          ids: selectedPackages,
        },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedPackages([]);
      fetchPackageData();
    } catch (error: any) {
      console.error('Error delete packages:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleExportCSV = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.post(
        API_END_POINTS.PACKAGE_BULK_EXPORT,
        {
          data: { ids: selectedPackages },
        },
      );
      const blob = new Blob([response], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'packages.csv';
      a.click();
    } catch (error) {
      console.error('Error exporting CSV:', error);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleSubmit = () => fetchPackageData();
  const handleSearch = () => fetchPackageData();

  return (
    <Fragment>
      <section>
        <div className="content-mb-6 content-flex content-justify-between content-text-secondary">
          <h4 className="content-font-medium">
            Package List {data.total > 0 && `(${data.total})`}
          </h4>
          <div className="content-flex content-gap-x-5">
            <Button
              onClick={() => handleOpenModal(ModalType.General)}
              className="content-flex content-items-center content-gap-x-2 content-rounded content-bg-primary hover:!content-bg-primary"
              size="sm"
            >
              <IoAdd className="content-text-2xl" /> Create Package
            </Button>
            <div className="content-flex content-text-secondary">
              <Input
                id="search"
                placeholder="Search"
                className="content-rounded-r-none content-border-soft-blue-gray content-px-2 content-py-1"
                value={queryParams.search}
                onChange={e =>
                  setQueryParams(prevState => ({
                    ...prevState,
                    search: e.target.value,
                  }))
                }
              />
              <Button className="content-p-0" onClick={handleSearch}>
                <IoSearchSharp className="content-h-auto content-w-10 content-rounded-r content-bg-primary content-p-2 content-text-3xl content-text-white" />
              </Button>
            </div>
          </div>
        </div>

        {selectedPackages.length > 0 && (
          <BulkAction
            selectedIds={selectedPackages}
            onEnable={() => setDialogType(ConfirmDialogType.BulkEnable)}
            onDisable={() => setDialogType(ConfirmDialogType.BulkDisable)}
            onDelete={() => setDialogType(ConfirmDialogType.BulkDelete)}
            onExportCSV={() => setDialogType(ConfirmDialogType.BulkExport)}
          />
        )}
        <div>
          {loading ? (
            <p>Loading</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  {columns.map(column => (
                    <TableHead key={column.key}>{column.header}</TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.items.map((row, index) => (
                  <TableRow key={index}>
                    {columns.map(column => (
                      <TableCell key={column.key}>
                        {column.render
                          ? column.render(row)
                          : (row as any)[column.key]}
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <div className="content-my-10 content-flex content-justify-end">
            <Pagination
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </div>
      </section>

      <PackageModal
        packageId={packageId}
        isOpen={showModal === ModalType.General}
        onClose={handleHideModal}
        onSubmit={handleSubmit}
      />

      <PackageViewModal
        isOpen={showModal === ModalType.View}
        onClose={handleHideModal}
        packageId={packageId}
      />

      <ConfirmDialog
        isOpen={dialogType !== ConfirmDialogType.None}
        message={DialogMap[dialogType]?.message}
        loading={submitting}
        loadingText={DialogMap[dialogType]?.loadingText}
        onClose={() => setDialogType(ConfirmDialogType.None)}
        onConfirm={DialogMap[dialogType]?.onConfirm}
      />
    </Fragment>
  );
};

export default SuperAdminPackageList;

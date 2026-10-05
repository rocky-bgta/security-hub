import { Fragment, useEffect, useState } from 'react';
import { IoAdd } from 'react-icons/io5';
import { toast } from 'react-toastify';

import { DeleteIcon } from 'assets/icons';
import clsx from 'clsx';
import { Button } from 'common/Button';
import { Card, CardTitle } from 'common/Card';
import CustomCheckbox from 'common/CustomCheckbox';
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
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import FeatureModal from 'features/feature/FeatureModal';
import FeatureViewModal from 'features/feature/FeatureViewModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Edit, Eye, Package, Package2 } from 'lucide-react';
import { IFeatureDetails } from 'models/Feature';
import { IGetListParams, IList, IResponse, Status } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';

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

const FeatureList = () => {
  const [selectedFeatures, setSelectedFeatures] = useState<Array<string>>([]);
  const [queryString, setQueryString] = useState<string>('');
  const [showModal, setShowModal] = useState<ModalType>(ModalType.None);
  const [dialogType, setDialogType] = useState<ConfirmDialogType>(
    ConfirmDialogType.None,
  );
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [featureId, setFeatureId] = useState<string>('');
  const [featureDetails, setFeatureDetails] = useState<IFeatureDetails>();
  const searchDebounce = useDebounce(queryString, 1000);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    pageSize: 10,
    offset: 0,
  });
  const [data, setData] = useState<IList<any>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });

  const apiClient = useAPI();

  // set query string when query params change
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  // fetching feature data
  useEffect(() => {
    if (searchDebounce) {
      fetchFeatureData();
    }
  }, [searchDebounce]);

  const DialogMap = {
    [ConfirmDialogType.None]: {
      message: '',
      loadingText: '',
      onConfirm: () => {},
    },
    [ConfirmDialogType.Delete]: {
      message: 'Are you sure you want to delete this feature?',
      loadingText: 'Deleting...',
      onConfirm: () => handleDeleteFeature(),
    },
    [ConfirmDialogType.BulkEnable]: {
      message: 'Are you sure you want to enable the selected features?',
      loadingText: 'Enabling...',
      onConfirm: () => handleBulkEnable(),
    },
    [ConfirmDialogType.BulkDisable]: {
      message: 'Are you sure you want to disable the selected features?',
      loadingText: 'Disabling...',
      onConfirm: () => handleBulkDisable(),
    },
    [ConfirmDialogType.BulkDelete]: {
      message: 'Are you sure you want to delete the selected features?',
      loadingText: 'Deleting...',
      onConfirm: () => handleBulkDelete(),
    },
    [ConfirmDialogType.BulkExport]: {
      message: 'Are you sure you want to export the selected features?',
      loadingText: 'Exporting...',
      onConfirm: () => handleExportCSV(),
    },
  };

  // fetching feature list data function
  const fetchFeatureData = async () => {
    try {
      const response: IResponse<IList<IFeatureDetails>> = await apiClient.get(
        API_END_POINTS.FEATURE_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching feature data:', error);
    } finally {
      setLoading(false);
    }
  };

  // view modal open and set (feature details, feature id) function
  const handleViewModal = (feature: any) => {
    setShowModal(ModalType.View);
    setFeatureDetails(feature);
    setFeatureId(feature.id);
  };

  // create modal open and set (feature details, feature id) function
  const handleCreateModal = () => {
    setShowModal(ModalType.General);
    setFeatureDetails(undefined);
    setFeatureId('');
  };

  // edit modal open and set (feature details, feature id) function
  const handleEditModal = (feature: any) => {
    setShowModal(ModalType.General);
    setFeatureDetails(feature);
    setFeatureId(feature.id);
  };

  // select all features function
  const handleSelectAll = () => {
    if (selectedFeatures.length === data.items.length) {
      setSelectedFeatures([]);
    } else {
      setSelectedFeatures(data.items.map(feature => feature.id));
    }
  };

  // single select feature function
  const handleSelectFeature = (id: string) => {
    setSelectedFeatures(prevSelected =>
      prevSelected.includes(id)
        ? prevSelected.filter(featureId => featureId !== id)
        : [...prevSelected, id],
    );
  };

  // page change function
  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  // search function
  // const handleSearch = () => fetchFeatureData();

  // hide modal function
  const handleHideModal = () => {
    setShowModal(ModalType.None);
    setFeatureId('');
    setFeatureDetails(undefined);
  };

  const handleClickDelete = (id: string) => {
    setFeatureId(id);
    setDialogType(ConfirmDialogType.Delete);
  };

  // single feature delete function
  const handleDeleteFeature = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.del(
        API_END_POINTS.FEATURE_DELETE(featureId),
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      fetchFeatureData();
    } catch (error: any) {
      console.error('Error delete feature:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
      setFeatureId('');
    }
  };

  const handleBulkEnable = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.put(API_END_POINTS.FEATURE_BULK_UPDATE, {
        data: { ids: selectedFeatures, status: Status.ENABLED },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedFeatures([]);
      fetchFeatureData();
    } catch (error: any) {
      console.error('Error enabling features:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleBulkDisable = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.put(API_END_POINTS.FEATURE_BULK_UPDATE, {
        data: { ids: selectedFeatures, status: Status.DISABLED },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedFeatures([]);
      fetchFeatureData();
    } catch (error: any) {
      console.error('Error disabling features:', error);
      toast.error(error.message);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  const handleBulkDelete = async () => {
    setSubmitting(true);

    try {
      const response = await apiClient.del(API_END_POINTS.FEATURE_BULK_DELETE, {
        data: { ids: selectedFeatures },
      });
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      setSelectedFeatures([]);
      fetchFeatureData();
    } catch (error: any) {
      console.error('Error delete features:', error);
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
        API_END_POINTS.FEATURE_BULK_EXPORT,
        { data: { ids: selectedFeatures } },
      );

      const blob = new Blob([response], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'features.csv';
      a.click();
    } catch (error) {
      console.error('Error exporting CSV:', error);
    } finally {
      setSubmitting(false);
      setDialogType(ConfirmDialogType.None);
    }
  };

  // Check if all available features are selected
  const isAllSelected =
    selectedFeatures.length === data.items?.length && data.items?.length > 0;

  // Define table columns configuration for feature list
  // Each column specifies how to render and format feature data
  // Includes checkbox for selection, basic info fields, status indicator, and action buttons
  const columns = [
    {
      key: 'select',
      header: (
        <div className="content-relative">
          <CustomCheckbox checked={isAllSelected} onChange={handleSelectAll} />
        </div>
      ),
      render: (feature: any) => (
        <div className="content-relative">
          <CustomCheckbox
            checked={selectedFeatures.includes(feature.id)}
            onChange={() => handleSelectFeature(feature.id)}
          />
        </div>
      ),
    },
    {
      key: 'featureName',
      header: 'Name',
      tdClassName: 'content-w-[18%] content-text-left',
    },
    {
      key: 'featureDescription',
      header: 'Description',
      tdClassName: 'content-w-[24%] content-text-left',
      render: (feature: any) => {
        const description = feature.featureDescription || '';
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
      key: 'featureStatus',
      header: 'Status',
      render: (feature: any) => (
        <span
          className={clsx(
            'content-rounded content-px-2 content-py-1 content-capitalize',
            feature?.featureStatus === Status.ENABLED
              ? 'content-bg-success content-bg-opacity-10 content-text-success'
              : 'content-bg-vibrant-red content-bg-opacity-10 content-text-vibrant-red',
          )}
        >
          {typeof feature?.featureStatus === 'object'
            ? 'N/A'
            : feature?.featureStatus
              ? String(feature?.featureStatus)?.toLowerCase()
              : 'N/A'}
        </span>
      ),
    },
    {
      key: 'availability',
      header: 'Availability',
      render: (feature: any) => (
        <span className="content-capitalize">
          {typeof feature?.availability === 'object'
            ? 'N/A'
            : feature?.availability
              ? String(feature?.availability)?.toLowerCase()
              : 'N/A'}
        </span>
      ),
    },
    {
      key: 'action',
      header: 'Action',
      render: (feature: any) => (
        <div className="content-flex content-justify-center content-gap-x-4">
          <Button
            onClick={() => handleViewModal(feature)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <Eye className="content-size-5 content-text-cloudy-white" />
          </Button>
          <Button
            onClick={() => handleEditModal(feature)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <Edit className="content-size-5 content-text-cloudy-white" />
          </Button>
          <Button
            onClick={() => handleClickDelete(feature.id)}
            className="content-bg-transparent content-p-0 hover:!content-bg-transparent"
          >
            <DeleteIcon fill="#FFFFFFBF" width={18} height={18} />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Fragment>
      <section>
        <div className="content-flex content-items-center content-gap-3">
          <Package className="content-size-8 content-text-primary" />
          <div>
            <h1 className="content-text-3xl content-font-bold content-text-white">
              Feature Management
            </h1>
            <p className="content-text-cloudy-white">Manage your Features</p>
          </div>
        </div>
        <Card className="content-mt-6 content-p-6">
          <div className="content-flex content-justify-between content-text-secondary">
            <CardTitle className="content-flex content-items-center content-gap-x-2">
              <Package2 />
              Feature List {data.total > 0 && `(${data.total})`}
            </CardTitle>
            <div className="content-flex content-gap-x-5">
              <div className="content-flex content-text-white">
                <Input
                  id="search"
                  placeholder="Search Features..."
                  className="content-mr-2 content-w-96"
                  value={queryParams.search}
                  onChange={e =>
                    setQueryParams(prevState => ({
                      ...prevState,
                      search: e.target.value,
                    }))
                  }
                />
                <Button onClick={handleCreateModal} size="sm">
                  <IoAdd className="content-text-2xl" /> Create Feature
                </Button>
              </div>
            </div>
          </div>
          {selectedFeatures.length > 0 && (
            <div className="content-mt-6">
              <BulkAction
                selectedIds={selectedFeatures}
                onEnable={() => setDialogType(ConfirmDialogType.BulkEnable)}
                onDisable={() => setDialogType(ConfirmDialogType.BulkDisable)}
                onDelete={() => setDialogType(ConfirmDialogType.BulkDelete)}
                onExportCSV={() => setDialogType(ConfirmDialogType.BulkExport)}
              />
            </div>
          )}
          <div className="content-mt-6">
            {loading ? (
              <AssignedPackagesLoader />
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
                          {column.render ? column.render(row) : row[column.key]}
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
        </Card>
      </section>

      <FeatureModal
        isOpen={showModal === ModalType.General}
        onClose={handleHideModal}
        onSubmit={() => fetchFeatureData()}
        formData={featureDetails}
        featureId={featureId}
      />

      <FeatureViewModal
        isOpen={showModal === ModalType.View}
        onClose={handleHideModal}
        data={featureDetails}
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
export default FeatureList;

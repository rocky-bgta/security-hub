import { Edit, Eye, Plus, RefreshCcw, Search, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import ConfirmDialog from 'components/ConfirmDialog';
import TableLoader from 'components/skeleton/TableLoader';
import TemplateCreateDialog from 'features/certificate/aspire-admin/TemplateCreateDialog';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { ICertificateTemplate } from 'models/Certificate';
import {
  CourseStatus,
  IGetListParams,
  IList,
  IResponse,
  ModalType,
  Status,
} from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import CertificateViewDetails from './CertificateViewDetails';
import ViewCertificateBackgroundModal from './ViewCertificateBackground';

const CertificateTemplateList = () => {
  const apiClient = useAPI();
  const [templates, setTemplates] = useState<ICertificateTemplate[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isModalShow, setIsModalShow] = useState<ModalType>(ModalType.NONE);
  const [
    isViewCertificateBackgroundModalShow,
    setIsViewCertificateBackgroundModalShow,
  ] = useState<boolean>(false);
  const [templateId, setTemplateId] = useState<string>('');
  const [isDeleteLoading, setIsDeleteLoading] = useState<boolean>(false);
  const [backgroundImageUrl, setBackgroundImageUrl] = useState<string>('');
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
    search: '',
    status: CourseStatus.ALL,
  });
  const searchDebounce = useDebounce<string>(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchTemplates();
    }
  }, [searchDebounce]);

  const fetchTemplates = async () => {
    setLoading(true);
    try {
      const response: IResponse<IList<ICertificateTemplate>> =
        await apiClient.get(
          API_END_POINTS.CERTIFICATE_TEMPLATE_LIST + queryString,
        );

      if (isSuccessResponse(response.statusCode)) {
        setTemplates(response.data?.items || []);
      } else {
        toast.error('Failed to fetch certificate templates');
      }
    } catch (error) {
      console.error('Error fetching certificate templates:', error);
      toast.error('An error occurred while fetching templates');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (templateId: string) => {
    try {
      setIsDeleteLoading(true);
      const response: IResponse<any> = await apiClient.del(
        API_END_POINTS.CERTIFICATE_TEMPLATE_DELETE(templateId),
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Certificate template deleted successfully');
        fetchTemplates();
      } else {
        toast.error('Failed to delete template');
      }
    } catch (error) {
      console.error('Error deleting template:', error);
      toast.error('An error occurred while deleting template');
    } finally {
      setIsModalShow(ModalType.NONE);
      setIsDeleteLoading(false);
    }
  };

  const handleTemplateCreated = () => {
    fetchTemplates();
  };

  const handleViewTemplate = (templateId: string) => {
    setTemplateId(templateId);
    setIsModalShow(ModalType.VIEW);
  };

  const handleEditTemplate = (templateId: string) => {
    setTemplateId(templateId);
    setIsModalShow(ModalType.ADD);
  };

  const handleViewCertificateBackground = (backgroundImageUrl: string) => {
    setBackgroundImageUrl(backgroundImageUrl);
    setIsViewCertificateBackgroundModalShow(true);
  };

  return (
    <div className="content-space-y-6">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-foreground">
            Certificate Templates
          </h1>
          <p className="content-text-muted-foreground">
            Design and manage certificate templates with dynamic fields
          </p>
        </div>

        <Button
          onClick={() => {
            setTemplateId('');
            setIsModalShow(ModalType.ADD);
          }}
          className="content-flex content-items-center"
        >
          <Plus className="content-mr-2 content-size-4" />
          Create Template
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Certificate Templates</CardTitle>
          <CardDescription>
            Manage certificate templates with custom backgrounds and dynamic
            fields
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="content-mb-6 content-grid content-grid-cols-5 content-gap-4">
            <div className="content-relative content-col-span-3">
              <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
              <Input
                placeholder="Search templates..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams(prev => ({ ...prev, search: e.target.value }))
                }
                className="content-pl-10"
              />
            </div>
            <Select
              value={queryParams.status}
              onValueChange={value =>
                setQueryParams(prev => ({
                  ...prev,
                  status:
                    value === 'all'
                      ? ('' as CourseStatus)
                      : (value as CourseStatus),
                }))
              }
            >
              <SelectTrigger className="content-col-span-1 content-w-full">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value={Status.ENABLED}>Active</SelectItem>
                <SelectItem value={Status.DISABLED}>Inactive</SelectItem>
              </SelectContent>
            </Select>

            <Button
              variant="outline"
              className="content-col-span-1"
              onClick={() =>
                setQueryParams(prev => ({
                  ...prev,
                  offset: 0,
                  search: '',
                  status: '' as CourseStatus,
                }))
              }
            >
              <RefreshCcw className="content-size-4" />
              Reset
            </Button>
          </div>

          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sr. No.</TableHead>
                  <TableHead>Template Name</TableHead>
                  <TableHead>Background</TableHead>
                  <TableHead>Trial Template</TableHead>
                  <TableHead>Default Template</TableHead>
                  <TableHead>Created Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {templates.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={10} className="content-text-center">
                      No templates found. Create your first template to get
                      started.
                    </TableCell>
                  </TableRow>
                ) : (
                  templates.map((template, index) => {
                    return (
                      <TableRow key={template.id}>
                        <TableCell>{index + 1}</TableCell>

                        <TableCell>{template.templateName}</TableCell>
                        <TableCell
                          onClick={() =>
                            handleViewCertificateBackground(
                              FILE_PATH_PREFIX + template.backgroundImageUrl,
                            )
                          }
                        >
                          {template.backgroundImageUrl ? (
                            <img
                              src={
                                FILE_PATH_PREFIX + template.backgroundImageUrl
                              }
                              alt="Template"
                              className="content-h-8 content-w-10 content-rounded content-object-cover"
                              onError={e => {
                                const target = e.target as HTMLImageElement;
                                target.style.display = 'none';
                                const parent = target.parentElement;
                                if (parent) {
                                  parent.textContent = 'No Image';
                                }
                              }}
                            />
                          ) : (
                            'No Image'
                          )}
                        </TableCell>
                        <TableCell>
                          {template.isTrialTemplate ? (
                            <Badge variant="default">Trial</Badge>
                          ) : (
                            <Badge variant="secondary">Not Trial</Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          {template.isDefault ? (
                            <Badge variant="default">Default</Badge>
                          ) : (
                            <Badge variant="secondary">Not Default</Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          {template.createdAt
                            ? new Date(template.createdAt).toLocaleDateString()
                            : 'N/A'}
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={
                              template.status === Status.ENABLED
                                ? 'default'
                                : 'secondary'
                            }
                          >
                            {template.status === Status.ENABLED
                              ? 'Active'
                              : 'Inactive'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="content-flex content-gap-2">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleViewTemplate(template.id)}
                              title="View"
                            >
                              <Eye className="content-size-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleEditTemplate(template.id)}
                              title="Edit"
                            >
                              <Edit className="content-size-4" />
                            </Button>

                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => {
                                setTemplateId(template.id);
                                setIsModalShow(ModalType.DELETE);
                              }}
                              disabled={isDeleteLoading}
                              title="Delete"
                            >
                              <Trash2 className="content-size-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {isModalShow === ModalType.ADD && (
        <TemplateCreateDialog
          isOpen={isModalShow === ModalType.ADD}
          id={templateId}
          setIsOpen={() => setIsModalShow(ModalType.NONE)}
          onSubmit={handleTemplateCreated}
        />
      )}
      {isModalShow === ModalType.VIEW && (
        <CertificateViewDetails
          isOpen={isModalShow === ModalType.VIEW}
          setIsOpen={() => setIsModalShow(ModalType.NONE)}
          id={templateId}
        />
      )}

      <ConfirmDialog
        isOpen={isModalShow === ModalType.DELETE}
        onClose={() => setIsModalShow(ModalType.NONE)}
        onConfirm={() => handleDelete(templateId)}
        message="Are you sure you want to delete this template?"
        loadingText="Deleting..."
        buttonText="Delete"
        loading={isDeleteLoading}
      />
      {isViewCertificateBackgroundModalShow && (
        <ViewCertificateBackgroundModal
          isOpen={isViewCertificateBackgroundModalShow}
          onClose={() => setIsViewCertificateBackgroundModalShow(false)}
          data={backgroundImageUrl}
        />
      )}
    </div>
  );
};

export default CertificateTemplateList;

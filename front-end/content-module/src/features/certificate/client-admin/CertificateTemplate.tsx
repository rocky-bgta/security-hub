import { Award, Eye } from 'lucide-react';

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
import TableLoader from 'components/skeleton/TableLoader';
import TrialUpgradePopup from 'components/TrialUpgradePopup';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IClientCertificateTemplate } from 'models/Certificate';
import { IGetListParams, IList } from 'models/Global';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import ViewCertificateBackgroundModal from '../aspire-admin/ViewCertificateBackground';
import ClientCertificateViewDetails from './CertificateViewDetails';
import { routes } from 'routes/Routes';
import { useNavigate } from 'react-router-dom';

const CertificateTemplate = () => {
  const navigate = useNavigate();
  const { userInfo } = useStore();

  const apiClient = useAPI();
  const [templates, setTemplates] = useState<IList<IClientCertificateTemplate>>(
    {
      ...InitGetListParams,
      total: 0,
      items: [],
    },
  );

  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
    search: '',
  });
  const [isAssignTemplateOpen, setIsAssignTemplateOpen] = useState(false);
  const [templateId, setTemplateId] = useState<string>('');
  const [isAssignTemplateLoading, setIsAssignTemplateLoading] = useState(false);
  const [isViewTemplateDetailsOpen, setIsViewTemplateDetailsOpen] =
    useState(false);
  const [loading, setLoading] = useState(true);
  const [backgroundImageUrl, setBackgroundImageUrl] = useState<string>('');
  const [
    isPreviewBackgroundImageModalOpen,
    setIsPreviewBackgroundImageModalOpen,
  ] = useState(false);
  const [isTrialPopupOpen, setIsTrialPopupOpen] = useState(false);
  const [trialToggleId, setTrialToggleId] = useState<string | null>(null);
  const searchDebounce = useDebounce(queryString, 500);
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
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_CERTIFICATE_TEMPLATE + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setTemplates({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
      } else {
        toast.error('Failed to fetch templates');
      }
    } catch (error) {
      console.error('Error fetching templates:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewTemplate = (id: string) => {
    setTemplateId(id);
    setIsViewTemplateDetailsOpen(true);
  };

  const handleAssignTemplate = async (id: string) => {
    try {
      setIsAssignTemplateLoading(true);
      const response = await apiClient.post(
        API_END_POINTS.ASSIGN_CLIENT_CERTIFICATE_TEMPLATE,
        {
          data: {
            templateId: id,
          },
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        setTemplates(prev => ({
          ...prev,
          items: prev.items.map(item => ({
            ...item,
            isAssigned: item.id === id,
          })),
        }));
        toast.success('Template assigned successfully');
        setIsAssignTemplateOpen(false);
        setTemplateId('');
      } else {
        toast.error('Failed to assign template');
      }
    } catch (error) {
      console.error('Error assigning template:', error);
      toast.error('Failed to assign template');
    } finally {
      setIsAssignTemplateLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handlePreviewBackgroundImage = (backgroundImageUrl: string) => {
    setBackgroundImageUrl(backgroundImageUrl);
    setIsPreviewBackgroundImageModalOpen(true);
  };

  const handleClosePreviewBackgroundImage = () => {
    setIsPreviewBackgroundImageModalOpen(false);
    setBackgroundImageUrl('');
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div>
        <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
          Certificate Management
        </h1>
        <p className="content-text-muted-foreground">
          Manage certificates templates
        </p>
      </div>

      <Card>
        <CardHeader>
          <div className="content-flex content-items-center content-justify-between">
            <div className="content-flex content-flex-col content-gap-2">
              <CardTitle className="content-flex content-items-center content-gap-2">
                <Award className="content-size-5" />
                Certificate Templates
              </CardTitle>
              <CardDescription className="content-text-muted-foreground">
                Select one certificate template and assign to your users
              </CardDescription>
            </div>
            <Input
              value={queryParams.search}
              onChange={e =>
                setQueryParams(prev => ({
                  ...prev,
                  search: e.target.value,
                }))
              }
              className="content-w-1/3"
              placeholder="Search templates..."
            />
          </div>
        </CardHeader>
        <CardContent>
          <div>
            {loading ? (
              <TableLoader count={10} />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Sr. No.</TableHead>
                    <TableHead>Template Name</TableHead>
                    <TableHead className="content-text-center">
                      Certificate Template
                    </TableHead>
                    <TableHead className="content-text-center">Default</TableHead>
                    <TableHead>Assigned</TableHead>
                    <TableHead className="content-text-center">
                      Action
                    </TableHead>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {templates?.items?.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="content-text-center">
                        No templates found
                      </TableCell>
                    </TableRow>
                  ) : (
                    templates?.items
                      ?.filter(tem => {
                        if (userInfo.onboardBy === 'TRIAL') {
                          return true;
                        }
                        return tem.isTrialTemplate !== true;
                      })
                      ?.map((tem, index) => {
                        const isTrialUser = userInfo.onboardBy === 'TRIAL';

                        return (
                          <TableRow key={tem.id}>
                            <TableCell>{index + 1}</TableCell>
                            <TableCell>{tem.templateName}</TableCell>
                            <TableCell
                              onClick={() =>
                                handlePreviewBackgroundImage(
                                  FILE_PATH_PREFIX + tem.backgroundImageUrl,
                                )
                              }
                              className="content-flex content-cursor-pointer content-items-center content-justify-center"
                            >
                              <img
                                src={FILE_PATH_PREFIX + tem.backgroundImageUrl}
                                alt={tem.templateName}
                                className="content-h-8 content-w-10 content-rounded content-object-cover"
                              />
                            </TableCell>
                            <TableCell className="content-text-center">
                              {tem.isDefault ? (
                                <div>
                                  <span className="content-rounded-full content-bg-green-500/10 content-px-3 content-py-1 content-text-green-500">
                                    Default Template
                                  </span>
                                </div>
                              ) : (
                                <div>Not Default Template</div>
                              )}
                            </TableCell>
                            <TableCell>
                              <Switch
                                checked={
                                  tem.isAssigned || trialToggleId === tem.id
                                }
                                disabled={tem.isAssigned}
                                onCheckedChange={() => {
                                  if (isTrialUser) {
                                    setTrialToggleId(tem.id);
                                    setIsTrialPopupOpen(true);
                                    return;
                                  }
                                  setTemplateId(tem.id);
                                  setIsAssignTemplateOpen(true);
                                }}
                                title={
                                  tem.isAssigned
                                    ? 'Already assigned'
                                    : 'Assign template'
                                }
                                className="content-cursor-pointer"
                              />
                            </TableCell>

                            <TableCell className="content-flex content-items-center content-justify-center content-gap-2">
                              <Button
                                variant="outline"
                                size="icon"
                                title="View template"
                                onClick={() => handleViewTemplate(tem.id)}
                              >
                                <Eye className="content-size-5 content-cursor-pointer" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        );
                      })
                  )}
                </TableBody>
              </Table>
            )}
            <Pagination
              total={templates.items.length || 0}
              perPage={queryParams.pageSize || 10}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      <ConfirmDialog
        isOpen={isAssignTemplateOpen}
        loading={isAssignTemplateLoading}
        message="Are you sure you want to assign this template?"
        loadingText="Assigning..."
        onClose={() => setIsAssignTemplateOpen(false)}
        onConfirm={() => handleAssignTemplate(templateId)}
        buttonText="Assign"
      />

      <TrialUpgradePopup
        isOpen={isTrialPopupOpen}
        message="First, you need to buy a product to unlock the certificate template."
        onClose={() => {
          navigate(routes.buyProduct.path);
        }}
      />

      {setIsViewTemplateDetailsOpen && (
        <ClientCertificateViewDetails
          isOpen={isViewTemplateDetailsOpen}
          setIsOpen={setIsViewTemplateDetailsOpen}
          id={templateId}
          isAssigned={
            templates?.items?.find(item => item.id === templateId)?.isAssigned ||
            false
          }
        />
      )}
      <ViewCertificateBackgroundModal
        isOpen={isPreviewBackgroundImageModalOpen}
        onClose={handleClosePreviewBackgroundImage}
        data={backgroundImageUrl}
      />
    </div>
  );
};

export default CertificateTemplate;

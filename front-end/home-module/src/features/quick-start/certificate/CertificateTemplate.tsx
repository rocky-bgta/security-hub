import {
  forwardRef,
  Fragment,
  useCallback,
  useEffect,
  useImperativeHandle,
  useState,
} from 'react';
import { Award, Eye } from 'lucide-react';
import { toast } from 'react-toastify';

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import { Switch } from 'common/Switch';
import { Button } from 'common/Button';
import Pagination from 'common/Pagination';
import { IGetListParams, IList, Status } from 'models/Global';
import useDebounce from 'hooks/UseDebounce';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import ClientCertificateViewDetails from './CertificateViewDetails';
import ViewCertificateBackgroundModal from './ViewCertificateBackground';
import useStore from 'hooks/UseStore';
import AssignedPackagesLoader from '../assign/AssignedPackagesLoader';
import ConfirmDialog from 'components/ConfirmDialog';

export interface IClientCertificateTemplate {
  id: string;
  templateName: string;
  certificateTitle: string;
  certificateType: string;
  acknowledgement: string;
  completionStatus: string;
  completionTitle: string;
  logoImageUrl: string;
  signatureImageUrl: string;
  signerName: string;
  signerDesignation: string;
  signatureIdentity: string;
  backgroundImageUrl: string;
  dynamicFields: {
    showLearnerName: boolean;
    showCourseName: boolean;
    showIssueDate: boolean;
    showCertificateId: boolean;
    showQrCode: boolean;
  };
  status: Status;
  isDefault: boolean;
  isTrialTemplate: boolean;
  isAssigned: boolean;
}

interface IProps {
  stepId: number;
  updateStepsStatus: (
    stepId: number,
    status: 'complete' | 'incomplete',
  ) => void;
}

export interface ICertificateTemplateHandle {
  setTemplate: () => Promise<boolean>;
  hasAssignedTemplate: () => boolean;
}

const CertificateTemplate = forwardRef<ICertificateTemplateHandle, IProps>(
  ({ stepId, updateStepsStatus }, ref) => {
    const { userInfo } = useStore();

    const apiClient = useAPI();
    const [templates, setTemplates] = useState<
      IList<IClientCertificateTemplate>
    >({
      ...InitGetListParams,
      total: 0,
      items: [],
    });
    const [queryString, setQueryString] = useState<string>('');
    const [queryParams, setQueryParams] = useState<IGetListParams>({
      offset: 0,
      pageSize: 1000,
      search: '',
    });
    const [templateId, setTemplateId] = useState<string>('');
    const [pendingTemplateId, setPendingTemplateId] = useState<string>('');
    const [isAssignTemplateOpen, setIsAssignTemplateOpen] = useState(false);
    const [isAssignTemplateLoading, setIsAssignTemplateLoading] =
      useState(false);
    const [isViewTemplateDetailsOpen, setIsViewTemplateDetailsOpen] =
      useState(false);
    const [loading, setLoading] = useState(true);
    const [backgroundImageUrl, setBackgroundImageUrl] = useState<string>('');
    const [
      isPreviewBackgroundImageModalOpen,
      setIsPreviewBackgroundImageModalOpen,
    ] = useState(false);

    const searchDebounce = useDebounce(queryString, 500);

    useEffect(() => {
      const resp = objectToQueryString(queryParams);
      setQueryString(resp);
    }, [queryParams]);

    const fetchTemplates = useCallback(async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.CLIENT_ADMIN_CERTIFICATE_TEMPLATE + queryString,
        );
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error('Failed to fetch templates');
        }

        setTemplates({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
      } catch (error) {
        console.error('Error fetching templates:', error);
        toast.error('Failed to fetch templates');
      } finally {
        setLoading(false);
      }
    }, [apiClient, queryString]);

    useEffect(() => {
      if (searchDebounce) {
        fetchTemplates();
      }
    }, [searchDebounce, fetchTemplates]);

    const handleViewTemplate = (id: string) => {
      setTemplateId(id);
      setIsViewTemplateDetailsOpen(true);
    };

    const applyAssignedTemplateLocally = (id: string) => {
      setTemplates(prev => ({
        ...prev,
        items: prev.items.map(item => ({
          ...item,
          isAssigned: item.id === id,
        })),
      }));
    };

    const handleAssignTemplate = async (id: string) => {
      if (!id) {
        toast.error('Please select a certificate template');
        return false;
      }

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
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error('Failed to assign template');
        }

        applyAssignedTemplateLocally(id);
        setIsAssignTemplateOpen(false);
        setPendingTemplateId('');
        toast.success('Template assigned successfully');
        updateStepsStatus(stepId, 'complete');
        return true;
      } catch (error) {
        console.error('Error assigning template:', error);
        toast.error('Failed to assign template');
        return false;
      } finally {
        setIsAssignTemplateLoading(false);
      }
    };

    const handleOpenAssignConfirm = (id: string) => {
      const alreadyAssigned = templates.items?.find(
        item => item.id === id && item.isAssigned,
      );
      if (alreadyAssigned) return;

      setPendingTemplateId(id);
      setIsAssignTemplateOpen(true);
    };

    const handleCloseAssignConfirm = () => {
      if (isAssignTemplateLoading) return;
      setIsAssignTemplateOpen(false);
      setPendingTemplateId('');
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

    useImperativeHandle(ref, () => ({
      setTemplate: async () => {
        const assignedTemplate = templates.items?.find(item => item.isAssigned);
        if (assignedTemplate) return true;

        toast.error('Please select a certificate template');
        return false;
      },
      hasAssignedTemplate: () =>
        Boolean(templates.items?.some(item => item.isAssigned)),
    }));

    return (
      <Fragment>
        <Card>
          <CardHeader>
            <div className="home-flex home-items-center home-justify-between">
              <div>
                <CardTitle className="home-flex home-items-center home-gap-2">
                  <Award className="home-size-5" />
                  Certificate Templates
                </CardTitle>
                <CardDescription className="home-text-muted-foreground">
                  Select one certificate template and assign
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <AssignedPackagesLoader count={5} />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Sr. No.</TableHead>
                    <TableHead>Template Name</TableHead>
                    <TableHead className="home-text-center">
                      Certificate Template
                    </TableHead>
                    <TableHead className="home-text-center">Default</TableHead>
                    <TableHead>Assigned</TableHead>
                    <TableHead className="home-text-center">Action</TableHead>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {templates?.total === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="home-text-center">
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
                        const isSwitchDisabled = userInfo.onboardBy === 'TRIAL';

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
                              className="home-flex home-cursor-pointer home-items-center home-justify-center"
                            >
                              <img
                                src={FILE_PATH_PREFIX + tem.backgroundImageUrl}
                                alt={tem.templateName}
                                className="home-h-8 home-w-10 home-rounded home-object-cover"
                              />
                            </TableCell>
                            <TableCell className="home-text-center">
                              {tem.isDefault ? (
                                <div className="home-w-fit home-rounded-full home-bg-green-500/10 home-px-3 home-py-1 home-text-green-500">
                                  Default Template
                                </div>
                              ) : (
                                <div>-</div>
                              )}
                            </TableCell>
                            <TableCell>
                              <Switch
                                checked={tem.isAssigned}
                                disabled={
                                  isSwitchDisabled ||
                                  isAssignTemplateLoading ||
                                  tem.isAssigned
                                }
                                onCheckedChange={() => {
                                  if (isSwitchDisabled || tem.isAssigned)
                                    return;
                                  handleOpenAssignConfirm(tem.id);
                                }}
                                title={
                                  isSwitchDisabled
                                    ? 'Trial user cannot assign template. Upgrade to premium to assign template'
                                    : tem.isAssigned
                                      ? 'Already assigned'
                                      : 'Assign template'
                                }
                                className={
                                  isSwitchDisabled || tem.isAssigned
                                    ? 'home-cursor-not-allowed'
                                    : 'home-cursor-pointer'
                                }
                              />
                            </TableCell>

                            <TableCell className="home-flex home-items-center home-justify-center home-gap-2">
                              <Button
                                variant="outline"
                                size="icon"
                                title="View template"
                                onClick={() => handleViewTemplate(tem.id)}
                              >
                                <Eye className="home-size-5 home-cursor-pointer" />
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
              total={templates.total || 0}
              perPage={queryParams.pageSize || 10}
              onPageChange={onPageChangeHandler}
            />
          </CardContent>
        </Card>

        <ConfirmDialog
          isOpen={isAssignTemplateOpen}
          loading={isAssignTemplateLoading}
          message="Are you sure you want to assign this template?"
          loadingText="Assigning..."
          buttonText="Assign"
          onClose={handleCloseAssignConfirm}
          onConfirm={() => handleAssignTemplate(pendingTemplateId)}
        />

        <ClientCertificateViewDetails
          isOpen={isViewTemplateDetailsOpen}
          setIsOpen={setIsViewTemplateDetailsOpen}
          id={templateId}
          isAssigned={
            templates?.items?.find(item => item.id === templateId)
              ?.isAssigned || false
          }
        />

        <ViewCertificateBackgroundModal
          isOpen={isPreviewBackgroundImageModalOpen}
          onClose={handleClosePreviewBackgroundImage}
          data={backgroundImageUrl}
        />
      </Fragment>
    );
  },
);

CertificateTemplate.displayName = 'CertificateTemplate';

export default CertificateTemplate;

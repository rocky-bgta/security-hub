import { useEffect, useState } from 'react';
import { Award, Image as ImageIcon } from 'lucide-react';

import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Status } from 'models/Global';
import { isSuccessResponse } from 'utils/Helper';
import { API_END_POINTS } from 'routes/APIEndpoints';
import useAPI from 'hooks/UseAPI';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  id: string;
  isAssigned: boolean;
}

export interface ICertificateTemplateDetails {
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
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
  isDefault: boolean;
  isTrialTemplate: boolean;
}

const ClientCertificateViewDetails = ({
  isOpen,
  setIsOpen,
  id,
  isAssigned,
}: IProps) => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [certificate, setCertificate] =
    useState<ICertificateTemplateDetails | null>(null);

  const fetchCertificate = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_CERTIFICATE_TEMPLATE_DETAILS.replace(':id', id),
      );
      if (isSuccessResponse(response.statusCode)) {
        setCertificate(response.data);
      }
    } catch (error) {
      console.error('Error fetching certificate:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && id) {
      fetchCertificate();
    }
  }, [isOpen, id]);

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="home-max-h-[90vh] home-w-2/3 home-overflow-y-auto home-text-white">
        <DialogHeader>
          <DialogTitle className="home-flex home-items-center home-gap-2">
            <Award className="home-size-5" />
            Certificate Template Details
          </DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="home-flex home-items-center home-justify-center home-py-8">
            <p className="home-text-muted-foreground">Loading...</p>
          </div>
        ) : certificate ? (
          <div className="home-space-y-6">
            {/* Template Name and Status */}
            <div className="home-grid home-grid-cols-2 home-gap-4">
              <div>
                <h4 className="home-mb-2 home-text-sm home-font-semibold home-text-muted-foreground">
                  Template Name
                </h4>
                <p className="home-text-base home-font-medium">
                  {certificate.templateName}
                </p>
              </div>
              <div>
                <h4 className="home-mb-2 home-text-sm home-font-semibold home-text-muted-foreground">
                  Assigned
                </h4>
                <div className="home-flex home-items-center home-gap-2">
                  <Badge variant={isAssigned ? 'default' : 'secondary'}>
                    {isAssigned ? 'Assigned' : 'Unassigned'}
                  </Badge>
                </div>
              </div>
            </div>

            {/* Background Image */}
            <div>
              <h4 className="home-mb-2 home-text-sm home-font-semibold home-text-muted-foreground">
                Background Image
              </h4>
              {certificate.backgroundImageUrl ? (
                <div className="home-overflow-hidden home-rounded-lg home-border home-border-card-border home-bg-muted/50">
                  <img
                    src={FILE_PATH_PREFIX + certificate.backgroundImageUrl}
                    alt={certificate.templateName}
                    className="home-h-auto home-max-h-64 home-w-full home-object-contain"
                    onError={e => {
                      const target = e.target as HTMLImageElement;
                      target.style.display = 'none';
                    }}
                  />
                </div>
              ) : (
                <div className="home-flex home-items-center home-justify-center home-rounded-lg home-border home-border-card-border home-bg-muted/50 home-py-8 home-text-muted-foreground">
                  <ImageIcon className="home-mr-2 home-size-8" />
                  No Image Available
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="home-flex home-items-center home-justify-center home-py-8">
            <p className="home-text-muted-foreground">
              No certificate template found
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ClientCertificateViewDetails;

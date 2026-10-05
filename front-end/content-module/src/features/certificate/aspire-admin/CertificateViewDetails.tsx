import { Award, Check, Image as ImageIcon, X } from 'lucide-react';

import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { ICertificateTemplateDetails } from 'models/Certificate';
import { Status } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { formatDateAndTime, isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  id: string;
}

const CertificateViewDetails = ({ isOpen, setIsOpen, id }: IProps) => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
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

  const dynamicFieldsLabels = [
    { key: 'showLearnerName', label: 'Learner Name' },
    { key: 'showCourseName', label: 'Course Name' },
    { key: 'showIssueDate', label: 'Issue Date' },
    { key: 'showCertificateId', label: 'Certificate ID' },
    { key: 'showQrCode', label: 'QR Code' },
  ] as const;

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="content-max-h-[90vh] content-w-2/3 content-overflow-y-auto content-text-white">
        <DialogHeader>
          <DialogTitle className="content-flex content-items-center content-gap-2">
            <Award className="content-size-5" />
            Certificate Template Details
          </DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="content-flex content-items-center content-justify-center content-py-8">
            <p className="content-text-muted-foreground">Loading...</p>
          </div>
        ) : certificate ? (
          <div className="content-space-y-6">
            {/* Template Name and Status */}
            <div className="content-grid content-grid-cols-2 content-gap-4">
              <div>
                <h4 className="content-mb-2 content-text-sm content-font-semibold content-text-muted-foreground">
                  Template Name
                </h4>
                <p className="content-text-base content-font-medium">
                  {certificate.templateName}
                </p>
              </div>
              <div>
                <h4 className="content-mb-2 content-text-sm content-font-semibold content-text-muted-foreground">
                  Status
                </h4>
                <div className="content-flex content-items-center content-gap-2">
                  <Badge
                    variant={
                      certificate.status === Status.ENABLED
                        ? 'default'
                        : 'secondary'
                    }
                  >
                    {certificate.status}
                  </Badge>
                  {certificate.isDefault && (
                    <Badge
                      variant="outline"
                      className="content-border-green-500/20 content-bg-green-500/10 content-text-green-500"
                    >
                      Default
                    </Badge>
                  )}
                </div>
              </div>
            </div>

            {/* Certificate Content */}
            <div>
              <h4 className="content-mb-3 content-text-sm content-font-semibold content-text-muted-foreground">
                Certificate Content
              </h4>
              <div className="content-space-y-3">
                <div className="content-grid content-grid-cols-2 content-gap-4">
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Certificate Title
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.certificateTitle || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Certificate Type
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.certificateType || 'N/A'}
                    </p>
                  </div>
                </div>
                <div>
                  <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                    Acknowledgement
                  </h5>
                  <p className="content-break-words content-text-sm content-font-medium">
                    {certificate.acknowledgement || 'N/A'}
                  </p>
                </div>
                <div className="content-grid content-grid-cols-2 content-gap-4">
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Completion Status
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.completionStatus || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Completion Title
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.completionTitle || 'N/A'}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Background Image */}
            <div>
              <h4 className="content-mb-2 content-text-sm content-font-semibold content-text-muted-foreground">
                Background Image
              </h4>
              {certificate.backgroundImageUrl ? (
                <div className="content-border-border content-overflow-hidden content-rounded-lg content-border content-bg-muted/50">
                  <img
                    src={FILE_PATH_PREFIX + certificate.backgroundImageUrl}
                    alt={certificate.templateName}
                    className="content-h-auto content-max-h-64 content-w-full content-object-contain"
                    onError={e => {
                      const target = e.target as HTMLImageElement;
                      target.style.display = 'none';
                    }}
                  />
                </div>
              ) : (
                <div className="content-border-border content-flex content-items-center content-justify-center content-rounded-lg content-border content-bg-muted/50 content-py-8 content-text-muted-foreground">
                  <ImageIcon className="content-mr-2 content-size-8" />
                  No Image Available
                </div>
              )}
            </div>

            {/* Logo Image */}
            <div>
              <h4 className="content-mb-2 content-text-sm content-font-semibold content-text-muted-foreground">
                Logo Image
              </h4>
              {certificate.logoImageUrl ? (
                <div className="content-border-border content-overflow-hidden content-rounded-lg content-border content-bg-muted/50 content-p-4">
                  <img
                    src={FILE_PATH_PREFIX + certificate.logoImageUrl}
                    alt="Logo"
                    className="content-size-auto content-max-h-32 content-object-contain"
                    onError={e => {
                      const target = e.target as HTMLImageElement;
                      target.style.display = 'none';
                    }}
                  />
                </div>
              ) : (
                <div className="content-border-border content-flex content-items-center content-justify-center content-rounded-lg content-border content-bg-muted/50 content-py-8 content-text-muted-foreground">
                  <ImageIcon className="content-mr-2 content-size-8" />
                  No Logo Available
                </div>
              )}
            </div>

            {/* Signature Details */}
            <div>
              <h4 className="content-mb-3 content-text-sm content-font-semibold content-text-muted-foreground">
                Signature Details
              </h4>
              <div className="content-space-y-3">
                <div>
                  <h5 className="content-mb-2 content-text-xs content-font-medium content-text-muted-foreground">
                    Signature Image
                  </h5>
                  {certificate.signatureImageUrl ? (
                    <div className="content-border-border content-overflow-hidden content-rounded-lg content-border content-bg-muted/50 content-p-4">
                      <img
                        src={FILE_PATH_PREFIX + certificate.signatureImageUrl}
                        alt="Signature"
                        className="content-size-auto content-max-h-24 content-object-contain"
                        onError={e => {
                          const target = e.target as HTMLImageElement;
                          target.style.display = 'none';
                        }}
                      />
                    </div>
                  ) : (
                    <div className="content-border-border content-flex content-items-center content-justify-center content-rounded-lg content-border content-bg-muted/50 content-py-6 content-text-sm content-text-muted-foreground">
                      <ImageIcon className="content-mr-2 content-size-6" />
                      No Signature Available
                    </div>
                  )}
                </div>
                <div className="content-grid content-grid-cols-3 content-gap-4">
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Signer Name
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.signerName || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Signer Designation
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.signerDesignation || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                      Signature Identity
                    </h5>
                    <p className="content-text-sm content-font-medium">
                      {certificate.signatureIdentity || 'N/A'}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Dynamic Fields */}
            <div>
              <h4 className="content-mb-3 content-text-sm content-font-semibold content-text-muted-foreground">
                Dynamic Fields
              </h4>
              <div className="content-grid content-grid-cols-2 content-gap-3">
                {dynamicFieldsLabels.map(field => (
                  <div
                    key={field.key}
                    className="content-flex content-items-center content-gap-2 content-rounded content-bg-muted/30 content-p-2"
                  >
                    {certificate.dynamicFields[field.key] ? (
                      <Check className="content-size-4 content-text-green-500" />
                    ) : (
                      <X className="content-size-4 content-text-muted-foreground" />
                    )}
                    <span
                      className={`content-text-sm ${
                        certificate.dynamicFields[field.key]
                          ? 'content-font-medium'
                          : 'content-text-muted-foreground'
                      }`}
                    >
                      {field.label}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Additional Settings */}
            <div>
              <h4 className="content-mb-3 content-text-sm content-font-semibold content-text-muted-foreground">
                Additional Settings
              </h4>
              <div className="content-flex content-items-center content-gap-2">
                {certificate.isTrialTemplate && (
                  <Badge
                    variant="outline"
                    className="content-border-blue-500/20 content-bg-blue-500/10 content-text-blue-500"
                  >
                    Trial Template
                  </Badge>
                )}
                {!certificate.isTrialTemplate && (
                  <Badge
                    variant="outline"
                    className="content-border-muted-foreground/20 content-bg-muted/10 content-text-muted-foreground"
                  >
                    Standard Template
                  </Badge>
                )}
              </div>
            </div>

            {/* Metadata */}
            <div className="content-border-border content-border-t content-pt-4">
              <h4 className="content-mb-3 content-text-sm content-font-semibold content-text-muted-foreground">
                Metadata
              </h4>
              <div className="content-grid content-grid-cols-2 content-gap-4">
                <div>
                  <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                    Created At
                  </h5>
                  <p className="content-text-sm content-font-medium">
                    {certificate.createdAt
                      ? formatDateAndTime(certificate.createdAt)
                      : 'N/A'}
                  </p>
                </div>
                <div>
                  <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                    Created By
                  </h5>
                  <p className="content-text-sm content-font-medium">
                    {certificate.createdBy || 'N/A'}
                  </p>
                </div>
                <div>
                  <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                    Updated At
                  </h5>
                  <p className="content-text-sm content-font-medium">
                    {certificate.updatedAt
                      ? formatDateAndTime(certificate.updatedAt)
                      : 'N/A'}
                  </p>
                </div>
                <div>
                  <h5 className="content-mb-1 content-text-xs content-font-medium content-text-muted-foreground">
                    Updated By
                  </h5>
                  <p className="content-text-sm content-font-medium">
                    {certificate.updatedBy || 'N/A'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="content-flex content-items-center content-justify-center content-py-8">
            <p className="content-text-muted-foreground">
              No certificate template found
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default CertificateViewDetails;

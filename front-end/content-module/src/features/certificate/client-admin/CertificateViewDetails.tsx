import { Award, Image as ImageIcon } from 'lucide-react';

import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { ICertificateTemplateDetails } from 'models/Certificate';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  id: string;
  isAssigned: boolean;
}

const ClientCertificateViewDetails = ({
  isOpen,
  setIsOpen,
  id,
  isAssigned,
}: IProps) => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
  const [certificate, setCertificate] =
    useState<ICertificateTemplateDetails | null>(null);

  useEffect(() => {
    if (!isOpen || !id) return;

    let cancelled = false;
    setCertificate(null);
    setLoading(true);

    const fetchCertificate = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.GET_CERTIFICATE_TEMPLATE_DETAILS.replace(':id', id),
        );
        if (
          !cancelled &&
          isSuccessResponse(response.statusCode)
        ) {
          setCertificate(response.data);
        }
      } catch (error) {
        if (!cancelled) {
          console.error('Error fetching certificate:', error);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchCertificate();

    return () => {
      cancelled = true;
    };
  }, [isOpen, id]);

  const isStaleData = certificate !== null && certificate.id !== id;
  const showLoading = loading || isStaleData;


  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="content-max-h-[90vh] content-w-2/3 content-overflow-y-auto content-text-white">
        <DialogHeader>
          <DialogTitle className="content-flex content-items-center content-gap-2">
            <Award className="content-size-5" />
            Certificate Template Details
          </DialogTitle>
        </DialogHeader>
        {showLoading ? (
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
                  Assigned
                </h4>
                <div className="content-flex content-items-center content-gap-2">
                  <Badge variant={isAssigned ? 'default' : 'secondary'}>
                    {isAssigned ? 'Assigned' : 'Unassigned'}
                  </Badge>
                </div>
              </div>
            </div>

            {/* Background Image */}
            <div>
              <h4 className="content-mb-2 content-text-sm content-font-semibold content-text-muted-foreground">
                Background Image
              </h4>
              {certificate.backgroundImageUrl ? (
                <div className="content-overflow-hidden content-rounded-lg content-border content-border-card-border content-bg-muted/50">
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
                <div className="content-flex content-items-center content-justify-center content-rounded-lg content-border content-border-card-border content-bg-muted/50 content-py-8 content-text-muted-foreground">
                  <ImageIcon className="content-mr-2 content-size-8" />
                  No Image Available
                </div>
              )}
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

export default ClientCertificateViewDetails;

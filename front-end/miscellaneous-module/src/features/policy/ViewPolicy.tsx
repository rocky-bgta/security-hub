import { safeWindowOpen, sanitizeHtml } from 'home-module/security';
import { FileText } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Label } from 'common/Label';
import { IPolicy, IPolicyDetails } from 'models/Policy';
import { formateDate, isSuccessResponse } from 'utils/Helper';
import { Status } from 'models/Global';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useEffect, useState } from 'react';

interface IProps {
  open: boolean;
  onClose: () => void;
  selectedPolicy: IPolicy;
}

const ViewPolicy = ({ open, onClose, selectedPolicy }: IProps) => {
  const apiClient = useAPI();
  const [policyDetails, setPolicyDetails] = useState<IPolicyDetails | null>(
    null,
  );
  const [loading, setLoading] = useState<boolean>(false);
  useEffect(() => {
    fetchPolicy();
  }, []);

  const fetchPolicy = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_POLICY_DETAILS.replace(
          ':id',
          selectedPolicy?.id ?? '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        setPolicyDetails(response.data);
      }
    } catch (error) {
      console.error('Error fetching policy details:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadFile = (fileUrl: string) => {
    safeWindowOpen(fileUrl, '_blank');
  };
  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-h-[80vh] max-w-4xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            Policy Details: {selectedPolicy?.policyName ?? ''}
          </DialogTitle>
          <DialogDescription>
            View detailed information about this policy
          </DialogDescription>
        </DialogHeader>
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-white">Loading...</div>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="font-semibold">Policy Name</Label>
                  <p className="text-sm text-muted-foreground">
                    {policyDetails?.policyName}
                  </p>
                </div>
                <div>
                  <Label className="font-semibold">Policy Type</Label>
                  <p className="text-sm text-muted-foreground">
                    {policyDetails?.policyTypeName}
                  </p>
                </div>
                <div>
                  <Label className="font-semibold">Effective Date</Label>
                  <p className="text-sm text-muted-foreground">
                    {formateDate(policyDetails?.effectiveDate ?? '')}
                  </p>
                </div>
                <div>
                  <Label className="font-semibold">End Date</Label>
                  <p className="text-sm text-muted-foreground">
                    {formateDate(policyDetails?.policyEndDate ?? '')}
                  </p>
                </div>
                <div>
                  <Label className="font-semibold">Status</Label>
                  <Badge
                    variant={
                      policyDetails?.status === Status.ACTIVE
                        ? 'default'
                        : 'secondary'
                    }
                  >
                    {policyDetails?.status}
                  </Badge>
                </div>
              </div>

              <div>
                <Label className="font-semibold">Policy Description</Label>
                <div
                  dangerouslySetInnerHTML={{
                    __html: sanitizeHtml(policyDetails?.description ?? ''),
                  }}
                  className="mt-1"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="font-semibold">Assigned Countries</Label>
                  <div className="mt-1 flex flex-wrap gap-1">
                    {policyDetails?.country?.name}
                  </div>
                </div>

                <div>
                  <Label className="font-semibold">Industry</Label>
                  <div className="mt-1 flex flex-wrap gap-1">
                    {policyDetails?.industry?.name}
                  </div>
                </div>
              </div>
            </div>

            {selectedPolicy?.files?.length > 0 && (
              <div>
                <Label className="font-semibold">Attachment</Label>
                <Button
                  variant="outline"
                  size="sm"
                  className="mt-1 flex items-center gap-2"
                  onClick={() =>
                    handleDownloadFile(selectedPolicy?.files?.[0]?.fileUrl)
                  }
                >
                  <FileText className="size-4" />
                  {selectedPolicy?.files?.[0]?.fileType}
                </Button>
              </div>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewPolicy;

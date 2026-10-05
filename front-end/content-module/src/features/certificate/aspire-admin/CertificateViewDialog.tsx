import { Award, Download } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  selectedCertificate: any;
  onDownload: (certificate: any) => void;
}

const CertificateViewDialog = ({
  isOpen,
  setIsOpen,
  selectedCertificate,
  onDownload,
}: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="content-max-w-2xl content-text-white">
        <DialogHeader>
          <DialogTitle className="content-flex content-items-center content-gap-2">
            <Award className="content-size-5" />
            Certificate Details
          </DialogTitle>
        </DialogHeader>
        {selectedCertificate && (
          <div className="content-space-y-4">
            <div className="content-grid content-grid-cols-2 content-gap-4">
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Learner Name
                </h4>
                <p className="content-font-medium">
                  {selectedCertificate.learnerName}
                </p>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Course Name
                </h4>
                <p className="content-font-medium">
                  {selectedCertificate.courseName}
                </p>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Exam Name
                </h4>
                <p className="content-font-medium">
                  {selectedCertificate.examName}
                </p>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Score
                </h4>
                <p className="content-font-medium">
                  {selectedCertificate.score}%
                </p>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Pass Status
                </h4>
                <Badge
                  variant={
                    selectedCertificate.passStatus === 'Pass'
                      ? 'default'
                      : 'destructive'
                  }
                >
                  {selectedCertificate.passStatus}
                </Badge>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Issue Date
                </h4>
                <p className="content-font-medium">
                  {selectedCertificate.issueDate}
                </p>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Expiry Date
                </h4>
                <p className="content-font-medium">
                  {selectedCertificate.expiryDate}
                </p>
              </div>
              <div>
                <h4 className="content-text-sm content-font-semibold content-text-muted-foreground">
                  Status
                </h4>
                <Badge
                  variant={
                    selectedCertificate.status === 'Active'
                      ? 'default'
                      : 'secondary'
                  }
                >
                  {selectedCertificate.status}
                </Badge>
              </div>
            </div>
            <div className="content-flex content-gap-2 content-pt-4">
              <Button onClick={() => onDownload(selectedCertificate)}>
                <Download className="content-mr-2 content-size-4" />
                Download Certificate
              </Button>
              <Button variant="outline" onClick={() => setIsOpen(false)}>
                Close
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default CertificateViewDialog;

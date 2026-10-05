import { safeWindowOpen, sanitizeHtml } from 'home-module/security';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from 'common/Dialog';
import { DialogHeader } from 'common/Dialog';
import { Label } from 'common/Label';
import PdfViewer from 'components/PdfViewer';
import { Download, FileText } from 'lucide-react';
import { IPolicyList } from 'models/Policy';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { buildFileUrl, cn, isPdfType } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  policy: IPolicyList;
}
const ViewPolicyContent = ({ isOpen, onClose, policy }: IProps) => {
  const fileUrl = policy.files[0].fileUrl;
  const fileType = policy.files[0].fileType;
  const isPdf = isPdfType(policy.policyTypeName) || isPdfType(fileType);

  const handleDownload = (url: string) => {
    safeWindowOpen(FILE_PATH_PREFIX + url, '_blank');
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent
        className={cn(
          'max-h-[90vh] w-full overflow-hidden rounded-2xl border border-slate-700/60 bg-slate-900 p-0 shadow-2xl shadow-black/40 ',
          isPdf ? 'max-w-5xl' : 'max-w-3xl',
        )}
      >
        <DialogHeader className="border-b border-slate-700/60 bg-slate-800/50 px-6 py-4">
          <DialogTitle className="flex flex-col gap-2">
            <span className="flex items-center justify-between gap-3">
              <span className="text-lg font-semibold text-white">
                View Policy
              </span>
              <span className="mr-4 flex flex-wrap items-center gap-2">
                <Badge
                  variant="outline"
                  className="rounded-full border-slate-600/60 bg-slate-800/40 px-2.5 py-0 text-xs text-slate-300"
                >
                  {policy.policyTypeName}
                </Badge>
                <Badge className="rounded-full bg-sky-500/15 px-2.5 py-0 text-xs font-medium text-sky-300">
                  {policy.files[0].fileType}
                </Badge>
              </span>
            </span>
            <DialogDescription className="text-xs text-slate-400">
              View detailed information and attachments for this policy.
            </DialogDescription>
          </DialogTitle>
        </DialogHeader>
        <div className="flex flex-col gap-6 overflow-y-auto px-6 py-5">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1.5">
              <Label className="text-xs font-semibold text-slate-300">
                Policy Name
              </Label>
              <p className="text-sm font-medium text-white">
                {policy.policyName}
              </p>
            </div>
            <div className="space-y-1.5">
              <Label className="text-xs font-semibold text-slate-300">
                Policy Type
              </Label>
              <p className="text-sm text-slate-300">{policy.policyTypeName}</p>
            </div>
          </div>

          <div className="space-y-2">
            <Label className="text-xs font-semibold text-slate-300">
              Description
            </Label>
            <div
              className="prose prose-invert prose-headings:text-white prose-a:text-sky-400 prose-a:no-underline hover:prose-a:underline max-w-none text-sm leading-relaxed text-slate-300"
              dangerouslySetInnerHTML={{
                __html: sanitizeHtml(policy.description ?? ''),
              }}
            />
          </div>

          {isPdf ? (
            <PdfViewer src={buildFileUrl(fileUrl)} title={policy.policyName} />
          ) : (
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-700/50 bg-slate-800/40 px-4 py-3">
              <div className="flex flex-col gap-1">
                <Label className="text-xs font-semibold text-slate-300">
                  Attachment
                </Label>
                <p className="text-xs text-slate-400">
                  {fileType || 'No attachment available'}
                </p>
              </div>
              <Button
                onClick={() => handleDownload(fileUrl)}
                variant="outline"
                size="sm"
                className="inline-flex items-center gap-2 rounded-lg border-slate-600/60 bg-slate-800/60 text-slate-200 hover:bg-slate-700/60 hover:text-white"
              >
                <FileText className="size-4" />
                <span className="text-xs font-medium">
                  Download ({policy.files[0].fileType})
                </span>
                <Download className="size-4" />
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ViewPolicyContent;

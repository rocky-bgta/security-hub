import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import {
  getProfileTypeLabel,
  ISenderProfileImportData,
} from 'models/SenderProfile';

type SenderProfileImportResultModalProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  result: ISenderProfileImportData | null;
};

const SenderProfileImportResultModal = ({
  open,
  onOpenChange,
  result,
}: SenderProfileImportResultModalProps) => {
  if (!result) {
    return null;
  }

  const {
    totalRows,
    successCount,
    failedCount,
    importedProfiles = [],
    errors = [],
  } = result;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="flex max-h-[90vh] max-w-3xl flex-col gap-0 overflow-hidden p-0 sm:rounded-lg">
        <div className="border-b border-card-border p-6 pb-4">
          <DialogHeader>
            <DialogTitle>Import results</DialogTitle>
            <DialogDescription>
              Summary of the bulk sender profile import. Review any row errors
              below and fix your file if needed.
            </DialogDescription>
          </DialogHeader>
        </div>

        <div className="space-y-6 overflow-y-auto p-6 pt-4">
          <dl className="grid grid-cols-3 gap-3 text-sm">
            <div className="rounded-md border border-card-border bg-muted/30 p-3">
              <dt className="text-muted-foreground">Total rows</dt>
              <dd className="text-lg font-semibold tabular-nums">
                {totalRows}
              </dd>
            </div>
            <div className="rounded-md border border-card-border bg-muted/30 p-3">
              <dt className="text-muted-foreground">Imported</dt>
              <dd className="text-lg font-semibold tabular-nums text-green-700 dark:text-green-400">
                {successCount}
              </dd>
            </div>
            <div className="rounded-md border border-card-border bg-muted/30 p-3">
              <dt className="text-muted-foreground">Failed</dt>
              <dd className="text-lg font-semibold tabular-nums text-destructive">
                {failedCount}
              </dd>
            </div>
          </dl>

          {errors.length > 0 && (
            <section className="space-y-2">
              <h3 className="text-sm font-medium text-foreground">
                Row errors
              </h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Row</TableHead>
                    <TableHead>Profile name</TableHead>
                    <TableHead>Message</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {errors.map((row, idx) => (
                    <TableRow
                      key={`${row.rowNumber}-${row.profileName}-${idx}`}
                    >
                      <TableCell className="tabular-nums">
                        {row.rowNumber}
                      </TableCell>
                      <TableCell>{row.profileName || '—'}</TableCell>
                      <TableCell>{row.message}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </section>
          )}

          {importedProfiles.length > 0 && (
            <section className="space-y-2">
              <h3 className="text-sm font-medium text-foreground">
                Imported profiles ({importedProfiles.length})
              </h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>From</TableHead>
                    <TableHead>Type</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {importedProfiles.map(p => (
                    <TableRow key={p.profileId}>
                      <TableCell>{p.profileName}</TableCell>
                      <TableCell>{p.fromAddress}</TableCell>
                      <TableCell>
                        {getProfileTypeLabel(p.profileType)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </section>
          )}
        </div>

        <DialogFooter className="border-t border-card-border p-4 sm:justify-end">
          <Button type="button" onClick={() => onOpenChange(false)}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default SenderProfileImportResultModal;

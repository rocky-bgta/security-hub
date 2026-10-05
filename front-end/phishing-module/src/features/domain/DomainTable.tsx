import { Button } from 'common/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { TableSkeleton } from 'components/LoadingSkeleton';
import {
  Lock,
  LockOpen,
  MoreHorizontal,
  ShieldCheck,
  Trash2,
} from 'lucide-react';
import { DomainStatus, IDomain } from 'models/Domain';
import { formatDate } from 'utils/Helper';
import DomainStatusBadge from './DomainStatusBadge';

interface DomainTableProps {
  domains: IDomain[];
  loading: boolean;
  onVerify: (domain: IDomain) => void;
  onLock: (domain: IDomain) => void;
  onUnlock: (domain: IDomain) => void;
  onDelete: (domain: IDomain) => void;
}

/**
 * Table component for displaying domain list
 */
const DomainTable = ({
  domains,
  loading,
  onVerify,
  onLock,
  onUnlock,
  onDelete,
}: DomainTableProps) => {
  if (loading) {
    return <TableSkeleton count={5} />;
  }

  if (domains?.length === 0) {
    return (
      <p className="text-center text-sm text-muted-foreground">
        No domains found.
      </p>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Domain</TableHead>
          <TableHead>Status</TableHead>
          <TableHead>Verified Date</TableHead>
          <TableHead>Locked Date</TableHead>
          <TableHead className="text-center">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {domains?.map(domain => (
          <TableRow key={domain.domainId}>
            <TableCell className="font-medium">{domain.domain}</TableCell>
            <TableCell>
              <DomainStatusBadge
                status={domain.status}
                onClick={
                  domain.status === DomainStatus.UNVERIFIED
                    ? () => onVerify(domain)
                    : undefined
                }
              />
            </TableCell>
            <TableCell>{formatDate(domain.verifiedAt)}</TableCell>
            <TableCell>{formatDate(domain.lockedAt)}</TableCell>
            <TableCell>
              <div className="flex justify-center">
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon">
                      <MoreHorizontal className="size-4" />
                      <span className="sr-only">Open menu</span>
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    {/* Verify action for unverified domains */}
                    {domain.status === DomainStatus.UNVERIFIED && (
                      <DropdownMenuItem onClick={() => onVerify(domain)}>
                        <ShieldCheck className="mr-2 size-4" />
                        Verify Domain
                      </DropdownMenuItem>
                    )}

                    {/* Lock action for verified (unlocked) domains */}
                    {domain.status === DomainStatus.VERIFIED &&
                      domain.canLock && (
                        <DropdownMenuItem onClick={() => onLock(domain)}>
                          <Lock className="mr-2 size-4" />
                          Lock Domain
                        </DropdownMenuItem>
                      )}

                    {/* Show upgrade message for non-eligible users */}
                    {domain.status === DomainStatus.VERIFIED &&
                      !domain.canLock && (
                        <DropdownMenuItem disabled>
                          <Lock className="mr-2 size-4 opacity-50" />
                          <span className="text-muted-foreground">
                            Upgrade to lock
                          </span>
                        </DropdownMenuItem>
                      )}

                    {/* Unlock action for locked domains */}
                    {domain.status === DomainStatus.VERIFIED_AND_LOCKED && (
                      <DropdownMenuItem onClick={() => onUnlock(domain)}>
                        <LockOpen className="mr-2 size-4" />
                        Unlock Domain
                      </DropdownMenuItem>
                    )}

                    {/* Delete action */}
                    <DropdownMenuItem
                      onClick={() => onDelete(domain)}
                      className="text-vibrant-red focus:text-vibrant-red"
                    >
                      <Trash2 className="mr-2 size-4" />
                      Delete Domain
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

export default DomainTable;

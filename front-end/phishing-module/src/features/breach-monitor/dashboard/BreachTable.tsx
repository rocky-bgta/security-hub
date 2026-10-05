import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { SeverityBadge } from 'components/common/Badge';
import { EmptyState } from 'components/common/EmptyState';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import { BreachAlert } from 'models/BreachMonitor';
import { formateDateAndTime } from 'utils/Helper';

interface Props {
  breaches: BreachAlert[];
  onViewDetail?: (breach: BreachAlert) => void;
  title: string;
  type: 'email' | 'ip';
}

const BreachTable = ({ breaches, title, type }: Props) => {
  if (breaches.length === 0) {
    return (
      <EmptyState
        title="No breaches detected"
        description="No breaches detected for the last 30 days."
      />
    );
  }

  const isEmail = type === 'email';

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              {isEmail && (
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                  Echoes
                </TableHead>
              )}
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Breach Date
              </TableHead>
              {isEmail && (
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                  Ingestion Date
                </TableHead>
              )}
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Domain
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                {isEmail ? 'Email' : 'IP Address'}
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Password/Hash
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Database Name
              </TableHead>
              {isEmail && (
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                  Found In
                </TableHead>
              )}
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Source
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Severity
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {breaches.map(b => (
              <TableRow key={b.id} className="hover:bg-muted/50">
                {isEmail && (
                  <TableCell className="p-3">
                    {b.echoes === 0 ? (
                      <span className="text-xs text-muted-foreground">
                        No echoes
                      </span>
                    ) : (
                      <span className="inline-flex items-center rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                        {b.echoes} {b.echoes === 1 ? 'Echo' : 'Echoes'}
                      </span>
                    )}
                  </TableCell>
                )}
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {formateDateAndTime(b.breachDate)}
                </TableCell>
                {isEmail && (
                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.ingestionDate
                      ? formateDateAndTime(b.ingestionDate)
                      : formateDateAndTime(b.detectedDate)}
                  </TableCell>
                )}
                <TableCell className="p-3 text-xs">{b.domain}</TableCell>
                <TableCell className="p-3 font-medium">
                  {isEmail ? b.email : b.ip}
                </TableCell>
                <TableCell className="p-3 font-mono text-xs">
                  {b.passwordHash}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {b.databaseName}
                </TableCell>
                {isEmail && (
                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.foundIn || '—'}
                  </TableCell>
                )}
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {b.source}
                </TableCell>
                <TableCell className="p-3">
                  <SeverityBadge severity={b.severity} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};

export default BreachTable;

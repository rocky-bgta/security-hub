import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { SeverityBadge, StatusBadge } from 'components/common/Badge';
import { EmptyState } from 'components/common/EmptyState';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import type { IPBreachAlert } from 'models/BreachMonitor';
import { formatDate } from 'utils/Helper';

interface Props {
  alerts: IPBreachAlert[];
  onViewDetail: (alert: IPBreachAlert) => void;
}

const IPBreachAlertsTable = ({ alerts }: Props) => {
  if (alerts.length === 0) {
    return (
      <EmptyState
        title="No breaches detected"
        description="No breaches detected for the last 30 days."
      />
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">IP Breach Alerts</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Fake Domain
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Type
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Date Detected
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Severity
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {alerts.map(a => (
              <TableRow key={a.id} className="hover:bg-muted/50">
                <TableCell className="p-3 font-medium">
                  {a.fakeDomain}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {a.type}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {formatDate(a.dateDetected)}
                </TableCell>
                <TableCell className="p-3">
                  {a.status === 'completed' ? (
                    <StatusBadge status="completed" />
                  ) : (
                    <SeverityBadge severity={a.severity} />
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};

export default IPBreachAlertsTable;

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
import type { ImpersonationAlert } from 'models/BreachMonitor';
import { formatDate } from 'utils/Helper';

interface Props {
  alerts: ImpersonationAlert[];
  onViewDetail: (alert: ImpersonationAlert) => void;
}

const ImpersonationAlertsTable = ({ alerts }: Props) => {
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
            {alerts.map(alert => (
              <TableRow key={alert.id} className="hover:bg-muted/50">
                <TableCell className="p-3 font-medium">
                  {alert.fakeDomain}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {alert.type}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {formatDate(alert.dateDetected)}
                </TableCell>
                <TableCell className="p-3">
                  {alert.status === 'resolved' ? (
                    <span className="inline-flex items-center rounded-full bg-[#22c55e]/10 px-2 py-0.5 text-xs font-semibold text-[#22c55e]">
                      Resolved
                    </span>
                  ) : (
                    <SeverityBadge severity={alert.severity} />
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

export default ImpersonationAlertsTable;

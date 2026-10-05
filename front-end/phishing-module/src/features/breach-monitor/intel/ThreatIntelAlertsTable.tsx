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
import type { ThreatIntelAlert } from 'models/BreachMonitor';
import { formatDate } from 'utils/Helper';

interface Props {
  alerts: ThreatIntelAlert[];
  onViewDetail: (alert: ThreatIntelAlert) => void;
}

const ThreatIntelAlertsTable = ({ alerts }: Props) => {
  if (alerts.length === 0) {
    return (
      <EmptyState
        title="No new alerts detected"
        description="No new alerts detected for the last 30 days."
      />
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Threat Intelligence Alerts</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Threat Domain
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Description
              </TableHead>
              <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider">
                Date Found
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
                  {a.threatDomain}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {a.description}
                </TableCell>
                <TableCell className="p-3 text-xs text-muted-foreground">
                  {formatDate(a.dateFound)}
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

export default ThreatIntelAlertsTable;

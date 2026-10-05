import { Search } from 'lucide-react';
import { useEffect, useState } from 'react';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { SeverityBadge } from 'components/common/Badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import { useAPI } from 'hooks/UseAPI';
import type { IBreachedEmailResponse } from 'models/BreachMonitor';
import { IList, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, objectToQueryString } from 'utils/Helper';

const EmailBreachTable = () => {
  const [breachedEmails, setBreachedEmails] = useState<
    IList<IBreachedEmailResponse>
  >({ items: [], total: 0, pageSize: 0, offset: 0 });

  const apiclient = useAPI();

  useEffect(() => {
    const fetchBreachedEmails = async () => {
      try {
        const response: IResponse<IList<IBreachedEmailResponse>> =
          await apiclient.get(
            API_END_POINTS.GET_BREACH_MONITOR_BREACHED_EMAIL_LIST +
              objectToQueryString({ offset: 0, pageSize: 5 }),
          );
        setBreachedEmails(response.data);
      } catch (error) {
        console.error('Error fetching breached emails:', error);
      }
    };

    fetchBreachedEmails();
  }, [apiclient]);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Recent Breach Alerts</CardTitle>
      </CardHeader>
      <CardContent>
        {breachedEmails.total === 0 && (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="mb-4 rounded-full bg-muted p-4">
              <Search className="size-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold">No breaches detected</h3>
            <p className="mt-1 max-w-sm text-sm text-muted-foreground">
              No breaches detected for the last 30 days.
            </p>
          </div>
        )}
        {breachedEmails.total > 0 && (
          <Table className="border-0">
            <TableHeader className="bg-transparent">
              <TableRow>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Echoes
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Breach Date
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Ingestion Date
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Domain
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Email
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Password/Hash
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Database Name
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Found In
                </TableHead>

                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Source
                </TableHead>
                <TableHead className="h-auto px-3 py-2 text-xs uppercase tracking-wider text-muted-foreground">
                  Severity
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {breachedEmails.items.map(b => (
                <TableRow key={b.id} className="hover:bg-muted/50">
                  <TableCell className="p-3">
                    {b.echoesCount === 0 ? (
                      <span className="text-xs text-muted-foreground">
                        No echoes
                      </span>
                    ) : (
                      <span className="inline-flex items-center rounded-full bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                        {b.echoesCount}{' '}
                        {b.echoesCount === 1 ? 'Echo' : 'Echoes'}
                      </span>
                    )}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {formateDateAndTime(b.firstSeenAt)}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.lastSeenAt
                      ? formateDateAndTime(b.lastSeenAt)
                      : formateDateAndTime(b.firstSeenAt)}
                  </TableCell>

                  <TableCell className="p-3 text-xs">{b.domain}</TableCell>
                  <TableCell className="p-3 font-medium">{b.email}</TableCell>
                  <TableCell className="p-3 font-mono text-xs">
                    {b.password ? (
                      b.password
                    ) : (
                      <span className="text-muted-foreground">N/A</span>
                    )}
                  </TableCell>
                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.databaseName || '—'}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.foundIn || '—'}
                  </TableCell>

                  <TableCell className="p-3 text-xs text-muted-foreground">
                    {b.source}
                  </TableCell>
                  <TableCell className="p-3">
                    <SeverityBadge severity={b.breachSeverity} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
};

export default EmailBreachTable;

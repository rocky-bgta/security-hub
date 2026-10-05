import { Badge } from 'common/Badge';
import { Status } from 'models/Global';

export const getStatusBadge = (status: string) => {
  switch (status) {
    case Status.PAID:
      return <Badge variant="default">Paid</Badge>;
    case Status.PENDING:
      return <Badge variant="secondary">Pending</Badge>;
    case Status.CANCELLED:
      return <Badge variant="destructive">Failed</Badge>;
    case Status.ON_PROGRESS:
      return (
        <Badge
          variant="outline"
          className="border border-orange-500 bg-orange-500 text-white"
        >
          On Progress
        </Badge>
      );
    case Status.SUCCESS:
      return <Badge variant="default">Success</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

import { Edit, Eye, Plus } from 'lucide-react';
import { IoFilter } from 'react-icons/io5';
import { Link } from 'react-router-dom';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';

type DomainStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED_SOON';

interface DemoDomain {
  sr_no: string;
  domain: string;
  usage: string;
  last_used: string;
  expire: string;
  status: DomainStatus;
}

const demoDomain: DemoDomain[] = [
  {
    sr_no: '01',
    domain: 'secure-banking.com',
    usage: 'Phishing Campaign',
    last_used: '2025-01-15',
    expire: '2025-12-15',
    status: 'ACTIVE',
  },
  {
    sr_no: '02',
    domain: 'Training-portal.net',
    usage: 'Phishing Campaign',
    last_used: '2025-01-10',
    expire: '2025-11-20',
    status: 'INACTIVE',
  },
  {
    sr_no: '03',
    domain: 'Security-update.org',
    usage: 'Phishing Campaign',
    last_used: '2025-01-08',
    expire: '2025-10-30',
    status: 'EXPIRED_SOON',
  },
];

const getStatusBadge = (status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED_SOON') => {
  switch (status) {
    case 'ACTIVE':
      return (
        <Badge className="home-flex home-items-center home-gap-2">
          <div className="home-size-2 home-rounded-full home-bg-primary"></div>
          Active
        </Badge>
      );

    case 'INACTIVE':
      return (
        <Badge
          variant="destructive"
          className="home-flex home-items-center home-gap-2"
        >
          <div className="home-size-2 home-rounded-full home-bg-vibrant-red"></div>
          In active
        </Badge>
      );

    default:
      return (
        <Badge
          variant="secondary"
          className="home-flex home-items-center home-gap-2"
        >
          <div className="home-size-2 home-rounded-full home-bg-yellow-400"></div>
          Expired Soon
        </Badge>
      );
  }
};

const DomainManagement = () => {
  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Domain Management
          </Link>
        </CardTitle>
        <div className="home-flex home-w-full home-flex-col home-gap-2 sm:home-flex-row lg:home-w-auto lg:home-items-center lg:home-gap-3">
          <Button variant="outline" className="home-w-full sm:home-w-auto">
            <IoFilter />
            Filter
          </Button>
          <Button className="home-w-full sm:home-w-auto">
            <Plus /> Add Domain
          </Button>
        </div>
      </CardHeader>

      <CardContent>
        <div className="home-w-full home-overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>SR.NO</TableHead>
                <TableHead>DOMAIN</TableHead>
                <TableHead>USAGE</TableHead>
                <TableHead>Last Used</TableHead>
                <TableHead>Expire</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {demoDomain.map(item => (
                <TableRow key={item.sr_no}>
                  <TableCell>{item.sr_no}</TableCell>
                  <TableCell>{item.domain}</TableCell>
                  <TableCell>{item.usage}</TableCell>
                  <TableCell>{item.last_used}</TableCell>
                  <TableCell>{item.expire}</TableCell>
                  <TableCell>{getStatusBadge(item.status)}</TableCell>
                  <TableCell>
                    <div className="home-flex home-items-center home-gap-6">
                      <Eye />
                      <Edit />
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
};

export default DomainManagement;

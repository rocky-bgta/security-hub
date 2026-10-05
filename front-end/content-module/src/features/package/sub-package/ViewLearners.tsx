import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Progress } from 'common/Progress';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import Pagination from 'common/Pagination';
import TableLoader from 'components/skeleton/TableLoader';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Users } from 'lucide-react';
import { IGetListParams, IList, IResponse, RiskGroup } from 'models/Global';
import { ISubPackageAssignedUser } from 'models/SubPackage';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { formatDateAndTime, humanizeText, objectToQueryString } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedSubPackage: { id: string; name: string };
}

const ViewLearners = ({ isOpen, onClose, selectedSubPackage }: IProps) => {
  const apiClient = useAPI();
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
  });
  const [assignedUsers, setAssignedUsers] = useState<
    IList<ISubPackageAssignedUser>
  >({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState(true);
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    if (isOpen) {
      setQueryParams({ ...InitGetListParams });
    }
  }, [isOpen, selectedSubPackage?.id]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce && isOpen && selectedSubPackage?.id) {
      fetchAssignedUsers();
    }
  }, [searchDebounce, isOpen, selectedSubPackage?.id]);

  const fetchAssignedUsers = async () => {
    setLoading(true);
    try {
      const response: IResponse<IList<ISubPackageAssignedUser>> =
        await apiClient.get(
          API_END_POINTS.SUB_PACKAGE_ASSIGNED_USERS.replace(
            ':subPackageId',
            selectedSubPackage.id,
          ) + queryString,
        );
      setAssignedUsers(response.data);
    } catch (error) {
      console.error('Error fetching assigned users:', error);
    } finally {
      setLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const getRiskGroupClassName = (riskGroup: RiskGroup) => {
    if (riskGroup === RiskGroup.HIGH_RISK) {
      return 'content-w-fit content-rounded content-bg-rose-600/10 content-px-2 content-py-1 content-font-medium content-text-rose-600';
    }
    if (riskGroup === RiskGroup.CRITICAL_RISK) {
      return 'content-w-fit content-rounded content-bg-red-600/10 content-px-2 content-py-1 content-font-medium content-text-red-600';
    }
    if (riskGroup === RiskGroup.MEDIUM_RISK) {
      return 'content-w-fit content-rounded content-bg-yellow-600/10 content-px-2 content-py-1 content-font-medium content-text-yellow-600';
    }
    return 'content-w-fit content-rounded content-bg-green-600/10 content-px-2 content-py-1 content-font-medium content-text-green-600';
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="!content-max-h-4/5 !content-w-4/5 !content-overflow-y-auto content-text-white">
        <DialogHeader>
          <DialogTitle className="content-flex content-items-center content-gap-2 content-text-white">
            <Users className="content-size-5" />
            Assigned Learners
          </DialogTitle>
          <DialogDescription>
            {assignedUsers.total} learner
            {assignedUsers.total !== 1 ? 's' : ''} assigned to{' '}
            {selectedSubPackage?.name}
          </DialogDescription>
        </DialogHeader>

        <div className="content-mt-4 content-space-y-4">
          {loading ? (
            <TableLoader />
          ) : (
            <Fragment>
              {assignedUsers.items.length === 0 ? (
                <div className="content-py-12 content-text-center content-text-gray-500">
                  <Users className="content-mx-auto content-mb-3 content-size-12 content-opacity-50" />
                  <p>No assigned learners found</p>
                </div>
              ) : (
                <div className="content-overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>SL</TableHead>
                        <TableHead>Full Name</TableHead>
                        <TableHead>Email</TableHead>
                        <TableHead>Department</TableHead>
                        <TableHead>Risk Group</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Assigned At</TableHead>
                        <TableHead>Expiry Date</TableHead>
                        <TableHead>Progress</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {assignedUsers.items.map((user, index) => (
                        <TableRow key={user.userId}>
                          <TableCell>
                            {assignedUsers.offset * assignedUsers.pageSize +
                              index +
                              1}
                          </TableCell>
                          <TableCell className="content-font-medium">
                            {user.fullName}
                          </TableCell>
                          <TableCell>{user.email}</TableCell>
                          <TableCell>
                            {humanizeText(user.department)}
                          </TableCell>
                          <TableCell>
                            <div
                              className={getRiskGroupClassName(user.riskGroup)}
                            >
                              {humanizeText(user.riskGroup)}
                            </div>
                          </TableCell>
                          <TableCell>
                            <Badge variant="secondary">
                              {humanizeText(user.status)}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {user.assignedAt
                              ? formatDateAndTime(user.assignedAt)
                              : 'N/A'}
                          </TableCell>
                          <TableCell>
                            {user.expiryDate
                              ? formatDateAndTime(user.expiryDate)
                              : 'N/A'}
                          </TableCell>
                          <TableCell>
                            <div className="content-flex content-items-center content-gap-2">
                              <Progress
                                value={user.progress}
                                className="content-w-16"
                              />
                              <span className="content-text-sm">
                                {user.progress}%
                              </span>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </Fragment>
          )}

          {assignedUsers.total > assignedUsers.pageSize && (
            <div className="content-flex content-justify-end content-pt-4">
              <Pagination
                total={assignedUsers.total}
                perPage={assignedUsers.pageSize}
                onPageChange={onPageChangeHandler}
              />
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ViewLearners;

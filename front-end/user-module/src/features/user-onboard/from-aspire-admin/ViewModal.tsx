import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Card, CardContent } from 'common/Card';
import { IUser } from 'models/User';
import { useEffect, useState } from 'react';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { Loader2 } from 'lucide-react';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedUserId: string;
}

const ViewModal = ({ isOpen, onClose, selectedUserId }: IProps) => {
  const [selectedUser, setSelectedUser] = useState<IUser>();
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    if (selectedUserId) {
      fetchUser();
    }
  }, [selectedUserId]);

  const fetchUser = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.VIEW_END_USER + selectedUserId,
      );
      if (isSuccessResponse(response.statusCode)) {
        setSelectedUser(response.data);
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>View User information</DialogTitle>
          {/* <DialogDescription>View user information</DialogDescription> */}
        </DialogHeader>{' '}
        {loading ? (
          <div className="flex h-40 items-center justify-center">
            <Loader2 className="size-8 animate-spin" />
          </div>
        ) : (
          <Card>
            <CardContent className="px-6 py-4">
              <div className="space-y-4 text-cloudy-white">
                <div className="flex">
                  <p className="w-40 text-ash-gray">Name</p>
                  <p className="mr-4">:</p>
                  <p>{selectedUser?.fullName}</p>
                </div>
                <div className="flex">
                  <p className="w-40 text-ash-gray">Email</p>
                  <p className="mr-4">:</p>
                  <p>{selectedUser?.email}</p>
                </div>
                <div className="flex">
                  <p className="w-40 text-ash-gray">Department</p>
                  <p className="mr-4">:</p>
                  <p>{selectedUser?.department}</p>
                </div>
                <div className="flex">
                  <p className="w-40 text-ash-gray">Phone Number</p>
                  <p className="mr-4">:</p>
                  <p>{selectedUser?.phoneNumber}</p>
                </div>
                <div className="flex">
                  <p className="w-40 text-ash-gray">Group</p>
                  <p className="mr-4">:</p>
                  <p>
                    {selectedUser?.riskGroup === 'HIGH_RISK'
                      ? 'High Risk'
                      : selectedUser?.riskGroup === 'MEDIUM_RISK'
                        ? 'Medium Risk'
                        : selectedUser?.riskGroup === 'LOW_RISK'
                          ? 'Low Risk'
                          : 'Critical Risk'}
                  </p>
                </div>
                <div className="flex">
                  <p className="w-40 text-ash-gray">Status</p>
                  <p className="mr-4">:</p>
                  <p
                    className={
                      selectedUser?.status === 'ACTIVE'
                        ? 'rounded bg-green-500/10 px-6 py-1 text-green-500'
                        : 'rounded bg-red-500/10 px-6 py-1 text-red-500'
                    }
                  >
                    {selectedUser?.status === 'ACTIVE' ? 'Active' : 'Inactive'}
                  </p>
                </div>
                {/* <div className="flex">
              <p className="w-40 text-ash-gray">Last Login</p>
              <p className="mr-4">:</p>
              <p>{HumanizeDate(selectedUser?.lastLogin)}</p>
            </div> */}
              </div>
            </CardContent>
          </Card>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewModal;

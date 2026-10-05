import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { ISystemUser } from 'models/User';
import { useEffect, useState } from 'react';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import Loader from 'common/loader/Loader';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedUserId: string;
}

const ViewModal = ({ isOpen, onClose, selectedUserId }: IProps) => {
  const [userDetails, setUserDetails] = useState<ISystemUser>();
  const [loading, setLoading] = useState<boolean>(true);
  const apiClient = useAPI();

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.GET_SYSTEM_USER.replace(':id', selectedUserId),
        );
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'Failed to fetch user details');
        }
        setUserDetails(response.data);
      } catch (error) {
        console.error(error);
        onClose();
      } finally {
        setLoading(false);
      }
    };

    if (selectedUserId) {
      fetchUser();
    }
  }, [apiClient, onClose, selectedUserId]);

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>View User information</DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="relative flex h-40 items-center justify-center">
            <Loader mode="container" />
          </div>
        ) : (
          <div className="space-y-4 text-cloudy-white">
            <div className="flex">
              <p className="w-40 text-ash-gray">Name</p>
              <p className="mr-4">:</p>
              <p>
                {userDetails?.firstName} {userDetails?.lastName}
              </p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Email</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.email}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Organization</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.companyName}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Designation</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.designation}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Department</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.department}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Risk Group</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.riskGroup}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Supervisor</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.supervisorName}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Role</p>
              <p className="mr-4">:</p>
              <p>{userDetails?.roles[0]}</p>
            </div>
            <div className="flex">
              <p className="w-40 text-ash-gray">Status</p>
              <p className="mr-4">:</p>
              <p
                className={
                  userDetails?.status === 'ACTIVE'
                    ? 'rounded bg-green-500/10 px-6 py-1 text-green-500'
                    : 'rounded bg-red-500/10 px-6 py-1 text-red-500'
                }
              >
                {userDetails?.status === 'ACTIVE' ? 'Active' : 'Inactive'}
              </p>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewModal;

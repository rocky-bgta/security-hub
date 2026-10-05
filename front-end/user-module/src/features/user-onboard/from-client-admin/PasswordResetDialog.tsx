import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { AlertCircle, KeyRound, Loader2, ShieldAlert } from 'lucide-react';
import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { Card, CardContent } from 'common/Card';
import { IUser } from 'models/User';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  selectedUserId: string;
  onSuccess?: () => void;
}

const PasswordResetDialog = ({
  isOpen,
  onClose,
  selectedUserId,
  onSuccess,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [userInfo, setUserInfo] = useState<IUser | null>(null);
  const [fetchingUser, setFetchingUser] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    if (isOpen && selectedUserId) {
      fetchUserInfo();
    } else {
      setUserInfo(null);
    }
  }, [isOpen, selectedUserId]);

  const fetchUserInfo = async () => {
    setFetchingUser(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.VIEW_END_USER + selectedUserId,
      );
      if (isSuccessResponse(response.statusCode)) {
        setUserInfo(response.data);
      }
    } catch (error) {
      console.error(error);
      toast.error('Failed to load user information');
    } finally {
      setFetchingUser(false);
    }
  };

  const handleResetPassword = async () => {
    if (!selectedUserId) return;

    setLoading(true);
    try {
      const response = await apiClient.post(
        API_END_POINTS.ADMIN_RESET_PASSWORD,
        {
          data: {
            userId: selectedUserId,
          },
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success(
          response.message ||
            'Password reset email has been sent successfully to the user.',
        );
        onSuccess?.();
        onClose();
      } else {
        toast.error(
          response.message || 'Failed to reset password. Please try again.',
        );
      }
    } catch (error: any) {
      console.error('Password reset error:', error);
      toast.error(
        error?.response?.data?.message ||
          'An error occurred while resetting the password. Please try again.',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <div className="flex items-center gap-3">
            <div className="flex size-10 items-center justify-center rounded-full bg-orange-500/10">
              <KeyRound className="size-5 text-orange-500" />
            </div>
            <div>
              <DialogTitle className="text-xl">Reset User Password</DialogTitle>
              <DialogDescription className="mt-1">
                Send a password reset email to the user
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {fetchingUser ? (
          <div className="flex h-40 items-center justify-center">
            <Loader2 className="size-8 animate-spin text-muted-foreground" />
          </div>
        ) : (
          <div className="space-y-4">
            {/* Security Warning */}
            <div className="rounded-lg border border-orange-500/20 bg-orange-500/5 p-4">
              <div className="flex gap-3">
                <ShieldAlert className="size-5 shrink-0 text-orange-500" />
                <div className="space-y-1">
                  <p className="text-sm font-medium text-orange-200">
                    Security Notice
                  </p>
                  <p className="text-xs text-muted-foreground">
                    This action will send a password reset email to the user.
                    They will be able to set a new password through the link
                    provided in the email. Make sure you have proper
                    authorization before proceeding.
                  </p>
                </div>
              </div>
            </div>

            {/* User Information */}
            {userInfo && (
              <Card>
                <CardContent className="px-4 py-3">
                  <p className="mb-3 text-sm font-medium text-muted-foreground">
                    User Information
                  </p>
                  <div className="space-y-2 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Name:</span>
                      <span className="font-medium text-white">
                        {userInfo.fullName}
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Email:</span>
                      <span className="font-medium text-white">
                        {userInfo.email}
                      </span>
                    </div>
                    {userInfo.department && (
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">
                          Department:
                        </span>
                        <span className="font-medium text-white">
                          {userInfo.department}
                        </span>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Confirmation Message */}
            <div className="flex gap-3 rounded-lg border border-blue-500/20 bg-blue-500/5 p-3">
              <AlertCircle className="size-5 shrink-0 text-blue-500" />
              <p className="text-sm text-muted-foreground">
                A password reset email will be sent to{' '}
                <span className="font-medium text-white">
                  {userInfo?.email || 'the user'}
                </span>
                . The user will need to click the link in the email to set a new
                password.
              </p>
            </div>
          </div>
        )}

        <DialogFooter className="gap-2 sm:gap-0">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={loading || fetchingUser}
          >
            Cancel
          </Button>
          <Button
            onClick={handleResetPassword}
            disabled={loading || fetchingUser || !userInfo}
            className="bg-orange-600 hover:bg-orange-700"
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 size-4 animate-spin" />
                Sending Reset Email...
              </>
            ) : (
              <>
                <KeyRound className="mr-2 size-4" />
                Send Reset Email
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default PasswordResetDialog;

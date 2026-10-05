import { AlertTriangle, Shield } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Alert, AlertDescription } from 'common/Alert';
import { Avatar, AvatarFallback } from 'common/Avatar';
import { Button } from 'common/Button';
import IconBackButton from 'components/IconBackButton';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { IResponse, Status } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';
import { IClientAdminDetails } from './Edit';

// const suspensionActions = [
//   {
//     id: 'disable_access',
//     label: 'Disable Client portal access',
//     description: 'Prevent Client from logging into their portal',
//     checked: false,
//   },
//   {
//     id: 'suspend_clients',
//     label: 'Suspend all client accounts',
//     description: 'Temporarily disable all client training access',
//     checked: false,
//   },
//   {
//     id: 'freeze_licenses',
//     label: 'Freeze license allocation',
//     description: 'Prevent new license assignments and renewals',
//     checked: false,
//   },
//   {
//     id: 'stop_billing',
//     label: 'Stop billing cycles',
//     description: 'Pause all recurring billing for this Client',
//     checked: false,
//   },
//   {
//     id: 'notify_clients',
//     label: 'Notify affected clients',
//     description: 'Send notification to all clients about service disruption',
//     checked: false,
//   },
// ];

interface IProps {
  hostPath: typeof routes;
}

export interface ISuspensionReason {
  id: string;
  name: string;
  description: string;
}

const ClientSuspend = ({ hostPath = routes }: IProps) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const clientId = searchParams.get('id');

  const [clientAdminData, setClientAdminData] =
    useState<Partial<IClientAdminDetails>>();
  const [suspensionReasons, setSuspensionReasons] = useState<
    Array<ISuspensionReason>
  >([]);
  const [suspensionReason, setSuspensionReason] = useState('');
  // const [suspensionNotes, setSuspensionNotes] = useState('');
  // const [selectedActions, setSelectedActions] = useState(
  //   suspensionActions.filter(action => action.checked).map(action => action.id),
  // );
  const [isLoading, setIsLoading] = useState(false);
  const [isSuspending, setIsSuspending] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
    fetchSuspensionReasons();
  }, [clientId]);

  if (!clientId) {
    navigate(hostPath.clientList.path);
  }

  const fetchData = async () => {
    try {
      const response: IResponse<IClientAdminDetails> = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_DETAILS.replace(':id', clientId as string),
      );

      setClientAdminData(response.data);
    } catch (error) {
      console.error('Error fetching client admin data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchSuspensionReasons = async () => {
    try {
      setIsLoading(true);
      const response: IResponse<Array<ISuspensionReason>> = await apiClient.get(
        API_END_POINTS.GET_SUSPENSION_REASONS,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setSuspensionReasons(response.data);
    } catch (error) {
      console.error('Error fetching suspension reasons data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // const handleActionChange = (actionId: string, checked: boolean) => {
  //   if (checked) {
  //     setSelectedActions([...selectedActions, actionId]);
  //   } else {
  //     setSelectedActions(selectedActions.filter(id => id !== actionId));
  //   }
  // };

  const handleSuspend = async () => {
    if (!suspensionReason) {
      toast.error('Please select a suspension reason');
      return;
    }
    try {
      setIsSuspending(true);
      const response = await apiClient.put(
        API_END_POINTS.SUSPEND_CLIENT_ADMIN.replace(':id', clientId as string),
        {
          data: {
            status: Status.SUSPEND,
            suspendReason: suspensionReason,
            // notes: suspensionNotes,
            // actions: selectedActions,
          },
        },
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      toast.success('Client Admin suspended successfully');
      navigate(hostPath.clientList.path);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'An error occurred');
      console.error('Error suspending Client Admin:', error);
    } finally {
      setIsSuspending(false);
    }
  };

  return (
    <div className="space-y-6">
      {isLoading ? (
        <p className="text-center text-muted-foreground">Loading...</p>
      ) : (
        <>
          <div className="space-y-3">
            <IconBackButton
              onClick={() => navigate(hostPath.clientList.path)}
              label="Back to Client List"
            />
            <div>
              <h2>Suspend Client Partner</h2>
              <p className="text-muted-foreground">
                Temporarily suspend Client partner access and services
              </p>
            </div>
          </div>

          <Alert className="border-red-500/50 bg-red-500/10">
            <AlertTriangle className="size-4 text-red-400" />
            <AlertDescription className="text-red-400">
              <strong>Warning:</strong> Suspending this Client will affect all
              their clients and ongoing training programs. This action should be
              taken with careful consideration.
            </AlertDescription>
          </Alert>

          <div className="grid gap-6 lg:grid-cols-3">
            <div className="space-y-6 lg:col-span-3">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-foreground">
                    <Shield className="size-5" />
                    Client Information
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="mb-6 flex items-center gap-4">
                    <Avatar className="size-16">
                      <AvatarFallback className="bg-primary text-lg text-primary-foreground">
                        {clientAdminData?.organizationName
                          ?.split(' ')
                          .map(n => n[0])
                          .join('')}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <h3 className="text-xl font-semibold text-foreground">
                        {clientAdminData?.organizationName}
                      </h3>
                      <p className="text-muted-foreground">
                        {clientAdminData?.contactEmail}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {clientAdminData?.email} •{' '}
                        {clientAdminData?.phoneNumber}
                      </p>
                    </div>
                  </div>

                  {/* <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Users className="mx-auto mb-2 h-6 w-6 text-blue-400" />
                  <p className="text-lg font-bold text-foreground">
                    {clientAdminData.clients}
                  </p>
                  <p className="text-xs text-muted-foreground">Clients</p>
                </div>
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Package className="mx-auto mb-2 h-6 w-6 text-green-400" />
                  <p className="text-lg font-bold text-foreground">
                    {clientAdminData.usedLicenses}
                  </p>
                  <p className="text-xs text-muted-foreground">Used Licenses</p>
                </div>
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Calendar className="mx-auto mb-2 h-6 w-6 text-purple-400" />
                  <p className="text-lg font-bold text-foreground">
                    {clientAdminData.revenue}
                  </p>
                  <p className="text-xs text-muted-foreground">Revenue</p>
                </div>
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Shield className="mx-auto mb-2 h-6 w-6 text-orange-400" />
                  <p className="text-lg font-bold text-foreground">
                    {clientAdminData.status}
                  </p>
                  <p className="text-xs text-muted-foreground">Status</p>
                </div>
              </div> */}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-foreground">
                    Suspension Details
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="reason" className="text-foreground">
                      Reason for Suspension *
                    </Label>
                    <Select onValueChange={setSuspensionReason}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select suspension reason" />
                      </SelectTrigger>
                      <SelectContent className="border-card-border bg-secondary">
                        {suspensionReasons.map(reason => (
                          <SelectItem key={reason.id} value={reason.id}>
                            {reason.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {/* <div className="space-y-2">
                    <Label htmlFor="notes" className="text-foreground">
                      Additional Notes
                    </Label>
                    <Textarea
                      id="notes"
                      value={suspensionNotes}
                      onChange={e => setSuspensionNotes(e.target.value)}
                      rows={4}
                      placeholder="Provide additional details about the suspension..."
                    />
                  </div> */}
                </CardContent>
              </Card>

              {/* <Card className="custom-disable-section">
                <CardHeader>
                  <CardTitle className="text-foreground">
                    Suspension Actions (Coming Soon)
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {suspensionActions.map(action => (
                      <div
                        key={action.id}
                        className="flex items-start space-x-3"
                      >
                        <Checkbox
                          id={action.id}
                          checked={selectedActions.includes(action.id)}
                          onCheckedChange={checked =>
                            handleActionChange(action.id, checked as boolean)
                          }
                          className="mt-1"
                        />
                        <div className="flex-1">
                          <Label
                            htmlFor={action.id}
                            className="cursor-pointer font-medium text-foreground"
                          >
                            {action.label}
                          </Label>
                          <p className="text-sm text-muted-foreground">
                            {action.description}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card> */}
            </div>

            <div className="space-y-6 lg:col-span-3">
              {/* <Card className="custom-disable-section">
                <CardHeader>
                  <CardTitle className="text-foreground">
                    Impact Summary (Coming Soon)
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="rounded-lg border border-red-500/20 bg-red-500/10 p-3">
                    <h4 className="mb-2 font-medium text-red-400">
                      Affected Services
                    </h4>
                    <ul className="space-y-1 text-sm text-muted-foreground">
                      <li>• 100 client organizations</li>
                      <li>• 50 active licenses</li>
                      <li>• Ongoing training programs</li>
                      <li>• Monthly revenue of $10,000</li>
                    </ul>
                  </div>

                  <div className="rounded-lg border border-orange-500/20 bg-orange-500/10 p-3">
                    <h4 className="mb-2 font-medium text-orange-400">
                      Recommended Actions
                    </h4>
                    <ul className="space-y-1 text-sm text-muted-foreground">
                      <li>• Contact Client before suspension</li>
                      <li>• Prepare client communication</li>
                      <li>• Document suspension reasons</li>
                      <li>• Set review date for reinstatement</li>
                    </ul>
                  </div>
                </CardContent>
              </Card>

              <Card className="custom-disable-section">
                <CardHeader>
                  <CardTitle className="text-foreground">
                    Suspension Timeline (Coming Soon)
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="text-sm">
                    <p className="text-muted-foreground">Immediate Effects:</p>
                    <p className="text-foreground">Portal access disabled</p>
                  </div>
                  <div className="text-sm">
                    <p className="text-muted-foreground">Within 24 hours:</p>
                    <p className="text-foreground">Client notifications sent</p>
                  </div>
                  <div className="text-sm">
                    <p className="text-muted-foreground">Review Period:</p>
                    <p className="text-foreground">30 days for appeal</p>
                  </div>
                  <div className="text-sm">
                    <p className="text-muted-foreground">Auto-termination:</p>
                    <p className="text-foreground">90 days if unresolved</p>
                  </div>
                </CardContent>
              </Card> */}

              <div className="flex justify-between gap-4">
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => navigate(hostPath.clientList.path)}
                >
                  Cancel
                </Button>
                <Button
                  onClick={handleSuspend}
                  variant="destructive"
                  // disabled={!suspensionReason || isSuspending}
                  className="w-full"
                >
                  {isSuspending ? 'Suspending...' : 'Suspend Client Partner'}
                </Button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default ClientSuspend;

import { AlertTriangle, Shield } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Alert, AlertDescription } from 'common/Alert';
import { Avatar, AvatarFallback, AvatarImage } from 'common/Avatar';
import { Button } from 'common/Button';
import IconBackButton from 'components/IconBackButton';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { IList, IResponse, Status } from 'models/Global';
import { IMSPList } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';

const suspensionActions = [
  {
    id: 'disable_access',
    label: 'Disable MSP portal access',
    description: 'Prevent MSP from logging into their portal',
    checked: false,
  },
  {
    id: 'suspend_clients',
    label: 'Suspend all client accounts',
    description: 'Temporarily disable all client training access',
    checked: false,
  },
  {
    id: 'freeze_licenses',
    label: 'Freeze license allocation',
    description: 'Prevent new license assignments and renewals',
    checked: false,
  },
  {
    id: 'stop_billing',
    label: 'Stop billing cycles',
    description: 'Pause all recurring billing for this MSP',
    checked: false,
  },
  {
    id: 'notify_clients',
    label: 'Notify affected clients',
    description: 'Send notification to all clients about service disruption',
    checked: false,
  },
];

interface IProps {
  hostPath: typeof routes;
}

interface ISuspensionReason {
  id: string;
  name: string;
  description: string;
}

const MSPSuspend = ({ hostPath = routes }: IProps) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const mspId = searchParams.get('id');

  const [selectedMSP, setSelectedMSP] = useState<IMSPList>();
  const [suspensionReasons, setSuspensionReasons] = useState<
    Array<ISuspensionReason>
  >([]);
  const [suspensionReason, setSuspensionReason] =
    useState<string>('Dummy Reason');
  const [suspensionNotes, setSuspensionNotes] = useState<string>('');
  const [selectedActions, setSelectedActions] = useState<string[]>(
    suspensionActions.filter(action => action.checked).map(action => action.id),
  );
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isSuspending, setIsSuspending] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchMSP();
    fetchSuspensionReasons();
  }, [mspId]);

  if (!mspId) {
    navigate(hostPath.mspList.path);
  }

  const fetchMSP = async () => {
    try {
      setIsLoading(true);
      const response: IResponse<IList<IMSPList>> = await apiClient.get(
        API_END_POINTS.MSP_LIST + 'offset=0&pageSize=100',
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setSelectedMSP(response.data.items.find(msp => msp.id === mspId));
    } catch (error) {
      console.error('Error fetching MSP data:', error);
      navigate(hostPath.mspList.path);
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

  const handleActionChange = (actionId: string, checked: boolean) => {
    if (checked) {
      setSelectedActions([...selectedActions, actionId]);
    } else {
      setSelectedActions(selectedActions.filter(id => id !== actionId));
    }
  };

  const handleSuspend = async () => {
    try {
      setIsSuspending(true);
      const response = await apiClient.put(
        API_END_POINTS.SUSPEND_MSP.replace(':id', mspId as string),
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
      toast.success('MSP suspended successfully');
      navigate(hostPath.mspList.path);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'An error occurred');
      console.error('Error suspending MSP:', error);
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
              onClick={() => navigate(hostPath.mspList.path)}
              label="Back to MSP List"
            />
            <div>
              <h1 className="text-3xl font-bold text-foreground">
                Suspend MSP Partner
              </h1>
              <p className="text-muted-foreground">
                Temporarily suspend MSP partner access and services
              </p>
            </div>
          </div>

          <Alert className="border-red-500/50 bg-red-500/10">
            <AlertTriangle className="size-4 text-red-400" />
            <AlertDescription className="text-red-400">
              <strong>Warning:</strong> Suspending this MSP will affect all
              their clients and ongoing training programs. This action should be
              taken with careful consideration.
            </AlertDescription>
          </Alert>

          <div className="grid gap-6 lg:grid-cols-3">
            <div className="space-y-6 lg:col-span-2">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-foreground">
                    <Shield className="size-5" />
                    MSP Information
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="mb-6 flex items-center gap-4">
                    <Avatar className="size-16">
                      <AvatarImage src="/placeholder.svg?height=64&width=64" />
                      <AvatarFallback className="bg-primary text-lg text-primary-foreground">
                        {selectedMSP?.organizationName
                          .split(' ')
                          .map(n => n[0])
                          .join('')}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <h3 className="text-xl font-semibold text-foreground">
                        {selectedMSP?.organizationName}
                      </h3>
                      <p className="text-muted-foreground">
                        {selectedMSP?.contactEmail}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {selectedMSP?.contactEmail} • {selectedMSP?.phoneNumber}
                      </p>
                    </div>
                  </div>

                  {/* <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Users className="mx-auto mb-2 h-6 w-6 text-blue-400" />
                  <p className="text-lg font-bold text-foreground">
                    {selectedMSP.clients}
                  </p>
                  <p className="text-xs text-muted-foreground">Clients</p>
                </div>
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Package className="mx-auto mb-2 h-6 w-6 text-green-400" />
                  <p className="text-lg font-bold text-foreground">
                    {selectedMSP.usedLicenses}
                  </p>
                  <p className="text-xs text-muted-foreground">Used Licenses</p>
                </div>
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Calendar className="mx-auto mb-2 h-6 w-6 text-purple-400" />
                  <p className="text-lg font-bold text-foreground">
                    {selectedMSP.revenue}
                  </p>
                  <p className="text-xs text-muted-foreground">Revenue</p>
                </div>
                <div className="rounded-lg border border-card-border p-3 text-center">
                  <Shield className="mx-auto mb-2 h-6 w-6 text-orange-400" />
                  <p className="text-lg font-bold text-foreground">
                    {selectedMSP.status}
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
                    <Select
                      value={suspensionReason}
                      onValueChange={setSuspensionReason}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select suspension reason" />
                      </SelectTrigger>
                      <SelectContent className="border-card-border bg-secondary">
                        {suspensionReasons.map(reason => (
                          <SelectItem key={reason.id} value={reason.name}>
                            {reason.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
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
                  </div>
                </CardContent>
              </Card>

              <Card className="custom-disable-section">
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
              </Card>
            </div>

            <div className="space-y-6">
              <Card className="custom-disable-section">
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
                      <li>• 500 active licenses</li>
                      <li>• Ongoing training programs</li>
                      <li>• Monthly revenue of $1,000,000</li>
                    </ul>
                  </div>

                  <div className="rounded-lg border border-orange-500/20 bg-orange-500/10 p-3">
                    <h4 className="mb-2 font-medium text-orange-400">
                      Recommended Actions
                    </h4>
                    <ul className="space-y-1 text-sm text-muted-foreground">
                      <li>• Contact MSP before suspension</li>
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
              </Card>

              <div className="space-y-3">
                <Button
                  onClick={handleSuspend}
                  // disabled={!suspensionReason || isSuspending}
                  variant="destructive"
                  className="w-full"
                >
                  {isSuspending ? 'Suspending...' : 'Suspend MSP Partner'}
                </Button>
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => navigate(hostPath.mspList.path)}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default MSPSuspend;

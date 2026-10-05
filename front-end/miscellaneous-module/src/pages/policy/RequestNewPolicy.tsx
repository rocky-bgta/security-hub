import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import { CountryList } from 'components/CustomCountryStateSelect';
import SearchSelect from 'components/SearchSelect';
import { routes } from 'routes/Routes';

const PolicyTypes = [
  { value: 'Security', label: 'Security' },
  { value: 'Compliance', label: 'Compliance' },
  { value: 'IT', label: 'IT' },
  { value: 'HR', label: 'HR' },
  { value: 'Finance', label: 'Finance' },
];

const MockDepartments = ['All Departments', 'IT', 'HR', 'Finance', 'Legal'];
const MockMSPs = ['All MSPs', 'TechCorp MSP', 'ComplianceCorp MSP'];
const MockClients = ['All Clients', 'Client A', 'Client B', 'Client C'];

const RequestNewPolicy = ({ hostPath = routes }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const requestType = location.state ? 'edit' : 'add';

  const [requestForm, setRequestForm] = useState<any>(
    location.state ?? {
      policyName: '',
      policyType: '',
      description: '',
      status: 'Pending',
      effectiveDate: '',
      endDate: '',
      assignAllUsers: false,
      assignedUsers: [],
      assignedDepartments: [],
      assignedCountries: [],
      assignedMSPs: [],
      assignedClients: [],
      products: [],
      attachment: null,
    },
  );

  const handleSubmitRequest = () => {};

  const handleCancelRequest = () => navigate(hostPath.policyList.path);

  return (
    <div className="space-y-4 px-4">
      <div>
        <h2>
          {requestType === 'add' && 'Request New Policy'}
          {requestType === 'edit' && 'Request Policy Update'}
        </h2>
        <p className="mt-2 text-muted-foreground">
          {requestType === 'add' &&
            'Submit a request to create a new policy. This will be sent to Super Admin for approval.'}
          {requestType === 'edit' &&
            'Submit a request to update this policy. This will be sent to Super Admin for approval.'}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="policy-name">Policy Name *</Label>
          <Input
            id="policy-name"
            value={requestForm.policyName}
            onChange={e =>
              setRequestForm({
                ...requestForm,
                policyName: e.target.value,
              })
            }
            placeholder="Enter policy name"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="policy-type">Policy Type *</Label>
          <SearchSelect
            value={requestForm.policyType}
            onValueChange={value =>
              setRequestForm({ ...requestForm, policyType: value })
            }
            placeholder="Select policy type"
            items={PolicyTypes}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="effective-date">Effective Date *</Label>
          <Input
            id="effective-date"
            type="date"
            value={requestForm.effectiveDate}
            onChange={e =>
              setRequestForm({
                ...requestForm,
                effectiveDate: e.target.value,
              })
            }
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="end-date">End Date</Label>
          <Input
            id="end-date"
            type="date"
            value={requestForm.endDate}
            onChange={e =>
              setRequestForm({
                ...requestForm,
                endDate: e.target.value,
              })
            }
          />
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Policy Description *</Label>
        <Textarea
          id="description"
          value={requestForm.description}
          onChange={e =>
            setRequestForm({
              ...requestForm,
              description: e.target.value,
            })
          }
          placeholder="Enter detailed policy description"
          rows={3}
        />
      </div>

      <div className="space-y-2">
        <Label>Policy Assignment</Label>
        <div className="space-y-3">
          <div className="flex items-center space-x-2">
            <Checkbox
              id="assign-all-users"
              checked={requestForm.assignAllUsers}
              onCheckedChange={checked =>
                setRequestForm({
                  ...requestForm,
                  assignAllUsers: !!checked,
                })
              }
            />
            <Label htmlFor="assign-all-users">Assign to All Users</Label>
          </div>

          {!requestForm.assignAllUsers && (
            <div className="space-y-2">
              <Label>Select Departments</Label>
              <div className="mt-1 space-y-1">
                {MockDepartments.map(dept => (
                  <div key={dept} className="flex items-center space-x-2">
                    <Checkbox
                      id={`dept-${dept}`}
                      checked={requestForm.assignedDepartments.includes(dept)}
                      onCheckedChange={checked => {
                        if (dept === 'All Departments') {
                          setRequestForm({
                            ...requestForm,
                            assignedDepartments: checked
                              ? MockDepartments.slice(1)
                              : [],
                          });
                          return;
                        }

                        if (checked) {
                          setRequestForm({
                            ...requestForm,
                            assignedDepartments: [
                              ...requestForm.assignedDepartments,
                              dept,
                            ],
                          });
                        } else {
                          setRequestForm({
                            ...requestForm,
                            assignedDepartments:
                              requestForm.assignedDepartments.filter(
                                (d: any) => d !== dept,
                              ),
                          });
                        }
                      }}
                    />
                    <Label htmlFor={`dept-${dept}`} className="text-sm">
                      {dept}
                    </Label>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <div className="space-y-2">
          <Label>Countries</Label>
          <div className="mt-1 max-h-40 space-y-1 overflow-y-auto">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="country-all"
                checked={
                  requestForm.assignedCountries.length === CountryList.length
                }
                onCheckedChange={checked => {
                  setRequestForm({
                    ...requestForm,
                    assignedCountries: checked
                      ? CountryList.map(country => country.code)
                      : [],
                  });
                }}
              />
              <Label htmlFor="country-all" className="text-sm">
                All Countries
              </Label>
            </div>
            {CountryList.map(country => (
              <div key={country.code} className="flex items-center space-x-2">
                <Checkbox
                  id={`country-${country.code}`}
                  checked={requestForm.assignedCountries.includes(country.code)}
                  onCheckedChange={checked => {
                    if (checked) {
                      setRequestForm({
                        ...requestForm,
                        assignedCountries: [
                          ...requestForm.assignedCountries,
                          country.code,
                        ],
                      });
                    } else {
                      setRequestForm({
                        ...requestForm,
                        assignedCountries: requestForm.assignedCountries.filter(
                          (c: any) => c !== country.code,
                        ),
                      });
                    }
                  }}
                />
                <Label htmlFor={`country-${country.code}`} className="text-sm">
                  {country.name}
                </Label>
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-2">
          <Label>MSPs</Label>
          <div className="mt-1 space-y-1">
            {MockMSPs.map(msp => (
              <div key={msp} className="flex items-center space-x-2">
                <Checkbox
                  id={`msp-${msp}`}
                  checked={requestForm.assignedMSPs.includes(msp)}
                  onCheckedChange={checked => {
                    if (msp === 'All MSPs') {
                      setRequestForm({
                        ...requestForm,
                        assignedMSPs: checked ? MockMSPs.slice(1) : [],
                      });
                      return;
                    }

                    if (checked) {
                      setRequestForm({
                        ...requestForm,
                        assignedMSPs: [...requestForm.assignedMSPs, msp],
                      });
                    } else {
                      setRequestForm({
                        ...requestForm,
                        assignedMSPs: requestForm.assignedMSPs.filter(
                          (m: any) => m !== msp,
                        ),
                      });
                    }
                  }}
                />
                <Label htmlFor={`msp-${msp}`} className="text-sm">
                  {msp}
                </Label>
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-2">
          <Label>Clients</Label>
          <div className="mt-1 space-y-1">
            {MockClients.map(client => (
              <div key={client} className="flex items-center space-x-2">
                <Checkbox
                  id={`client-${client}`}
                  checked={requestForm.assignedClients.includes(client)}
                  onCheckedChange={checked => {
                    if (client === 'All Clients') {
                      setRequestForm({
                        ...requestForm,
                        assignedClients: checked ? MockClients.slice(1) : [],
                      });
                      return;
                    }

                    if (checked) {
                      setRequestForm({
                        ...requestForm,
                        assignedClients: [
                          ...requestForm.assignedClients,
                          client,
                        ],
                      });
                    } else {
                      setRequestForm({
                        ...requestForm,
                        assignedClients: requestForm.assignedClients.filter(
                          (c: any) => c !== client,
                        ),
                      });
                    }
                  }}
                />
                <Label htmlFor={`client-${client}`} className="text-sm">
                  {client}
                </Label>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="attachment">Policy Attachment</Label>
        <Input
          id="attachment"
          type="file"
          accept=".pdf,.doc,.docx,.jpg,.png,.jpeg"
          onChange={e =>
            setRequestForm({
              ...requestForm,
              attachment: e.target.files?.[0] || null,
            })
          }
        />
        <p className="mt-1 text-xs text-muted-foreground">
          Supported formats: PDF, Word Document, Images (JPG, PNG)
        </p>
      </div>

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={handleCancelRequest}>
          Cancel
        </Button>
        <Button onClick={handleSubmitRequest}>Submit Request</Button>
      </div>
    </div>
  );
};

export default RequestNewPolicy;

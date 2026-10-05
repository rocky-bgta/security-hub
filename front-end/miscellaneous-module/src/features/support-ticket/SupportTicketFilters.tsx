import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Input } from 'components/common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import SearchSelect from 'components/SearchSelect';
import { IDropdownOption } from 'models/DropdownData';
import {
  SupportTicketAssignCategory,
  SupportTicketPriority,
  SupportTicketStatus,
} from 'models/SupportTicket';

export type SupportTicketView = 'all' | 'myPending' | 'user' | 'resolved';

export type SupportTicketFilterField =
  | 'search'
  | 'supportType'
  | 'priority'
  | 'status'
  | 'clientId'
  | 'assignCategory'
  | 'ticketView';

export interface ISupportTicketFilterValues {
  search: string;
  supportType?: string;
  priority?: string;
  status?: string;
  clientId?: string;
  assignCategory?: string;
  ticketView?: SupportTicketView;
}

interface IProps {
  description: string;
  searchPlaceholder?: string;
  values: ISupportTicketFilterValues;
  onChange: (field: SupportTicketFilterField, value: string) => void;
  onReset: () => void;
  supportTypeList?: Array<IDropdownOption>;
  clients?: Array<{ id: string; organizationName: string }>;
  showSupportType?: boolean;
  showPriority?: boolean;
  showStatus?: boolean;
  showClient?: boolean;
  showAssignTo?: boolean;
  showTicketView?: boolean;
  ticketViewOptions?: Array<{ value: string; label: string }>;
  disablePriority?: boolean;
  disableStatus?: boolean;
}

const SupportTicketFilters = ({
  description,
  searchPlaceholder = 'Search tickets...',
  values,
  onChange,
  onReset,
  supportTypeList = [],
  clients = [],
  showSupportType = false,
  showPriority = true,
  showStatus = false,
  showClient = false,
  showAssignTo = false,
  showTicketView = false,
  ticketViewOptions = [
    { value: 'all', label: 'All' },
    { value: 'myPending', label: 'My Pending' },
    { value: 'user', label: 'User Tickets' },
    { value: 'resolved', label: 'Resolved' },
  ],
  disablePriority = false,
  disableStatus = false,
}: IProps) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Filters</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex gap-4">
          <div className="flex-1">
            <Input
              value={values.search}
              onChange={e => onChange('search', e.target.value)}
              placeholder={searchPlaceholder}
              className="w-full"
            />
          </div>
          {showTicketView && (
            <div className="w-64">
              <Select
                value={values.ticketView || 'all'}
                onValueChange={value => onChange('ticketView', value)}
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Filter by View" />
                </SelectTrigger>
                <SelectContent>
                  {ticketViewOptions.map(option => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
          {showClient && (
            <div className="w-64">
              <SearchSelect
                value={values.clientId || ''}
                onValueChange={(value: string) =>
                  onChange('clientId', value === 'all' ? '' : value)
                }
                items={clients.map(client => ({
                  value: client.id,
                  label: client.organizationName,
                }))}
                placeholder="Filter by Client"
              />
            </div>
          )}
          {showAssignTo && (
            <div className="w-64">
              <Select
                value={values.assignCategory}
                onValueChange={value => onChange('assignCategory', value)}
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Filter by Assign Category" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    value={SupportTicketAssignCategory.ASSIGN_TO_SUPER_ADMIN}
                  >
                    Super Admin
                  </SelectItem>
                  <SelectItem value={SupportTicketAssignCategory.ASSIGN_TO_MSP}>
                    MSP
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
          {showSupportType && (
            <div className="w-64">
              <Select
                value={values.supportType}
                onValueChange={value =>
                  onChange('supportType', value === 'all' ? '' : value)
                }
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Filter by Support Type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Support Types</SelectItem>
                  {supportTypeList.length > 0 ? (
                    supportTypeList.map(type => (
                      <SelectItem key={type.id} value={type.id}>
                        {type.name}
                      </SelectItem>
                    ))
                  ) : (
                    <SelectItem disabled value="No support types found">
                      No support types found
                    </SelectItem>
                  )}
                </SelectContent>
              </Select>
            </div>
          )}
          {showPriority && (
            <div className="w-64">
              <Select
                value={values.priority}
                disabled={disablePriority}
                onValueChange={value =>
                  onChange('priority', value === 'all' ? '' : value)
                }
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Filter by Priority" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Priority</SelectItem>
                  <SelectItem value={SupportTicketPriority.HIGH}>
                    High
                  </SelectItem>
                  <SelectItem value={SupportTicketPriority.MEDIUM}>
                    Medium
                  </SelectItem>
                  <SelectItem value={SupportTicketPriority.LOW}>Low</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
          {showStatus && (
            <div className="w-64">
              <Select
                value={values.status}
                disabled={disableStatus}
                onValueChange={value =>
                  onChange('status', value === 'all' ? '' : value)
                }
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Filter by Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value={SupportTicketStatus.OPEN}>Open</SelectItem>
                  <SelectItem value={SupportTicketStatus.CLOSED}>
                    Closed
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
          <Button variant="outline" size="sm" onClick={onReset}>
            Reset Filters
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default SupportTicketFilters;

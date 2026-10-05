import { Edit, Plus, Search, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import ActionRole from 'features/role/ActionRole';
import DeleteRole from 'features/role/DeleteRole';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IResponse, OrderType } from 'models/Global';
import { IRole } from 'models/Role';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';

const getColorTheme = (color: string) => {
  const themes = {
    blue: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
    purple: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
    green: 'bg-green-500/20 text-green-400 border-green-500/30',
    orange: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
    red: 'bg-red-500/20 text-red-400 border-red-500/30',
    gray: 'bg-gray-500/20 text-gray-400 border-gray-500/30',
  };

  return themes[color as keyof typeof themes] || themes.gray;
};

const Roles = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<IRole>>([]);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    search: '',
    sortBy: 'accessLevel',
    order: OrderType.DESC,
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const apiClient = useAPI();

  const [selectedRole, setSelectedRole] = useState<IRole | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchData();
    }
  }, [searchDebounce]);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IRole>> = await apiClient.get(
        API_END_POINTS.GET_ROLE_LIST + queryString,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching role data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitRole = (role: IRole) => {
    let newData = [...data];
    if (actionType === 'create') {
      newData = [...newData, role];
    } else {
      newData = [...newData.map(r => (r.id === role.id ? role : r))];
    }
    newData.sort((a, b) => b.accessLevel - a.accessLevel);

    setData(newData);

    setSelectedRole(null);
    setActionType(null);
  };

  const handleDeleteRole = () => {
    toast.success(
      `Role ${selectedRole?.roleName} has been successfully deleted.`,
    );
    setData(prevData => [...prevData.filter(r => r.id !== selectedRole?.id)]);
    setSelectedRole(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Role Management
          </h1>
          <p className="text-muted-foreground">
            Manage user roles and access levels
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Role
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">Roles List</CardTitle>
          <CardDescription>View and manage all system roles</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex items-center gap-4">
            <div className="relative max-w-sm flex-1">
              <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search roles..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams({ ...queryParams, search: e.target.value })
                }
                className="pl-10"
              />
            </div>
          </div>

          <div className="rounded-md border border-card-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-semibold text-foreground">
                    Role Name
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Description
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Access Level
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Theme
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Users
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Created
                  </TableHead>
                  <TableHead className="text-center font-semibold text-foreground">
                    Actions
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading || data.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={7}
                      className="text-center text-muted-foreground"
                    >
                      {loading ? 'Loading...' : 'No roles found'}
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map(role => (
                    <TableRow key={role.id}>
                      <TableCell className="font-medium text-foreground">
                        {role.roleName}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {role.description}
                      </TableCell>
                      <TableCell>
                        <Badge variant="secondary">
                          Level {role.accessLevel}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className={getColorTheme(role.colorTheme)}>
                          {role.colorTheme.toUpperCase()}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-foreground">
                        {role.userCount ?? 0} users
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {new Date(
                          role.createdAt as string,
                        ).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedRole(role);
                              setActionType('edit');
                            }}
                            className="hover:bg-muted"
                          >
                            <Edit className="size-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedRole(role);
                              setShowDeleteDialog(true);
                            }}
                            className="hover:bg-destructive/20 hover:text-destructive"
                          >
                            <Trash2 className="size-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      <ActionRole
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedRole(null);
        }}
        role={selectedRole}
        onSubmit={handleSubmitRole}
      />

      {selectedRole && (
        <DeleteRole
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          role={selectedRole}
          onDeleteRole={handleDeleteRole}
        />
      )}
    </div>
  );
};

export default Roles;

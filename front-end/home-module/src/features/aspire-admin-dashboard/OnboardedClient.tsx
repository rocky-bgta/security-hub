import { Loader2, Plus } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import OrganizationCard from 'components/OrganizationCard';
import useAPI from 'hooks/UseAPI';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IClientOnboarding } from 'models/Client';
import ViewClientDetails from 'features/client-view/ViewClientDetails';
import { InitGetListParams } from 'utils/Constants';
import { IGetListParams } from 'models/Global';
import useDebounce from 'hooks/UseDebounce';
import { objectToQueryString } from 'utils/Helper';
import { routes } from 'routes/Route';
import Pagination from 'common/Pagination';
import OrganizationCardLoader from 'components/skeleton/OrganizationCard';

interface IClientOnboardingResponse {
  clientAdmins: Array<IClientOnboarding>;
  offset: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

const OnboardedClient = () => {
  const navigate = useNavigate();
  const apiClient = useAPI();
  const [clientData, setClientData] = useState<IClientOnboardingResponse>({
    clientAdmins: [],
    offset: 0,
    pageSize: 0,
    total: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [isOpenViewDialog, setIsOpenViewDialog] = useState<boolean>(false);
  const [selectedClientId, setSelectedClientId] = useState<string>();
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    offset: 0,
    pageSize: 4,
  });
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchClientData();
    }
  }, [searchDebounce]);

  const fetchClientData = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_LIST + queryString,
      );
      setClientData(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewClick = (id: string) => {
    setIsOpenViewDialog(true);
    setSelectedClientId(id);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <Fragment>
      <Card>
        <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
          <CardTitle>
            <Link
              to="/#"
              className="home-text-xl home-font-semibold home-text-white home-underline"
            >
              Onboarded Client
            </Link>
          </CardTitle>
          <div className="home-flex home-w-full home-flex-col home-gap-3 sm:home-flex-row lg:home-w-auto lg:home-items-center">
            <Input
              id="search"
              placeholder="Search Client"
              className="home-w-full home-text-foreground sm:home-w-64"
              value={queryParams.search}
              onChange={e =>
                setQueryParams({ ...queryParams, search: e.target.value })
              }
            />
            <Button
              onClick={() => navigate(routes.clientOnboarding.path)}
              className="home-w-full home-text-nowrap home-rounded home-px-4 home-py-2 sm:home-w-auto"
            >
              <Plus /> Add Client
            </Button>
          </div>
        </CardHeader>

        <CardContent>
          {loading ? (
            <OrganizationCardLoader count={4} />
          ) : (
            <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2 xl:home-grid-cols-4">
              {clientData.clientAdmins.map(client => (
                <OrganizationCard
                  key={client.id}
                  icon={client.logoUrl || ''}
                  brandName={client.organizationName}
                  licenseCount={
                    client.clientProducts.reduce(
                      (acc, product) => acc + product.licenseCount,
                      0,
                    ) || 10
                  }
                  status={client.status}
                  onViewClick={() => handleViewClick(client.id)}
                />
              ))}
            </div>
          )}
        </CardContent>
        <CardFooter>
          <Pagination
            total={clientData.total || 0}
            perPage={queryParams.pageSize || 0}
            onPageChange={onPageChangeHandler}
          />
        </CardFooter>
      </Card>

      {selectedClientId && (
        <ViewClientDetails
          clientId={selectedClientId}
          open={isOpenViewDialog}
          onClose={() => {
            setIsOpenViewDialog(false);
            setSelectedClientId(undefined);
          }}
        />
      )}
    </Fragment>
  );
};

export default OnboardedClient;

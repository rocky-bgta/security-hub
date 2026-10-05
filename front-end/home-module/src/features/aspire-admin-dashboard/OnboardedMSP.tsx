import { Plus } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { Fragment, useCallback, useEffect, useState } from 'react';

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
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IMSPOnboarding } from 'models/Client';
import ViewMSPDetails from 'features/msp-view/ViewMSPDetails';
import { InitGetListParams } from 'utils/Constants';
import { IGetListParams, IList } from 'models/Global';
import useDebounce from 'hooks/UseDebounce';
import { objectToQueryString } from 'utils/Helper';
import { routes } from 'routes/Route';
import Pagination from 'common/Pagination';
import OrganizationCardLoader from 'components/skeleton/OrganizationCard';

const OnboardedMSP = () => {
  const navigate = useNavigate();
  const apiClient = useAPI();
  const [mspData, setMspData] = useState<IList<IMSPOnboarding>>({
    offset: 0,
    pageSize: 0,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [isOpenViewDialog, setIsOpenViewDialog] = useState<boolean>(false);
  const [selectedMspId, setSelectedMspId] = useState<string>();
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

  const fetchMSPData = useCallback(async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.MSP_LIST + queryString,
      );
      setMspData(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [apiClient, queryString]);

  useEffect(() => {
    if (searchDebounce) {
      fetchMSPData();
    }
  }, [fetchMSPData, searchDebounce]);

  const handleViewClick = (id: string) => {
    setIsOpenViewDialog(true);
    setSelectedMspId(id);
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
              Onboarded MSP
            </Link>
          </CardTitle>
          <div className="home-flex home-w-full home-flex-col home-gap-3 sm:home-flex-row lg:home-w-auto lg:home-items-center">
            <Input
              id="search"
              placeholder="Search MSP"
              className="home-w-full home-text-foreground sm:home-w-64"
              value={queryParams.search}
              onChange={e =>
                setQueryParams({ ...queryParams, search: e.target.value })
              }
            />
            <Button
              onClick={() => navigate(routes.mspOnboarding.path)}
              className="home-w-full home-text-nowrap home-rounded home-px-4 home-py-2 sm:home-w-auto"
            >
              <Plus /> Add MSP
            </Button>
          </div>
        </CardHeader>

        <CardContent>
          {loading ? (
            <OrganizationCardLoader count={4} />
          ) : (
            <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2 xl:home-grid-cols-4">
              {mspData?.items?.map(msp => (
                <OrganizationCard
                  key={msp.id}
                  icon={msp.logoUrl || ''}
                  brandName={msp.organizationName}
                  licenseCount={
                    msp.mspProducts?.reduce(
                      (acc, product) => acc + product.licenseCount,
                      0,
                    ) || 10
                  }
                  status={msp.status}
                  onViewClick={() => handleViewClick(msp.id)}
                />
              ))}
            </div>
          )}
        </CardContent>
        <CardFooter>
          <Pagination
            total={mspData.total || 0}
            perPage={queryParams.pageSize || 0}
            onPageChange={onPageChangeHandler}
          />
        </CardFooter>
      </Card>

      {selectedMspId && (
        <ViewMSPDetails
          mspId={selectedMspId}
          open={isOpenViewDialog}
          onClose={() => {
            setIsOpenViewDialog(false);
            setSelectedMspId(undefined);
          }}
        />
      )}
    </Fragment>
  );
};

export default OnboardedMSP;

import { Link } from 'react-router-dom';
import {
  Bar,
  BarChart,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { useEffect, useState } from 'react';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import { API_END_POINTS } from 'routes/APIEndpoints';
import useAPI from 'hooks/UseAPI';
import { IClientAdminLicenseStatistics } from 'models/Dashboard';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import { routes } from 'routes/Route';
import { IResponse } from 'models/Context';
import Loader from 'common/loader/Loader';
import {
  Select,
  SelectItem,
  SelectContent,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import useStore from 'hooks/UseStore';
import { ClientProductTag, IGetListParams, IList } from 'models/Global';
import { IAssignedLicense } from 'features/quick-start/assign/Step1';

const getUniqueProducts = (products: IAssignedLicense[]) =>
  Array.from(
    new Map(products.map(product => [product.productId, product])).values(),
  );

const LicenseStatistics = () => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<
    {
      key: keyof IClientAdminLicenseStatistics;
      name: string;
      value: number;
      color: string;
    }[]
  >([
    {
      key: 'licenseCount',
      name: 'Total Licenses',
      value: 0,
      color: '#1A88E0',
    },
    {
      key: 'availableLicenseCount',
      name: 'Available',
      value: 0,
      color: '#37BE99',
    },
    {
      key: 'usedLicenseCount',
      name: 'Assigned',
      value: 0,
      color: '#F7C948',
    },
  ]);
  const [productList, setProductList] = useState<IList<IAssignedLicense>>({
    items: [],
    offset: 0,
    pageSize: 0,
    total: 0,
  });
  const [productFilter, setProductFilter] = useState<string>('all');
  const [queryParams, setQueryParams] = useState<IGetListParams | null>(null);

  useEffect(() => {
    if (!userInfo?.userId) {
      return;
    }

    let cancelled = false;

    const fetchProductList = async () => {
      try {
        setLoading(true);
        const response = await apiClient.get(
          API_END_POINTS.CLIENT_ADMIN_ASSIGN_PRODUCT_LIST.replace(
            ':clientAdminId',
            userInfo.userId,
          ),
        );
        if (cancelled) {
          return;
        }
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }
        const uniqueProducts = getUniqueProducts(response.data.items);
        setProductList({
          ...response.data,
          items: uniqueProducts,
        });
        const defaultProduct =
          uniqueProducts.find(product =>
            product.product?.tags?.includes(ClientProductTag.SECURITY),
          ) ?? uniqueProducts[0];
        const productId = defaultProduct?.productId ?? '';
        setQueryParams({ productId });
        setProductFilter(productId || 'all');
      } catch (error) {
        console.error('Error fetching product list:', error);
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void fetchProductList();

    return () => {
      cancelled = true;
    };
  }, [apiClient, userInfo?.userId]);

  useEffect(() => {
    if (!queryParams) {
      return;
    }

    let cancelled = false;

    const fetchLicenseData = async () => {
      try {
        setLoading(true);
        const queryString = objectToQueryString(queryParams);
        const response: IResponse<IClientAdminLicenseStatistics> =
          await apiClient.get(
            API_END_POINTS.CLIENT_ADMIN_LICENSE_STATISTICS + queryString,
          );
        if (cancelled) {
          return;
        }
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        setData(prev =>
          prev.map(item => ({
            ...item,
            value:
              response.data[item.key as keyof IClientAdminLicenseStatistics],
          })),
        );
      } catch (error) {
        console.error('Error fetching license data:', error);
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void fetchLicenseData();

    return () => {
      cancelled = true;
    };
  }, [apiClient, queryParams]);

  const totalValue = data.reduce((acc, curr) => acc + curr.value, 0);
  const maxValue = Math.max(...data.map(d => d.value), 1);

  const displayData =
    totalValue === 0
      ? data.map(d => ({ ...d, displayValue: 1, isDisabled: true }))
      : data.map(d => ({
          ...d,
          displayValue: d.value === 0 ? maxValue : d.value,
          isDisabled: d.value === 0,
        }));

  const CustomTooltip = ({
    active,
    payload,
    coordinate,
  }: {
    active?: boolean;
    payload?: any;
    coordinate?: { x: number; y: number };
  }) => {
    if (active && payload && payload.length && coordinate) {
      const chartHeight = 320;
      const maxDisplayValue = Math.max(
        ...displayData.map(d => d.displayValue),
        1,
      );
      const barHeight =
        (payload[0].payload.displayValue / maxDisplayValue) *
        (chartHeight - 80);
      const barTop = chartHeight - 50 - barHeight;

      return (
        <div className="home-relative">
          <div
            className="home-absolute home-rounded home-px-3 home-py-1 home-text-sm home-font-medium home-text-black"
            style={{
              backgroundColor: payload[0].payload.color,
              transform: 'translate(-50%, -100%)',
              top: barTop,
              left: coordinate.x,
              pointerEvents: 'none',
            }}
          >
            {payload[0].payload.value}
            <div
              className="home-absolute home-left-1/2 home-top-full home-size-0 -home-translate-x-1/2 home-transform home-border-x-4 home-border-t-4 home-border-transparent"
              style={{ borderTopColor: payload[0].payload.color }}
            ></div>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-items-center lg:home-justify-between">
        <CardTitle>
          <Link
            to={routes?.licenseHistory?.path ?? '/'}
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            License Statistics
          </Link>
        </CardTitle>
        <div className="home-w-full lg:home-w-72">
          <Select
            value={productFilter}
            onValueChange={value => {
              setProductFilter(value);
              setQueryParams(prev => ({
                ...prev,
                productId: value === 'all' ? '' : (value as string),
              }));
            }}
          >
            <SelectTrigger className="home-w-full">
              <SelectValue placeholder="Select Product" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Products</SelectItem>
              {productList.items.map((product: IAssignedLicense) => (
                <SelectItem key={product.productId} value={product.productId}>
                  {product.product.productName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardHeader>

      <CardContent className="home-flex home-flex-col home-items-center home-gap-4 md:home-gap-6 lg:home-flex-row lg:home-justify-evenly">
        {loading ? (
          <Loader mode="container" />
        ) : (
          <>
            <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px] lg:home-w-3/5">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={displayData}
                margin={{
                  top: 40,
                  right: 30,
                  left: 0,
                  bottom: 5,
                }}
                barCategoryGap="80%"
              >
                <XAxis
                  dataKey="name"
                  axisLine={false}
                  tickLine={false}
                  tick={false}
                />
                <YAxis hide domain={[0, 'auto']} />
                <Tooltip content={<CustomTooltip />} cursor={false} />
                <Bar dataKey="displayValue" radius={[4, 4, 4, 4]}>
                  {displayData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={entry.isDisabled ? 'transparent' : entry.color}
                      stroke={entry.isDisabled ? '#fafafa90' : ''}
                      opacity={entry.isDisabled ? 0.5 : 1}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
            </div>

            <ChartLegend data={data} />
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default LicenseStatistics;

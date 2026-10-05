import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import { useEffect, useState } from 'react';
import useAPI from 'hooks/UseAPI';
import {
  Select,
  SelectItem,
  SelectContent,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import useStore from 'hooks/UseStore';
import { ClientProductTag, IGetListParams, IList } from 'models/Global';
import Loader from 'common/loader/Loader';
import { IAssignedLicense } from 'features/quick-start/assign/Step1';

const getUniqueProducts = (products: IAssignedLicense[]) =>
  Array.from(
    new Map(products.map(product => [product.productId, product])).values(),
  );

interface IProps {
  key: keyof IRiskByContent;
  name: string;
  value: number;
  color: string;
}

interface IRiskByContent {
  safeUsers: number;
  lowRisk: number;
  averageRisk: number;
  highRisk: number;
  totalUsers: number;
}

const RiskByContent = () => {
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<IProps[]>([
    {
      key: 'safeUsers',
      name: 'Safe Users',
      value: 0,
      color: '#37BE99',
    },
    {
      key: 'lowRisk',
      name: 'Low Risk',
      value: 0,
      color: '#F7C948',
    },
    {
      key: 'averageRisk',
      name: 'Average Risk',
      value: 0,
      color: '#12E6C8',
    },
    {
      key: 'highRisk',
      name: 'High Risk',
      value: 0,
      color: '#F65E5B',
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

    const fetchRiskByContent = async () => {
      try {
        setLoading(true);
        const queryString = objectToQueryString(queryParams);
        const response = await apiClient.get(
          API_END_POINTS.CLIENT_ADMIN_USER_RISK_BY_CONTENT + queryString,
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
            value: response.data[item.key as keyof IRiskByContent] ?? 0,
          })),
        );
      } catch (error) {
        console.error('Error fetching risk by content:', error);
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void fetchRiskByContent();

    return () => {
      cancelled = true;
    };
  }, [apiClient, queryParams]);

  const totalValue = data.reduce((acc, entry) => acc + entry.value, 0);

  const CustomTooltip = ({
    active,
    payload,
  }: {
    active: boolean;
    payload: any;
  }) => {
    if (active && payload && payload.length) {
      return (
        <div className="home-flex home-items-center home-rounded home-bg-[#2B414F] home-px-4 home-py-2 home-shadow-lg">
          <div
            className="home-mr-2 home-size-3 home-rounded-full"
            style={{ backgroundColor: payload[0].payload.color }}
          />
          <p className="home-text-center home-font-semibold home-text-white">
            <span className="home-mr-4 home-text-center home-text-sm home-text-white home-text-opacity-50">
              {payload[0].name}
            </span>{' '}
            {`${totalValue === 0 ? 0 : payload[0].value}`}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-items-center lg:home-justify-between">
        <CardTitle>
          {/* <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          ></Link> */}
          User Risk By Content
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
            <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-w-3/5">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                {totalValue === 0 ? (
                  <Pie
                    data={[
                      { name: 'No Data', value: 1, color: '#37BE99' },
                      { name: 'No Data', value: 1, color: '#F7C948' },
                      { name: 'No Data', value: 1, color: '#12E6C8' },
                      { name: 'No Data', value: 1, color: '#F65E5B' },
                    ]}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={120}
                    paddingAngle={6}
                    cornerRadius={8}
                    startAngle={90}
                    endAngle={-270}
                    stroke="none"
                  >
                    <Cell key="cell-no-data1" fill="#37BE99" />
                    <Cell key="cell-no-data2" fill="#F7C948" />
                    <Cell key="cell-no-data3" fill="#12E6C8" />
                    <Cell key="cell-no-data4" fill="#F65E5B" />
                  </Pie>
                ) : (
                  <Pie
                    data={data}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={120}
                    paddingAngle={6}
                    cornerRadius={8}
                    startAngle={90}
                    endAngle={-270}
                    stroke="none"
                  >
                    {data.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                )}
                <Tooltip
                  content={<CustomTooltip active={false} payload={[]} />}
                />
              </PieChart>
            </ResponsiveContainer>
            </div>

            <ChartLegend title={`Total User ${totalValue}`} data={data} />
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default RiskByContent;

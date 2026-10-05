import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import NoDataText from 'common/NoDataText';
import Pagination from 'common/Pagination';
import CertificateCard from 'components/CertificateCard';
import CertificateCardLoader from 'components/skeleton/CertificateCard';
import Border from 'components/UserBorder';
import UserHeading from 'components/UserHeading';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useStore } from 'hooks/UseStore';
import { IUserCertificate } from 'models/Certificate';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { useSearchParams } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

const CertificateList = () => {
  const { userInfo } = useStore();
  const [searchParams] = useSearchParams();
  const courseId = searchParams.get('courseId');
  const [loading, setLoading] = useState<boolean>(true);
  const [certificateData, setCertificateData] = useState<
    IList<IUserCertificate>
  >({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    userId: userInfo.userId,
    subPackageId: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    if (courseId) {
      setQueryParams(prev => ({
        ...prev,
        subPackageId: courseId || '',
      }));
    } else {
      setQueryParams(prev => ({
        ...prev,
        subPackageId: '',
      }));
    }
  }, [courseId]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCertificateData();
    }
  }, [searchDebounce]);

  const fetchCertificateData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserCertificate>> = await apiClient.get(
        API_END_POINTS.USER_CERTIFICATE_LIST + queryString,
      );
      setCertificateData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching certificate data', error);
    } finally {
      setLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };
  return (
    <Fragment>
      <UserHeading
        variant="title"
        text="Certificates"
        className="content-mb-4 sm:content-mb-6"
      />
      <Border>
        <div className="content-p-4 sm:content-p-6">
          <UserHeading
            variant="subtitle"
            text="Certification Library"
            className="content-mb-4 sm:content-mb-6"
          />
          {loading ? (
            <CertificateCardLoader count={8} />
          ) : (
            <Fragment>
              {certificateData?.items?.length > 0 ? (
                <div className="content-grid content-grid-cols-1 content-gap-4 sm:content-gap-6 sm:content-grid-cols-2 lg:content-grid-cols-4">
                  {certificateData?.items?.map((item, index) => (
                    <CertificateCard key={index} data={item} />
                  ))}
                </div>
              ) : (
                <NoDataText text="No Certificate Found" />
              )}
            </Fragment>
          )}
          <div className="content-pt-6">
            <Pagination
              variant="user"
              total={certificateData.total}
              perPage={certificateData.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </div>
      </Border>
    </Fragment>
  );
};
export default CertificateList;

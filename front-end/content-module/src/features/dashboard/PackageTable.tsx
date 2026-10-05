import clsx from 'clsx';
import NoDataText from 'common/NoDataText';
import Border from 'components/UserBorder';
import CourseTableLoader from 'components/skeleton/CourseTable';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IList, IResponse } from 'models/Global';
import { IUserPackage } from 'models/Package';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';

const PackageTable = () => {
  const { userInfo } = useStore();
  const [packages, setPackages] = useState<IUserPackage[]>([]);
  const [loading, setLoading] = useState(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchPackageDetails();
  }, []);

  const fetchPackageDetails = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserPackage>> = await apiClient.get(
        API_END_POINTS.USER_PACKAGE_LIST +
          'offset=0&pageSize=12&' +
          objectToQueryString({
            userId: userInfo.userId,
          }),
      );

      setPackages(response.data.items);
    } catch (error) {
      console.error('Error fetching package details:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content-mt-4 sm:content-mt-6">
      {loading ? (
        <CourseTableLoader />
      ) : (
        <Border>
          <div className="content-p-4 sm:content-p-6">
            <h2 className="content-mb-2 content-text-lg content-font-semibold content-text-white sm:content-mb-3 sm:content-text-xl">
              Packages
            </h2>

            <table className="content-min-w-full content-text-white">
              <thead className="content-bg-white content-bg-opacity-25">
                <tr className="content-border-card-border content-text-xs content-uppercase content-text-white">
                  <th className="content-w-[10%] content-p-3 content-text-left">
                    SR.NO
                  </th>
                  <th className="content-w-[30%] content-p-3 content-text-left">
                    Package Name
                  </th>
                  <th className="content-w-[10%] content-py-3 content-text-left">
                    Total Courses
                  </th>
                  <th className="content-w-[10%] content-py-3 content-text-left">
                    Validity
                  </th>
                  <th className="content-w-[10%] content-py-3 content-text-left">
                    Status
                  </th>
                  <th className="content-w-[30%] content-py-3 content-pr-6 content-text-left">
                    Progress
                  </th>
                </tr>
              </thead>
            </table>
            <div className="content-max-h-[400px] content-overflow-y-auto content-pr-1">
              <table className="content-min-w-full content-text-white">
                <tbody>
                  {packages?.length > 0 ? (
                    <>
                      {packages.map((pkg, index) => (
                        <tr
                          key={index}
                          className={`text-sm content-border-b content-border-card-border last:content-border-none`}
                        >
                          <td className="content-w-[10%] content-px-3 content-py-4">
                            {index + 1}
                          </td>
                          <td className="content-w-[30%] content-px-1 content-py-4">
                            {pkg.packageName}
                          </td>
                          <td className="content-w-[10%] content-px-1 content-py-4">
                            {pkg.totalCourses}
                          </td>
                          <td className="content-w-[10%] content-px-1 content-py-4">
                            {pkg.validity}
                          </td>
                          <td
                            className={clsx(
                              `content-w-[10%] content-px-1 content-py-4 ${pkg.expired ? 'content-text-vibrant-red' : ''}`,
                            )}
                          >
                            {pkg.expired ? 'Expired' : 'Active'}
                          </td>
                          <td className="content-flex content-w-full content-gap-4 content-px-1 content-py-4">
                            <div className="content-flex content-w-full content-items-center content-gap-2">
                              {Math.round(pkg.progress)}%
                              <div className="content-h-2.5 content-w-full content-rounded-full content-bg-white content-bg-opacity-25">
                                <div
                                  className="content-h-2.5 content-w-full content-rounded-full content-bg-primary"
                                  style={{
                                    width: `${Math.round(pkg.progress)}%`,
                                  }}
                                ></div>
                              </div>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </>
                  ) : (
                    <NoDataText
                      text="No Packages Found"
                      className="content-mt-10 content-text-center"
                    />
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </Border>
      )}
    </div>
  );
};

export default PackageTable;

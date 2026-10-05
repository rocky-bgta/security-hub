import PackageCard from 'components/PackageCard';
import { Fragment } from 'react/jsx-runtime';
import { Navigation } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/react';

import { SliderLeftIcon, SliderRightIcon } from 'assets/icons';
import NoDataText from 'common/NoDataText';
import PackageCardLoader from 'components/skeleton/PackageCard';
import { IGetListParams } from 'models/Global';
import { IUserPackage } from 'models/Package';

interface IProps {
  loading: boolean;
  data: Array<IUserPackage>;
  queryParams: IGetListParams;
  setQueryParams: React.Dispatch<React.SetStateAction<IGetListParams>>;
}
const UserPackageList = ({
  loading,
  data,
  queryParams,
  setQueryParams,
}: IProps) => {
  return (
    <div className="content-relative content-mb-6">
      {loading ? (
        <PackageCardLoader />
      ) : (
        <Fragment>
          {data.length > 0 ? (
            <Swiper
              modules={[Navigation]}
              navigation={{
                nextEl: '.arrow-right',
                prevEl: '.arrow-left',
              }}
              slidesPerView={4}
              spaceBetween={20}
              allowTouchMove={true}
            >
              {data.map((item: IUserPackage) => (
                <SwiperSlide key={item.packageId}>
                  <PackageCard
                    data={item}
                    queryParams={queryParams}
                    setQueryParams={setQueryParams}
                  />
                </SwiperSlide>
              ))}
              <div className="arrow-slider">
                <button className="arrow-left slider-arrow content-absolute content-left-0 content-top-2/4 content-z-50 content-flex content-size-8 -content-translate-y-2/4 content-items-center content-justify-center content-rounded-full content-bg-white content-transition content-duration-300 hover:content-bg-primary disabled:content-hidden">
                  <SliderLeftIcon />
                </button>
                <button className="arrow-right slider-arrow content-absolute content-left-auto content-right-0 content-top-2/4 content-z-50 content-flex content-size-8 -content-translate-y-2/4 content-items-center content-justify-center content-rounded-full content-bg-white content-transition content-duration-300 hover:content-bg-primary disabled:content-hidden">
                  <SliderRightIcon />
                </button>
              </div>
            </Swiper>
          ) : (
            <NoDataText text="No Package Found" />
          )}
        </Fragment>
      )}
    </div>
  );
};
export default UserPackageList;

import { Dispatch, SetStateAction, useEffect, useRef } from 'react';
import { Fragment } from 'react/jsx-runtime';
import { Navigation } from 'swiper/modules';
import { Swiper as SwiperType, SwiperSlide, Swiper } from 'swiper/react';

import { SliderLeftIcon, SliderRightIcon } from 'assets/icons';
import NoDataText from 'common/NoDataText';
import PackageCardLoader from 'components/skeleton/PackageCard';
import SubPackageCard from 'components/SubPackageCard';
import { IUserSubPackage } from 'models/Package';
import { useNavigate, useSearchParams } from 'react-router-dom';

interface IProps {
  loading: boolean;
  data: Array<IUserSubPackage>;
  selectedSubPackage: IUserSubPackage | null;
  setSelectedSubPackage: Dispatch<SetStateAction<IUserSubPackage | null>>;
}
const UserSubPackageList = ({
  loading,
  data,
  selectedSubPackage,
  setSelectedSubPackage,
}: IProps) => {
  const swiperRef = useRef<typeof SwiperType | null>(null);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const selectedSubpackageId = searchParams.get('course');

  useEffect(() => {
    if (!swiperRef.current || !selectedSubpackageId || !data.length) return;

    const index = data.findIndex(
      item => item.subPackageId === selectedSubpackageId,
    );

    if (index !== -1) {
      (swiperRef.current as any).slideTo(index, 600);
      setSelectedSubPackage(data[index]);
    }
  }, [selectedSubpackageId, data]);

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
              slidesPerView={1}
              spaceBetween={12}
              breakpoints={{
                640: { slidesPerView: 2, spaceBetween: 16 },
                1024: { slidesPerView: 3, spaceBetween: 20 },
                1280: { slidesPerView: 4, spaceBetween: 20 },
              }}
              allowTouchMove={true}
              onSwiper={(swiper: any) => (swiperRef.current = swiper)}
            >
              {data.map((item: IUserSubPackage) => (
                <SwiperSlide key={item.subPackageId}>
                  <SubPackageCard
                    data={item}
                    isSelected={
                      selectedSubPackage?.subPackageId === item.subPackageId
                    }
                    onClick={() => {
                      setSelectedSubPackage(item);
                      navigate('?course=' + item.subPackageId);
                    }}
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
            <NoDataText text="No Courses Found" />
          )}
        </Fragment>
      )}
    </div>
  );
};
export default UserSubPackageList;

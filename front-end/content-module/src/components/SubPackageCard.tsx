import { MouseEvent } from 'react';
import { useNavigate } from 'react-router-dom';

import { CourseIcon, ExamIcon } from 'assets/icons';
import Border from 'components/UserBorder';
import { IUserSubPackage, UserSubPackageStatus } from 'models/Package';
import { routes } from 'routes/Routes';
import { cn } from 'utils/Helper';

interface IProps {
  data: IUserSubPackage;
  isSelected: boolean;
  onClick: () => void;
}

const SubPackageCard = ({ data, isSelected, onClick }: IProps) => {
  const navigate = useNavigate();

  const handleTakeExam = (e: MouseEvent) => {
    e.stopPropagation();
    navigate(routes.exam.path.replace(':slug', data.subPackageId));
  };

  return (
    <Border>
      <div className="content-tooltip content-w-full">
        {/* Card Body */}
        <div
          onClick={onClick}
          className={cn(
            isSelected
              ? 'content-bg-primary hover:content-bg-primary'
              : 'hover:content-bg-white hover:content-bg-opacity-25',
            'content-relative content-w-full content-cursor-pointer content-px-4 content-py-3 content-transition-colors content-duration-200',
          )}
        >
          <p className="content-line-clamp-1 content-text-left content-text-base content-font-semibold content-text-white">
            {data.subPackageName}
          </p>
          <p className="content-line-clamp-1 content-text-left content-text-sm content-text-cloudy-white">
            {data.productName}
          </p>

          <div className="content-flex content-items-center content-justify-between content-gap-x-2 content-pt-2">
            <div className="content-flex content-items-center content-gap-1 content-py-1">
              <CourseIcon
                className="group-hover:content-fill-black"
                fill="white"
              />
              <span
                className={cn(
                  isSelected ? 'content-opacity-100' : 'content-opacity-75',
                  'content-text-sm content-text-white group-hover:content-opacity-100',
                )}
              >
                {data.topicCount} Topics
              </span>
            </div>

            {data.status === UserSubPackageStatus.EXAM && (
              <button
                onClick={handleTakeExam}
                className="content-flex content-animate-bounce content-items-center content-gap-1 content-rounded-full content-bg-yellow-500 content-px-2 content-py-1"
              >
                <ExamIcon stroke="#192129" />
                <span className="content-text-xs content-text-dark">
                  Take Exam
                </span>
              </button>
            )}
          </div>
          {/* Tooltip */}
          <div className="content-tooltip-text sub-package-card-tooltip">
            <span className="content-block content-text-xs">
              {data.subPackageName}
            </span>
            <span className="content-block content-text-xs">
              {data.productName}
            </span>
          </div>
        </div>
      </div>
    </Border>
  );
};

export default SubPackageCard;

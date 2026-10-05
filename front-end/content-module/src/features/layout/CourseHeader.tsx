import { useEffect, useState } from 'react';
import { RxCross2 } from 'react-icons/rx';
import { useLocation, useNavigate, useParams } from 'react-router-dom';

import { Button } from 'common/Button';
import { routes } from 'routes/Routes';

const CourseHeader = ({ hostPath = routes }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { slug } = useParams();
  const { state } = useLocation();
  const [courseName, setCourseName] = useState<string>(state?.courseName ?? '');

  useEffect(() => {
    if (state?.courseName) {
      setCourseName(state?.courseName);
    }
  }, [state]);

  const handleClose = () => {
    if (location.pathname.includes('/settings')) {
      navigate(hostPath.courseChapters.path.replace(':slug', slug as string));
    } else {
      navigate(hostPath.addContent.path, { replace: true });
    }
  };

  return (
    <div className="content-flex content-items-center content-justify-between">
      <h3 className="content-font-medium content-text-white">{courseName}</h3>
      <div className="content-flex content-items-center content-gap-x-4">
        <Button size="sm" variant="outline">
          Save & Preview
        </Button>
        <Button
          onClick={handleClose}
          className="content-size-fit !content-bg-transparent content-p-0 content-text-secondary"
        >
          <RxCross2 className="content-text-2xl content-text-white" />
        </Button>
      </div>
    </div>
  );
};

export default CourseHeader;

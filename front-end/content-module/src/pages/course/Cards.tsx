import AspireCourseCardList from 'features/course/CourseCardList';
import MSPCourseCardList from 'features/course/msp-admin/CourseCardList';
import { useAuth } from 'hooks/UseAuth';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

interface IProps {
  hostPath: typeof routes;
}

const CourseCardList = ({ hostPath }: IProps) => {
  const { role } = useAuth();

  if (role === ROLE.MSP_ADMIN) {
    return <MSPCourseCardList hostPath={hostPath} />;
  }

  return <AspireCourseCardList hostPath={hostPath} />;
};

export default CourseCardList;

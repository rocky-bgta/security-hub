import { useAuth } from 'hooks/UseAuth';
import ClientUserCourseList from 'pages/course/ClientUserList';
import SuperAdminCourseList from 'pages/course/SuperAdminList';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const CourseList = ({ hostPath = routes }) => {
  const { role } = useAuth();

  if (role === ROLE.SUPER_ADMIN) {
    return <SuperAdminCourseList hostPath={hostPath} />;
  }

  if (role === ROLE.CLIENT_USER) {
    return <ClientUserCourseList hostPath={hostPath} />;
  }
};

export default CourseList;

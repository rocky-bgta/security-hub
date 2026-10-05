import { Navigate, Route, Routes } from 'react-router-dom';

import CreateSurvey from 'pages/survey/CreateSurvey';
import SurveyList from 'pages/survey/SurveyList';
import { routes } from 'routes/AppRoutes';

const SurveyRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="polls-surveys" />} />

      <Route path={routes.pollSurvey.path} element={<SurveyList />} />
      <Route path={routes.createSurvey.path} element={<CreateSurvey />} />
    </Routes>
  );
};

export default SurveyRouter;

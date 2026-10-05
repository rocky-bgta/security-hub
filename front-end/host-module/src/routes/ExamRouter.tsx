import { Navigate, Route, Routes } from 'react-router-dom';

import BulkExamImport from 'pages/exam/BulkExamImport';
import ExamAnalytics from 'pages/exam/ExamAnalytics';
import ExamSettings from 'pages/exam/ExamSettings';
import ExamineeReports from 'pages/exam/ExamineeReports';
import ExamsList from 'pages/exam/ExamsList';
import { routes } from 'routes/AppRoutes';
import ExamLibrary from 'pages/exam/ExamLibrary';

const ExamRouter = () => {
  return (
    <Routes>
      <Route index element={<Navigate to="exam-list" />} />

      <Route path={routes.examLibrary.path} element={<ExamLibrary />} />
      <Route path={routes.examList.path} element={<ExamsList />} />
      <Route path={routes.bulkExamImport.path} element={<BulkExamImport />} />
      <Route path={routes.examSetting.path} element={<ExamSettings />} />
      <Route path={routes.examineeReports.path} element={<ExamineeReports />} />
      <Route path={routes.examAnalytics.path} element={<ExamAnalytics />} />
    </Routes>
  );
};

export default ExamRouter;

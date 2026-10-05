import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSurveyList = lazy(() => import('miscellaneous-module/PollSurvey'));

const SurveyList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSurveyList />
    </ErrorBoundaryWrapper>
  );
};

export default SurveyList;

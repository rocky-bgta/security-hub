// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteCreateSurvey = lazy(
//   () => import('miscellaneous-module/CreateSurvey'),
// );

const CreateSurvey = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteCreateSurvey /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default CreateSurvey;

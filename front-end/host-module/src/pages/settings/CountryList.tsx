import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCountryList = lazy(
  () => import('miscellaneous-module/CountryList'),
);

const CountryList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCountryList />
    </ErrorBoundaryWrapper>
  );
};

export default CountryList;

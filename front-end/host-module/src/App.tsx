import { Suspense } from 'react';

import Loader from 'components/Loader';
import AppRouter from 'routes/AppRouter';

const App = () => {
  return (
    <Suspense fallback={<Loader />}>
      <AppRouter />
    </Suspense>
  );
};

export default App;

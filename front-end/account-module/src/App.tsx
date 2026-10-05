import { Suspense } from 'react';

import Loader from 'common/loader/Loader';
import AppRouter from 'routes/AppRouter';

function App() {
  return (
    <Suspense fallback={<Loader />}>
      <AppRouter />
    </Suspense>
  );
}

export default App;

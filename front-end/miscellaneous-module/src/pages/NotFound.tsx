import { Link } from 'react-router-dom';

import { routes } from 'routes/Routes';
import { PUBLIC_URL } from 'utils/Constants';

const NotFoundImage = PUBLIC_URL + '/images/not-found.png';

const NotFound = () => {
  return (
    <section className="flex flex-col items-center justify-center bg-transparent text-white">
      <img className="w-96" src={NotFoundImage} alt="not found" />

      <p className="mb-4 p-2 text-sm md:text-base xl:text-2xl">
        Oops! The page you are looking for doesn't exist.
      </p>
      <Link
        to={routes.dashboard.path}
        className="rounded border bg-transparent px-4 py-2 shadow"
      >
        Back to Home
      </Link>
    </section>
  );
};

export default NotFound;

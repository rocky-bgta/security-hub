import { Link } from 'react-router-dom';

import { routes } from 'routes/Route';
import { PUBLIC_URL } from 'utils/Constants';

const NotFoundImage = PUBLIC_URL + '/images/not-found.png';

const NotFound = () => {
  return (
    <section className="home-flex home-flex-col home-items-center home-justify-center home-bg-transparent home-text-white">
      <img className="home-w-96" src={NotFoundImage} alt="not found" />

      <p className="home-mb-4 home-p-2 home-text-sm md:home-text-base xl:home-text-2xl">
        Oops! The page you are looking for doesn't exist.
      </p>
      <Link
        to={routes.dashboard.path}
        className="home-rounded home-border home-bg-transparent home-px-4 home-py-2 home-shadow"
      >
        Back to Home
      </Link>
    </section>
  );
};

export default NotFound;

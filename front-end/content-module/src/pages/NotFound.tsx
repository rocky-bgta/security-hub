import { Link } from 'react-router-dom';

import { routes } from 'routes/Routes';
import { PUBLIC_URL } from 'utils/Constants';

const NotFoundImage = PUBLIC_URL + '/images/not-found.png';

const NotFound = () => {
  return (
    <section className="content-flex content-flex-col content-items-center content-justify-center content-bg-transparent content-text-white">
      <img className="content-w-96" src={NotFoundImage} alt="not found" />

      <p className="content-mb-4 content-p-2 content-text-sm md:content-text-base xl:content-text-2xl">
        Oops! The page you are looking for doesn't exist.
      </p>
      <Link
        to={routes.dashboard.path}
        className="content-rounded content-border content-bg-transparent content-px-4 content-py-2 content-shadow"
      >
        Back to Home
      </Link>
    </section>
  );
};

export default NotFound;

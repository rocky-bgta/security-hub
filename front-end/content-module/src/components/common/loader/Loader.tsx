import clsx from 'clsx';

import LoadingAnimation from 'assets/animations/app-loader.svg';
import 'styles/loader.css';

interface ILoadingProps {
  mode?: 'screen' | 'container';
  blur?: boolean;
}

const Loader = ({ mode = 'screen', blur = false }: ILoadingProps) => {
  return (
    <div
      className={clsx(
        'content-loader-container',
        blur && 'content-blur',
        mode === 'screen' ? 'content-screen-size' : 'content-container-size',
      )}
    >
      <img src={LoadingAnimation} alt="loading..." height={100} width={100} />
    </div>
  );
};

export default Loader;

import LoadingAnimation from 'assets/animations/app-loader.svg';
import { cn } from 'utils/Helper';

import 'styles/loader.css';

interface ILoadingProps {
  mode?: 'screen' | 'container';
  blur?: boolean;
}

const Loader = ({ mode = 'screen', blur = false }: ILoadingProps) => {
  return (
    <div
      className={cn(
        'home-loader-container',
        blur && 'home-blur',
        mode === 'screen' ? 'home-screen-size' : 'home-container-size',
      )}
    >
      <img src={LoadingAnimation} alt="loading..." height={100} width={100} />
    </div>
  );
};

export default Loader;

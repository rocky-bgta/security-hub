import LoadingAnimation from 'assets/animations/app-loader.svg';

import 'styles/loader.css';
import { cn } from 'utils/Helper';

interface ILoadingProps {
  mode?: 'screen' | 'container';
  blur?: boolean;
}

const Loader = ({ mode = 'screen', blur = false }: ILoadingProps) => {
  return (
    <div
      className={cn(
        'loader-container',
        blur && 'blur',
        mode === 'screen' ? 'screen-size' : 'container-size',
      )}
    >
      <img src={LoadingAnimation} alt="loading..." height={100} width={100} />
    </div>
  );
};

export default Loader;

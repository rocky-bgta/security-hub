import { Button } from 'common/Button';
import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { cn } from 'utils/Helper';

interface IProps {
  to?: string;
  onClick?: () => void;
  label?: string;
  ariaLabel?: string;
  className?: string;
}

const IconBackButton = ({
  to,
  onClick,
  label = 'Back',
  ariaLabel,
  className,
}: IProps) => {
  const navigate = useNavigate();
  const accessibleName = ariaLabel ?? label;

  const handleClick = () => {
    if (onClick) {
      onClick();
      return;
    }

    if (to) {
      navigate(to);
      return;
    }

    navigate(-1);
  };

  return (
    <Button
      type="button"
      variant="outline"
      aria-label={accessibleName}
      title={accessibleName}
      className={cn('content-w-fit content-shrink-0 content-text-white', className)}
      onClick={handleClick}
    >
      <ArrowLeft className="content-size-4" />
      {label}
    </Button>
  );
};

export default IconBackButton;

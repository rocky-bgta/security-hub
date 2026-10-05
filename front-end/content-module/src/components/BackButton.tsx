import { useNavigate } from 'react-router-dom';
import { IoArrowBack } from 'react-icons/io5';

interface IProps {
  onClick?: () => void;
  text?: string;
}

const BackButton = ({ onClick, text = 'Back' }: IProps) => {
  const navigate = useNavigate();

  const handleBackClick = () => {
    if (onClick) {
      onClick();
    } else {
      navigate(-1);
    }
  };

  return (
    <button
      onClick={handleBackClick}
      className="content-mb-3 content-flex content-items-center content-text-white"
    >
      <IoArrowBack className="content-mr-2 content-text-2xl" />
      <span className="content-text-2xl content-font-semibold">{text}</span>
    </button>
  );
};

export default BackButton;

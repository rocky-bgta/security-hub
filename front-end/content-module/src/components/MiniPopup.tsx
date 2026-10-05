import { Button } from 'common/Button';

interface IMiniPopupProps {
  text: string;
  textColor: string;
  buttonText: string;
  buttonColor: string;
  buttonTextColor: string;
  onClose: () => void;
}

const MiniPopup = ({
  text,
  textColor,
  buttonText,
  buttonColor,
  buttonTextColor,
  onClose,
}: IMiniPopupProps) => {
  return (
    <div className="content-absolute content-inset-0 content-z-20 content-flex content-items-center content-justify-center content-bg-black content-bg-opacity-50">
      <div className="content-min-w-[400px] content-max-w-md content-rounded-lg content-bg-white content-p-6 content-shadow-lg">
        <p
          className="content-mb-6 content-text-black"
          style={{ color: textColor }}
        >
          {text}
        </p>
        <div className="content-flex content-justify-end content-gap-4">
          <Button
            style={{
              color: buttonTextColor,
              background: buttonColor,
            }}
            onClick={onClose}
          >
            {buttonText}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default MiniPopup;

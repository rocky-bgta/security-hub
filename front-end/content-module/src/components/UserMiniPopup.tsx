import { Button } from 'common/Button';
import Border from 'components/UserBorder';

interface IProps {
  text: string;
  textColor: string;
  buttonText: string;
  buttonColor: string;
  buttonTextColor: string;
  onClose: () => void;
}

const UserMiniPopup = ({
  text,
  textColor,
  buttonText,
  buttonColor,
  buttonTextColor,
  onClose,
}: IProps) => {
  return (
    <div className="content-absolute content-inset-0 content-z-[51] content-flex content-items-center content-justify-center content-bg-black content-bg-opacity-50">
      <div className="content-min-w-[400px] content-max-w-md content-rounded-lg content-bg-gradient-to-t content-from-[#12151E] content-to-[#324650] content-p-6 content-shadow-lg">
        <Border>
          <div className="content-p-4">
            <p className="content-mb-6" style={{ color: textColor }}>
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
        </Border>
      </div>
    </div>
  );
};

export default UserMiniPopup;

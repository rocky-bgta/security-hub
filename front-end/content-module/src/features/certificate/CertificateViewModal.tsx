import { CloseIcon, DownloadIcon } from 'assets/icons';
import { Button } from 'common/Button';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import UserCopyTextButton from 'components/UserCopyTextButton';
import { IUserCertificate } from 'models/Certificate';
import { useState } from 'react';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  data: IUserCertificate;
}
const CertificateViewModal = ({ isOpen, onClose, data }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const handleDownload = () => {
    setLoading(true);
    const link = document.createElement('a');
    link.href = FILE_PATH_PREFIX + data.certificateLink;
    link.setAttribute('download', 'Certificate.pdf');
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setLoading(false);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/5"
    >
      <ModalBody className="!content-p-0">
        <div className="content-bg-[linear-gradient(180deg,_#324650_0%,_#12151E_100%)]">
          <div className="content-flex content-items-center content-justify-between content-gap-4 content-p-6">
            <h2 className="content-text-2xl content-text-white">
              {data?.productName || 'N/A'}
            </h2>
            <div className="content-flex content-items-center content-justify-between content-gap-4">
              <UserCopyTextButton
                copyText={FILE_PATH_PREFIX + data.certificateLink}
              />
              <Button
                variant="outline"
                className="content-w-44 content-grow content-px-4 content-py-1.5"
                disabled={loading}
                onClick={() => handleDownload()}
              >
                <DownloadIcon fill="#37BE99" />
                {loading ? 'Downloading...' : 'Download'}
              </Button>
              <button onClick={onClose}>
                <CloseIcon className="content-size-7 content-text-white" />
              </button>
            </div>
          </div>
          <img
            src={FILE_PATH_PREFIX + data?.imageCertificateLink}
            alt={data?.productName}
          />
        </div>
      </ModalBody>
    </Modal>
  );
};
export default CertificateViewModal;

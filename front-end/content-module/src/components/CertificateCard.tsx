import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import { DownloadIcon } from 'assets/icons';
import { Button } from 'common/Button';
import Border from 'components/UserBorder';
import CertificateViewModal from 'features/certificate/CertificateViewModal';
import { IUserCertificate } from 'models/Certificate';
import { useSearchParams } from 'react-router-dom';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import UserCopyTextButton from './UserCopyTextButton';

interface IProps {
  data: IUserCertificate;
}

const CertificateCard = ({ data }: IProps) => {
  const [searchParams] = useSearchParams();
  const courseId = searchParams.get('courseId');
  const [modalOpen, setModalOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    if (courseId && courseId === data?.productId) {
      setModalOpen(true);
    }
  }, [courseId]);

  const handleDownload = () => {
    setLoading(true);

    const link = document.createElement('a');
    link.href = FILE_PATH_PREFIX + data.certificateLink;
    link.setAttribute('download', 'Certificate.pdf');
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  return (
    <Fragment>
      <Border>
        <div className="content-flex content-h-full content-flex-col content-justify-between content-p-1">
          <button onClick={() => setModalOpen(true)}>
            <img
              src={FILE_PATH_PREFIX + data?.imageCertificateLink}
              alt={data?.productName}
              className="content-w-full content-rounded-b-none content-rounded-t-md content-object-cover"
            />
          </button>
          <div className="content-p-3">
            <h2 className="content-mb-3 content-line-clamp-2 content-text-lg content-font-semibold content-text-cloudy-white">
              {data?.productName || 'N/A'}
            </h2>
            <div className="content-grid content-grid-cols-2 content-gap-3">
              <UserCopyTextButton
                copyText={FILE_PATH_PREFIX + data?.certificateLink}
              />

              <Button
                variant="outline"
                className="content-grow content-p-2"
                disabled={loading}
                onClick={() => handleDownload()}
              >
                <DownloadIcon fill="#37BE99" />
                {loading ? 'Downloading...' : 'Download'}
              </Button>
            </div>
          </div>
        </div>
      </Border>
      <CertificateViewModal
        isOpen={modalOpen}
        onClose={() => {
          setModalOpen(false);
          if (courseId === data?.productId) {
            searchParams.delete('courseId');
            const newUrl =
              window.location.pathname +
              (searchParams.toString() ? `?${searchParams.toString()}` : '');
            window.history.replaceState(null, '', newUrl);
          }
        }}
        data={data}
      />
    </Fragment>
  );
};

export default CertificateCard;

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface QRCodeModalProps {
  isOpen: boolean;
  onClose: () => void;
  couponCode: string;
  qrCodeUrl: string;
  onQRCodeGenerated: () => void;
}

const QRCodeModal: React.FC<QRCodeModalProps> = ({
  isOpen,
  onClose,
  couponCode,
  qrCodeUrl,
  onQRCodeGenerated,
}) => {
  const [qrCode, setQRCode] = useState('');
  const [loading, setLoading] = useState(false);

  const apiClient = useAPI();

  useEffect(() => {
    if (!qrCodeUrl) {
      handleGenerateQRCode();
    }
  }, []);

  const handleGenerateQRCode = async () => {
    setLoading(true);
    try {
      const response = await apiClient.post(
        API_END_POINTS.COUPON_GENERATE_QR_CODE + couponCode + '/qr',
      );
      setQRCode(response.data.qrCodeUrl);
      onQRCodeGenerated();
    } catch (error) {
      console.error('Error generating QR code:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle>QR Code - {couponCode}</DialogTitle>
        </DialogHeader>
        <div className="flex flex-col items-center space-y-4">
          <div className="rounded-lg bg-white p-4 shadow-sm">
            {loading ? (
              'Loading...'
            ) : (
              <img
                src={qrCodeUrl || qrCode}
                alt={`QR Code for ${couponCode}`}
                className="size-48 object-contain"
              />
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default QRCodeModal;

import { Dialog, DialogContent } from 'common/Dialog';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  data: string;
}
const ViewCertificateBackgroundModal = ({ isOpen, onClose, data }: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="home-max-h-[80vh] home-w-1/2 home-overflow-y-scroll">
        <div className="home-flex home-size-full home-items-center home-justify-center home-p-3">
          <img
            src={data}
            alt="Certificate Background"
            className="home-size-full"
          />
        </div>
      </DialogContent>
    </Dialog>
  );
};
export default ViewCertificateBackgroundModal;

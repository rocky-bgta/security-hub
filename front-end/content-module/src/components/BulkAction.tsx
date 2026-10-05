import { CloseIcon, DeleteIcon } from 'assets/icons';
import { Button } from 'common/Button';
import { BsFiletypeCsv } from 'react-icons/bs';
import { IoCheckmarkSharp } from 'react-icons/io5';

interface BulkActionProps {
  selectedIds: string[];
  onEnable: () => void;
  onDisable: () => void;
  onDelete: () => void;
  onExportCSV: () => void;
}

const BulkAction = ({
  selectedIds,
  onEnable,
  onDisable,
  onDelete,
  onExportCSV,
}: BulkActionProps) => {
  return (
    <div className="content-mb-6 content-rounded content-border content-border-card-border content-px-5 content-py-3 content-opacity-100 content-transition content-duration-500 content-ease-in">
      <p className="content-mb-3 content-font-medium content-text-cloudy-white">
        Bulk Actions ({selectedIds.length} selected)
      </p>
      <div className="content-flex content-gap-5">
        <Button
          className="content-w-40"
          variant="outline"
          size="sm"
          onClick={onEnable}
        >
          <IoCheckmarkSharp /> Enable
        </Button>
        <Button
          className="content-w-40"
          size="sm"
          variant="outline"
          onClick={onDisable}
        >
          <CloseIcon height={20} /> Disable
        </Button>
        <Button
          className="content-w-40"
          size="sm"
          variant="outline"
          onClick={onDelete}
        >
          <DeleteIcon fill="#2aa684" width={16} height={16} /> Delete
        </Button>
        <Button
          className="content-w-40"
          size="sm"
          variant="outline"
          onClick={onExportCSV}
        >
          <BsFiletypeCsv /> Export CSV
        </Button>
      </div>
    </div>
  );
};

export default BulkAction;

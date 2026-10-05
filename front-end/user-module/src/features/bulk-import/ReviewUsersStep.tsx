import { Button } from 'common/Button';
import { Card, CardContent } from 'common/Card';
import ImportUsersTable from 'features/bulk-import/ImportUsersTable';
import { IBulkImportUser } from 'models/BulkImport';
import { IList } from 'models/Global';

interface IProps {
  totalValid: number;
  totalInvalid: number;
  validUsers: IList<IBulkImportUser>;
  invalidUsers: IList<IBulkImportUser>;
  loadingValid: boolean;
  loadingInvalid: boolean;
  paginationKey: string;
  onCancel: () => void;
  onEditInvalid: () => void;
  onOnboard: () => void;
  onValidPageChange: (page: number) => void;
  onInvalidPageChange: (page: number) => void;
}

const ReviewUsersStep = ({
  totalValid,
  totalInvalid,
  validUsers,
  invalidUsers,
  loadingValid,
  loadingInvalid,
  paginationKey,
  onCancel,
  onEditInvalid,
  onOnboard,
  onValidPageChange,
  onInvalidPageChange,
}: IProps) => {
  return (
    <Card>
      <CardContent className="space-y-8 p-6">
        <ImportUsersTable
          title={`Valid Users (${totalValid})`}
          titleClassName="text-lg font-semibold text-green-500"
          users={validUsers}
          variant="valid"
          loading={loadingValid}
          paginationKey={`valid-${paginationKey}`}
          onPageChange={onValidPageChange}
        />

        {totalInvalid > 0 && (
          <ImportUsersTable
            title={`Invalid Users (${totalInvalid})`}
            titleClassName="text-lg font-semibold text-red-500"
            users={invalidUsers}
            variant="invalid"
            loading={loadingInvalid}
            paginationKey={`invalid-${paginationKey}`}
            onPageChange={onInvalidPageChange}
          />
        )}

        <div className="flex justify-end space-x-4 pt-2">
          <Button variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          {totalInvalid > 0 && (
            <Button variant="outline" onClick={onEditInvalid}>
              Edit Invalid Users
            </Button>
          )}
          <Button onClick={onOnboard} disabled={totalValid === 0}>
            Onboard Users
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default ReviewUsersStep;

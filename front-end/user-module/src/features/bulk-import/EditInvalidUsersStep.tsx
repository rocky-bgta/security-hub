import { Button } from 'common/Button';
import { Card, CardContent } from 'common/Card';
import ImportUsersTable from 'features/bulk-import/ImportUsersTable';
import { IBulkImportUser, IBulkImportUserDraft } from 'models/BulkImport';
import { IDropdownOption } from 'models/Dropdown';
import { IList } from 'models/Global';

interface IProps {
  totalValid: number;
  totalInvalid: number;
  invalidUsers: IList<IBulkImportUser>;
  loading: boolean;
  saving: boolean;
  drafts: Record<number, IBulkImportUserDraft>;
  departmentOptions: Array<IDropdownOption>;
  paginationKey: string;
  onDraftChange: (
    rowIndex: number,
    patch: Partial<IBulkImportUserDraft>,
  ) => void;
  onPageChange: (page: number) => void;
  onCancel: () => void;
  onSave: () => void;
}

const EditInvalidUsersStep = ({
  totalValid,
  totalInvalid,
  invalidUsers,
  loading,
  saving,
  drafts,
  departmentOptions,
  paginationKey,
  onDraftChange,
  onPageChange,
  onCancel,
  onSave,
}: IProps) => {
  return (
    <Card>
      <CardContent className="space-y-6 p-6">
        <h3 className="text-lg font-semibold text-green-500">
          Valid Users ({totalValid})
        </h3>

        <ImportUsersTable
          title={`Invalid Users (${totalInvalid})`}
          titleClassName="text-lg font-semibold text-red-500"
          users={invalidUsers}
          variant="invalid"
          editable
          showPhone
          loading={loading}
          drafts={drafts}
          departmentOptions={departmentOptions}
          paginationKey={`edit-invalid-${paginationKey}`}
          onDraftChange={onDraftChange}
          onPageChange={onPageChange}
        />

        <div className="flex items-center justify-between pt-2">
          <p className="text-sm text-red-500">
            {totalInvalid} invalid {totalInvalid === 1 ? 'user' : 'users'}
          </p>
          <div className="flex space-x-4">
            <Button variant="outline" onClick={onCancel} disabled={saving}>
              Cancel
            </Button>
            <Button onClick={onSave} disabled={saving || loading}>
              {saving ? 'Saving...' : 'Save Changes'}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default EditInvalidUsersStep;

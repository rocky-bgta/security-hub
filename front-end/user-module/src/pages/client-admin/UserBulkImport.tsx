import { Button } from 'common/Button';
import CompletionStep from 'features/bulk-import/CompletionStep';
import EditInvalidUsersStep from 'features/bulk-import/EditInvalidUsersStep';
import OnboardingProgressStep from 'features/bulk-import/OnboardingProgressStep';
import ReviewUsersStep from 'features/bulk-import/ReviewUsersStep';
import UploadStep from 'features/bulk-import/UploadStep';
import ValidationSummaryStep from 'features/bulk-import/ValidationSummaryStep';
import {
  BULK_IMPORT_PAGE_SIZE,
  createDraftFromUser,
  createEmptyUserPage,
  getBulkImportFileError,
  isBulkImportSessionError,
  isDraftDirty,
  normalizeUserPage,
  toUpdatePayload,
} from 'features/bulk-import/utils';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import {
  BulkImportUserStatus,
  BulkImportView,
  IBulkImportOnboardResult,
  IBulkImportSession,
  IBulkImportUser,
  IBulkImportUserDraft,
} from 'models/BulkImport';
import { IDropdownOption } from 'models/Dropdown';
import { IList } from 'models/Global';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { BULK_IMPORT_TEMPLATE_URL } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  hostPath: typeof routes;
}

const PAGE_TITLES: Record<BulkImportView, { title: string; subtitle: string }> =
  {
    upload: {
      title: 'Bulk Import Client Users',
      subtitle: 'Import multiple Client Users using CSV/Excel files',
    },
    summary: {
      title: 'Bulk Import Client Users',
      subtitle: 'Import multiple Client Users using CSV/Excel files',
    },
    review: {
      title: 'Bulk Import Client Users',
      subtitle: 'Review and confirm users before onboarding',
    },
    edit: {
      title: 'Bulk Import Client Users',
      subtitle: 'Please update the invalid users and click "Save Changes"',
    },
    processing: {
      title: 'Bulk Import Client Users',
      subtitle: 'Onboarding users, please wait...',
    },
    complete: {
      title: 'Bulk Import Completed',
      subtitle: "User onboarding completed, here's the summary.",
    },
  };

const usersUrl = (
  sessionId: string,
  query: Record<string, string | number>,
) => {
  const path = API_END_POINTS.BULK_IMPORT_USERS.replace(
    ':sessionId',
    sessionId,
  );
  const search = Object.entries(query)
    .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
    .join('&');
  return `${path}?${search}`;
};

const UserBulkImport = ({ hostPath = routes }: IProps) => {
  const { userInfo } = useStore();
  const navigate = useNavigate();
  const apiClient = useAPI();

  const [view, setView] = useState<BulkImportView>('upload');
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [templateLoading, setTemplateLoading] = useState(false);
  const [loadingValid, setLoadingValid] = useState(false);
  const [loadingInvalid, setLoadingInvalid] = useState(false);

  const [sessionId, setSessionId] = useState('');
  const [validationMessage, setValidationMessage] = useState('');
  const [totalValid, setTotalValid] = useState(0);
  const [totalInvalid, setTotalInvalid] = useState(0);
  const [validUsers, setValidUsers] = useState<IList<IBulkImportUser>>(
    createEmptyUserPage(),
  );
  const [invalidUsers, setInvalidUsers] = useState<IList<IBulkImportUser>>(
    createEmptyUserPage(),
  );
  const [originals, setOriginals] = useState<Record<number, IBulkImportUser>>(
    {},
  );
  const [drafts, setDrafts] = useState<Record<number, IBulkImportUserDraft>>(
    {},
  );
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );
  const [onboardResult, setOnboardResult] =
    useState<IBulkImportOnboardResult | null>(null);
  const [paginationResetKey, setPaginationResetKey] = useState(0);

  const mergeOriginals = (users: Array<IBulkImportUser>) => {
    setOriginals(prev => {
      const next = { ...prev };
      for (const user of users) {
        next[user.rowIndex] = user;
      }
      return next;
    });
  };

  const applySession = (data: IBulkImportSession) => {
    const validPage = normalizeUserPage(data.validUsers);
    const invalidPage = normalizeUserPage(data.invalidUsers);

    setSessionId(data.importSessionId);
    setTotalValid(data.totalValid ?? validPage.total);
    setTotalInvalid(data.totalInvalid ?? invalidPage.total);
    setValidUsers(validPage);
    setInvalidUsers(invalidPage);
    setOriginals(() => {
      const next: Record<number, IBulkImportUser> = {};
      for (const user of [...validPage.items, ...invalidPage.items]) {
        next[user.rowIndex] = user;
      }
      return next;
    });
    setDrafts({});
    setPaginationResetKey(key => key + 1);
  };

  const resetWizard = useCallback(() => {
    setView('upload');
    setSessionId('');
    setUploadedFile(null);
    setFileError('');
    setValidationMessage('');
    setTotalValid(0);
    setTotalInvalid(0);
    setValidUsers(createEmptyUserPage());
    setInvalidUsers(createEmptyUserPage());
    setOriginals({});
    setDrafts({});
    setOnboardResult(null);
    setSubmitting(false);
    setSaving(false);
    setLoadingValid(false);
    setLoadingInvalid(false);
    setPaginationResetKey(key => key + 1);
  }, []);

  const handleSessionOrError = (
    statusCode: number,
    message: string,
    fallbackView?: BulkImportView,
  ) => {
    toast.error(message);
    if (isBulkImportSessionError(statusCode, message)) {
      resetWizard();
      return true;
    }
    if (fallbackView) {
      setView(fallbackView);
    }
    return false;
  };

  useEffect(() => {
    const fetchDepartmentList = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.GET_DEPARTMENT_LIST +
            'active=true' +
            '&clientAdminId=' +
            userInfo.userId +
            '&isSystemDefined=true&pageSize=1000',
        );
        if (isSuccessResponse(response.statusCode)) {
          setDepartmentList(
            (response.data?.items || []).map((department: IDropdownOption) => ({
              id: department.name,
              name: department.name,
            })),
          );
        }
      } catch (error) {
        console.error('Error fetching department list:', error);
      }
    };

    fetchDepartmentList();
  }, [apiClient, userInfo.userId]);

  useEffect(() => {
    if (view !== 'processing') return;

    const onBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = '';
    };

    window.addEventListener('beforeunload', onBeforeUnload);
    return () => window.removeEventListener('beforeunload', onBeforeUnload);
  }, [view]);

  const handleDownloadTemplate = () => {
    setTemplateLoading(true);
    const link = document.createElement('a');
    link.href = BULK_IMPORT_TEMPLATE_URL;
    link.download = 'Bulk_user_import_template.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setTimeout(() => {
      setTemplateLoading(false);
    }, 1000);
  };

  const fetchUsers = async (status: BulkImportUserStatus, page: number) => {
    if (!sessionId) return;

    const offset = (page - 1) * BULK_IMPORT_PAGE_SIZE;
    const setLoading = status === 'VALID' ? setLoadingValid : setLoadingInvalid;
    setLoading(true);

    try {
      const response = await apiClient.get(
        usersUrl(sessionId, {
          status,
          offset,
          pageSize: BULK_IMPORT_PAGE_SIZE,
        }),
      );

      if (!isSuccessResponse(response.statusCode)) {
        handleSessionOrError(
          response.statusCode,
          response.message || 'Failed to load users.',
        );
        return;
      }

      const pageData = normalizeUserPage(
        response.data as IList<IBulkImportUser>,
      );
      if (status === 'VALID') {
        setValidUsers(pageData);
        setTotalValid(pageData.total);
      } else {
        setInvalidUsers(pageData);
        setTotalInvalid(pageData.total);
      }
      mergeOriginals(pageData.items);
    } catch {
      toast.error('Failed to load users. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleImportUser = async () => {
    const validationError = getBulkImportFileError(uploadedFile);
    if (validationError) {
      setFileError(validationError);
      return;
    }

    try {
      setSubmitting(true);
      setFileError('');

      const formData = new FormData();
      formData.append('file', uploadedFile!);
      formData.append('clientAdminId', userInfo.userId);
      formData.append('offset', '0');
      formData.append('pageSize', String(BULK_IMPORT_PAGE_SIZE));

      const response = await apiClient.post(
        API_END_POINTS.BULK_IMPORT_VALIDATE,
        {
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      );

      if (!isSuccessResponse(response.statusCode) || !response.data) {
        setFileError(
          response.message || 'Failed to upload file. Please try again.',
        );
        return;
      }

      applySession(response.data as IBulkImportSession);
      setValidationMessage(
        response.message || 'File uploaded successfully and validated.',
      );
      setView('summary');
    } catch {
      setFileError('Failed to upload file. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDraftChange = (
    rowIndex: number,
    patch: Partial<IBulkImportUserDraft>,
  ) => {
    setDrafts(prev => {
      const original = originals[rowIndex];
      const base =
        prev[rowIndex] ??
        (original
          ? createDraftFromUser(original)
          : {
              fullName: '',
              email: '',
              phoneNumber: '',
              department: '',
            });
      return { ...prev, [rowIndex]: { ...base, ...patch } };
    });
  };

  const handleSaveChanges = async () => {
    if (!sessionId) return;

    const dirtyRows = Object.keys(drafts)
      .map(Number)
      .filter(rowIndex => {
        const original = originals[rowIndex];
        const draft = drafts[rowIndex];
        return original && draft && isDraftDirty(original, draft);
      });

    if (dirtyRows.length === 0) {
      toast.info('No changes to save.');
      return;
    }

    try {
      setSaving(true);
      const response = await apiClient.put(
        usersUrl(sessionId, {
          offset: 0,
          pageSize: BULK_IMPORT_PAGE_SIZE,
        }),
        {
          data: {
            users: dirtyRows.map(rowIndex =>
              toUpdatePayload(originals[rowIndex], drafts[rowIndex]),
            ),
          },
        },
      );

      if (!isSuccessResponse(response.statusCode) || !response.data) {
        handleSessionOrError(
          response.statusCode,
          response.message || 'Failed to save changes.',
        );
        return;
      }

      applySession(response.data as IBulkImportSession);
      toast.success(
        response.message || 'Bulk import users updated successfully',
      );
      setView('review');
    } catch {
      toast.error('Failed to save changes. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  const handleEditInvalid = async () => {
    setDrafts({});
    if (invalidUsers.offset !== 0) {
      await fetchUsers('INVALID', 1);
    }
    setPaginationResetKey(key => key + 1);
    setView('edit');
  };

  const handleOnboard = async () => {
    if (!sessionId || totalValid === 0) return;

    setView('processing');

    try {
      const response = await apiClient.post(
        API_END_POINTS.BULK_IMPORT_ONBOARD.replace(':sessionId', sessionId),
      );

      if (!isSuccessResponse(response.statusCode) || !response.data) {
        handleSessionOrError(
          response.statusCode,
          response.message || 'Failed to onboard users.',
          'review',
        );
        return;
      }

      setOnboardResult(response.data as IBulkImportOnboardResult);
      setView('complete');
    } catch {
      toast.error('Failed to onboard users. Please try again.');
      setView('review');
    }
  };

  const { title, subtitle } = PAGE_TITLES[view];

  return (
    <div className="space-y-6">
      <div>
        <h2>{title}</h2>
        <p className="text-muted-foreground">{subtitle}</p>
      </div>

      {view === 'upload' && (
        <UploadStep
          uploadedFile={uploadedFile}
          error={fileError}
          submitting={submitting}
          templateLoading={templateLoading}
          onFileChange={setUploadedFile}
          onFileError={setFileError}
          onClear={() => {
            setUploadedFile(null);
            setFileError('');
          }}
          onImport={handleImportUser}
          onDownloadTemplate={handleDownloadTemplate}
        />
      )}

      {view === 'summary' && (
        <ValidationSummaryStep
          message={validationMessage}
          totalValid={totalValid}
          totalInvalid={totalInvalid}
          onCancel={resetWizard}
          onReview={() => setView('review')}
        />
      )}

      {view === 'review' && (
        <ReviewUsersStep
          totalValid={totalValid}
          totalInvalid={totalInvalid}
          validUsers={validUsers}
          invalidUsers={invalidUsers}
          loadingValid={loadingValid}
          loadingInvalid={loadingInvalid}
          paginationKey={String(paginationResetKey)}
          onCancel={resetWizard}
          onEditInvalid={handleEditInvalid}
          onOnboard={handleOnboard}
          onValidPageChange={page => fetchUsers('VALID', page)}
          onInvalidPageChange={page => fetchUsers('INVALID', page)}
        />
      )}

      {view === 'edit' && (
        <EditInvalidUsersStep
          totalValid={totalValid}
          totalInvalid={totalInvalid}
          invalidUsers={invalidUsers}
          loading={loadingInvalid}
          saving={saving}
          drafts={drafts}
          departmentOptions={departmentList}
          paginationKey={String(paginationResetKey)}
          onDraftChange={handleDraftChange}
          onPageChange={page => fetchUsers('INVALID', page)}
          onCancel={async () => {
            setDrafts({});
            await Promise.all([
              validUsers.offset !== 0
                ? fetchUsers('VALID', 1)
                : Promise.resolve(),
              invalidUsers.offset !== 0
                ? fetchUsers('INVALID', 1)
                : Promise.resolve(),
            ]);
            setPaginationResetKey(key => key + 1);
            setView('review');
          }}
          onSave={handleSaveChanges}
        />
      )}

      {view === 'processing' && (
        <OnboardingProgressStep totalUsers={totalValid + totalInvalid} />
      )}

      {view === 'complete' && onboardResult && (
        <CompletionStep
          result={onboardResult}
          onDone={() => navigate(hostPath.users.path)}
        />
      )}

      {view === 'complete' && !onboardResult && (
        <div className="flex justify-end">
          <Button onClick={() => navigate(hostPath.users.path)}>Done</Button>
        </div>
      )}
    </div>
  );
};

export default UserBulkImport;

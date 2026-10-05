import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { useAPI } from 'hooks/UseAPI';
import { useAuth } from 'hooks/UseAuth';
import { Download, FileText, Upload } from 'lucide-react';
import { IGetListParams } from 'models/Global';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { BULK_IMPORT_TEMPLATE_URL, InitGetListParams } from 'utils/Constants';
import { useStore } from 'hooks/UseStore';

interface IError {
  file?: string;
}

const ClientAdminBulkImport = ({ hostPath = routes }) => {
  const { userInfo } = useStore();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);

  const [error, setError] = useState<IError>({
    file: '',
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    clientAdminId: userInfo.userId,
  });

  const apiClient = useAPI();

  const handleDownloadTemplate = () => {

    const link = document.createElement('a');
    link.href =
      BULK_IMPORT_TEMPLATE_URL;
    link.download = 'Bulk_user_import_template.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

  };

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setUploadedFile(file);
      if (file) setError({});
    }
  };

  const handleImportUser = async () => {
    if (!uploadedFile) {
      setError({ file: 'Import file required' });
      return;
    }
    try {
      setSubmitting(true);
      setError({});

      const formData = new FormData();
      formData.append('file', uploadedFile);

      await apiClient.post(
        API_END_POINTS.IMPORT_END_USER + queryParams.clientAdminId,
        {
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      );

      navigate(hostPath.users.path);
    } catch (err) {
      setError({ file: 'Failed to upload file' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2>Bulk Import Client Users</h2>
        <p className="text-muted-foreground">
          Import multiple Client Users using CSV/Excel files
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Upload className="size-5" />
            Bulk Import Users
          </CardTitle>
          <CardDescription>
            Download template, fill in user details, and upload to import
            multiple users at once
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Template Download */}
          <div className="rounded-lg border border-card-border p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <FileText className="size-8 text-primary" />
                <div>
                  <h3 className="font-semibold text-card-foreground">
                    Download Template
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Download the CSV template with required columns
                  </p>
                </div>
              </div>
              <Button variant="outline" onClick={handleDownloadTemplate}>
                <Download className="mr-2 size-4" />
                Download Template
              </Button>
            </div>
          </div>

          {/* File Upload */}
          <div className="space-y-4">
            <Label htmlFor="file-upload">Upload CSV/Excel File *</Label>
            <div className="rounded-lg border-2 border-dashed border-card-border p-6 text-center">
              <div className="space-y-4">
                <Upload className="mx-auto size-12 text-muted-foreground" />
                <div>
                  <p className="mb-2 text-sm text-muted-foreground">
                    Drag and drop your file here, or click to browse
                  </p>
                  <Input
                    id="file-upload"
                    type="file"
                    accept=".csv"
                    onChange={handleFileUpload}
                    className="hidden"
                  />
                  <Button
                    variant="outline"
                    onClick={() =>
                      document.getElementById('file-upload')?.click()
                    }
                  >
                    Choose File
                  </Button>
                </div>
                {uploadedFile && (
                  <div className="text-sm text-foreground">
                    Selected: {uploadedFile.name}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end space-x-4 pt-4">
            <Button
              variant="outline"
              onClick={() => {
                setUploadedFile(null);
              }}
            >
              Clear
            </Button>
            <Button onClick={handleImportUser} disabled={submitting}>
              {submitting ? 'Processing...' : 'Import Users'}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientAdminBulkImport;

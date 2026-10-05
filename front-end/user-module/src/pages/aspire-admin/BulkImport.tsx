import { useEffect, useState } from 'react';

import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

import { Input } from 'common/Input';
import {
  CheckCircle,
  Download,
  FileText,
  Loader2,
  Search,
  Upload,
} from 'lucide-react';
import { routes } from 'routes/Routes';
import SearchSelect from 'components/SearchSelect';
import { IClientDropdownData } from 'models/Client';
import { IMSPDropdownData } from 'models/MSP';
import { ICountry } from 'models/Dropdown';
import { isSuccessResponse } from 'utils/Helper';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import { toast } from 'react-toastify';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { BULK_IMPORT_TEMPLATE_URL } from 'utils/Constants';

const AspireAdminBulkImport = () => {
  const navigate = useNavigate();
  const searchParams = useSearchParams();
  const clientAdminId = searchParams[0].get('clientAdminId');

  const apiClient = useAPI();
  const [countryList, setCountryList] = useState<Array<ICountry>>([]);
  const [selectedCountry, setSelectedCountry] = useState('');
  const [selectedMSP, setSelectedMSP] = useState('');
  const [selectedClient, setSelectedClient] = useState('');
  const [mspList, setMspList] = useState<Array<IMSPDropdownData>>([]);
  const [clientList, setClientList] = useState<Array<IClientDropdownData>>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);

  useEffect(() => {
    fetchCountryList();
  }, []);

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setCountryList(response.data);
      }
    } catch (error) {
      console.error('Error fetching country list:', error);
    }
  };

  useEffect(() => {
    if (selectedCountry) {
      fetchMspList();
    }
  }, [selectedCountry]);

  const fetchMspList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.MSP_LIST_BY_COUNTRY +
          `country=${selectedCountry}&isClient=false`,
      );
      if (isSuccessResponse(response.statusCode)) {
        setMspList(response.data);
      }
    } catch (error) {
      console.error('Error fetching MSP list:', error);
    }
  };

  useEffect(() => {
    if (selectedMSP) {
      fetchClientList();
    }
  }, [selectedMSP]);

  const fetchClientList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_LIST_BY_MSP + `mspId=${selectedMSP}`,
      );
      if (isSuccessResponse(response.statusCode)) {
        setClientList(
          response.data.map((client: IClientDropdownData) => ({
            id: client.id,
            organizationName: client.organizationName,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching client list:', error);
    }
  };

  const handleDownloadTemplate = () => {
    setLoading(true);

    const link = document.createElement('a');
    link.href =
      BULK_IMPORT_TEMPLATE_URL;
    link.download = 'Bulk_user_import_template.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setUploadedFile(file);
      //   toast({
      //     title: 'File Selected',
      //     description: `${file.name} has been selected for upload.`,
      //   });
    }
  };

  const handleImportUser = async () => {
    if (!uploadedFile) {
      return toast.error('Import file required');
    }
    if (!clientAdminId && !selectedClient) {
      return toast.error('Please select a client');
    }
    try {
      setSubmitting(true);

      const formData = new FormData();
      formData.append('file', uploadedFile);

      await apiClient.post(API_END_POINTS.IMPORT_END_USER + selectedClient, {
        data: formData,
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      toast.success('Users imported successfully');
      if (clientAdminId) {
        navigate(routes.clientUserList.path.replace(':id', clientAdminId));
      } else {
        navigate(routes.clientUserList.path.replace(':id', selectedClient));
      }
    } catch (err) {
      toast.error('Error importing users');
      console.error('Error importing users:', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Bulk Import Client Users
        </h1>
        <p className="text-muted-foreground">
          Import multiple Client Users using CSV/Excel files
        </p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Upload className="size-5" />
              Bulk Import Users
            </CardTitle>
            <Button variant="outline" onClick={handleDownloadTemplate}>
              <Download className="mr-2 size-4" />
              Download Template
            </Button>
          </div>
          <CardDescription>
            Select client admin, then upload your CSV/Excel file to import
            multiple users at once
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* MSP Country Selection */}
          {/* <div className="space-y-2">
            <Label htmlFor="msp-country-select">Select MSP Country</Label>

            <SearchSelect
              value={selectedCountry}
              onValueChange={setSelectedCountry}
              placeholder="Search and select MSP Country..."
              items={countryList.map(country => ({
                value: country.id,
                label: country.name,
              }))}
            />
          </div> */}
          {/* MSP Selection */}
          {/* <div className="space-y-2">
            <Label htmlFor="msp-select">Select MSP</Label>
            <SearchSelect
              value={selectedMSP}
              onValueChange={setSelectedMSP}
              placeholder="Search and select MSP..."
              items={mspList.map(msp => ({
                value: msp.id,
                label: msp.organizationName,
              }))}
              disabled={!selectedCountry}
            />
          </div> */}

          {/* Client Admin Selection */}
          {/* <div className="space-y-2">
            <Label htmlFor="client-select">Select Client Admin</Label>
            <SearchSelect
              value={selectedClient}
              onValueChange={setSelectedClient}
              disabled={!selectedMSP}
              placeholder="Search and select Client Admin..."
              items={clientList.map(client => ({
                value: client.id,
                label: client.organizationName,
              }))}
            />
          </div> */}

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

export default AspireAdminBulkImport;

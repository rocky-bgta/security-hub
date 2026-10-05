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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { CheckCircle, Database, Download, Search, Upload } from 'lucide-react';
import { useState } from 'react';

const BulkImport = () => {
  const [selectedProduct, setSelectedProduct] = useState('');
  const [selectedExam, setSelectedExam] = useState('');
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [importStatus, setImportStatus] = useState<
    'idle' | 'uploading' | 'processing' | 'success' | 'error'
  >('idle');

  const handleDownloadTemplate = () => {
    // toast({
    //   title: 'Template Downloaded',
    //   description: 'CSV template file has been downloaded successfully.',
    // });
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

  const handleImport = () => {
    if (!selectedProduct || !selectedExam || !uploadedFile) {
      //   toast({
      //     title: 'Error',
      //     description: 'Please select product, exam, and upload a file.',
      //     variant: 'destructive',
      //   });
      return;
    }

    setImportStatus('processing');

    // Simulate import process
    setTimeout(() => {
      setImportStatus('success');
      //   toast({
      //     title: 'Import Successful',
      //     description: 'Questions have been successfully imported to the exam.',
      //   });
    }, 2000);
  };

  return (
    <div className="content-space-y-6">
      <div>
        <h1 className="content-text-3xl content-font-bold content-text-foreground">
          Bulk Import Questions
        </h1>
        <p className="content-text-muted-foreground">
          Import multiple questions using CSV/Excel files
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <Database className="content-size-5" />
            Bulk Question Import
          </CardTitle>
          <CardDescription>
            Select product and exam, then upload your CSV/Excel file to import
            questions
          </CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-6">
          {/* Product and Exam Selection */}
          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
            <div className="content-space-y-2">
              <Label htmlFor="product-select">Select Product *</Label>
              <div className="content-relative">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Select
                  value={selectedProduct}
                  onValueChange={setSelectedProduct}
                >
                  <SelectTrigger className="content-pl-10">
                    <SelectValue placeholder="Choose a product..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="mathematics">Mathematics</SelectItem>
                    <SelectItem value="science">Science</SelectItem>
                    <SelectItem value="history">History</SelectItem>
                    <SelectItem value="english">English</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="exam-select">Select Exam *</Label>
              <div className="content-relative">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Select
                  value={selectedExam}
                  onValueChange={setSelectedExam}
                  disabled={!selectedProduct}
                >
                  <SelectTrigger className="content-pl-10">
                    <SelectValue placeholder="Choose an exam..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="algebra-fundamentals">
                      Algebra Fundamentals
                    </SelectItem>
                    <SelectItem value="physics-basics">
                      Physics Basics
                    </SelectItem>
                    <SelectItem value="world-war-ii">World War II</SelectItem>
                    <SelectItem value="grammar-basics">
                      Grammar Basics
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Template Download */}
          <div className="content-rounded-lg content-border content-border-card-border content-p-4">
            <div className="content-flex content-items-center content-justify-between">
              <div className="content-flex content-items-center content-gap-3">
                <Download className="content-size-8 content-text-primary" />
                <div>
                  <p className="content-font-semibold content-text-white">
                    Download Template
                  </p>
                  <p className="content-text-sm content-text-muted-foreground">
                    Download the CSV/Excel template with required columns
                  </p>
                  <div className="content-mt-1 content-text-xs content-text-muted-foreground">
                    <p>
                      Columns: Question Text, Answer Options, Correct Answer,
                      Question Type, Status
                    </p>
                  </div>
                </div>
              </div>
              <Button variant="outline" onClick={handleDownloadTemplate}>
                <Download className="content-mr-2 content-size-4" />
                Download Template
              </Button>
            </div>
          </div>

          {/* File Upload */}
          <div className="content-space-y-4">
            <Label htmlFor="file-upload">Upload CSV/Excel File *</Label>
            <div className="content-rounded-lg content-border-2 content-border-dashed content-border-muted-foreground/25 content-p-6 content-text-center">
              <div className="content-space-y-4">
                <Upload className="content-mx-auto content-size-12 content-text-muted-foreground" />
                <div>
                  <p className="content-mb-2 content-text-sm content-text-muted-foreground">
                    Drag and drop your file here, or click to browse
                  </p>
                  <p className="content-mb-4 content-text-xs content-text-muted-foreground">
                    Supports CSV and Excel files (.csv, .xlsx, .xls)
                  </p>
                  <Input
                    id="file-upload"
                    type="file"
                    accept=".csv,.xlsx,.xls"
                    onChange={handleFileUpload}
                    className="content-hidden"
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
                  <div className="content-text-sm content-text-foreground">
                    Selected: {uploadedFile.name}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Template Information */}
          <div className="content-rounded-lg content-border content-border-card-border content-p-4">
            <h4 className="content-mb-2 content-font-semibold content-text-primary">
              Template Format Information
            </h4>
            <div className="content-space-y-1 content-text-sm content-text-primary">
              <p>
                <strong>Question Text:</strong> Required - The actual question
                text
              </p>
              <p>
                <strong>Answer Options:</strong> Required for MCQ - Separate
                options with semicolons (;)
              </p>
              <p>
                <strong>Correct Answer:</strong> Required - The correct answer
                text
              </p>
              <p>
                <strong>Question Type:</strong> Required - Multiple Choice,
                True/False, Yes/No, Fill in the Blank, Short Answer
              </p>
              <p>
                <strong>Status:</strong> Required - Active or Inactive
              </p>
            </div>
          </div>

          {/* Import Status */}
          {importStatus === 'success' && (
            <div className="content-rounded-lg content-border content-border-green-200 content-bg-green-50 content-p-4">
              <div className="content-flex content-items-center content-gap-3">
                <CheckCircle className="content-size-6 content-text-green-600" />
                <div>
                  <h3 className="content-font-semibold content-text-green-800">
                    Import Successful
                  </h3>
                  <p className="content-text-sm content-text-green-700">
                    45 questions imported successfully to the selected exam.
                  </p>
                  <div className="content-mt-2 content-text-xs content-text-green-600">
                    <p>• 35 Multiple Choice questions</p>
                    <p>• 8 True/False questions</p>
                    <p>• 2 Short Answer questions</p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Validation Results */}
          {importStatus === 'success' && (
            <div className="content-rounded-lg content-border content-p-4">
              <h4 className="content-mb-3 content-font-semibold">
                Import Summary
              </h4>
              <div className="content-grid content-grid-cols-3 content-gap-4 content-text-center">
                <div>
                  <div className="content-text-2xl content-font-bold content-text-green-600">
                    45
                  </div>
                  <div className="content-text-sm content-text-green-700">
                    Successfully Imported
                  </div>
                </div>
                <div>
                  <div className="content-text-2xl content-font-bold content-text-yellow-600">
                    3
                  </div>
                  <div className="content-text-sm content-text-yellow-700">
                    Warnings
                  </div>
                </div>
                <div>
                  <div className="content-text-2xl content-font-bold content-text-red-600">
                    2
                  </div>
                  <div className="content-text-sm content-text-red-700">
                    Failed
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="content-flex content-justify-end content-space-x-4 content-pt-4">
            <Button
              variant="outline"
              onClick={() => {
                setSelectedProduct('');
                setSelectedExam('');
                setUploadedFile(null);
                setImportStatus('idle');
              }}
            >
              Clear
            </Button>
            <Button
              onClick={handleImport}
              disabled={importStatus === 'processing'}
            >
              {importStatus === 'processing'
                ? 'Processing...'
                : 'Import Questions'}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default BulkImport;

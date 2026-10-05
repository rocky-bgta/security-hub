import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Calendar as CalendarComponent } from 'common/Calendar';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { format } from 'date-fns';
import {
  Calendar,
  Download,
  Eye,
  FileText,
  Filter,
  Search,
} from 'lucide-react';
import { useState } from 'react';
import { DateRange } from 'react-day-picker';
import { cn } from 'utils/Helper';

interface ExamineeReport {
  id: string;
  examineeName: string;
  examName: string;
  totalAttempts: number;
  score: number;
  status: 'Passed' | 'Failed';
  attemptDateTime: string;
  timeTaken: string;
  questionwiseAnswers: { question: string; answer: string; correct: boolean }[];
}

const mockExamineeData: ExamineeReport[] = [
  {
    id: '1',
    examineeName: 'John Doe',
    examName: 'Mathematics - Algebra',
    totalAttempts: 2,
    score: 85,
    status: 'Passed',
    attemptDateTime: '2024-01-15 10:30:00',
    timeTaken: '45 minutes',
    questionwiseAnswers: [
      { question: 'What is 2+2?', answer: '4', correct: true },
      { question: 'What is 5*3?', answer: '15', correct: true },
      { question: 'What is 10/2?', answer: '6', correct: false },
    ],
  },
  {
    id: '2',
    examineeName: 'Jane Smith',
    examName: 'Science - Physics',
    totalAttempts: 1,
    score: 72,
    status: 'Passed',
    attemptDateTime: '2024-01-14 14:15:00',
    timeTaken: '38 minutes',
    questionwiseAnswers: [
      { question: 'What is gravity?', answer: 'Force', correct: true },
      { question: 'Speed of light?', answer: '300000 km/s', correct: true },
    ],
  },
  {
    id: '3',
    examineeName: 'Bob Johnson',
    examName: 'History - World War II',
    totalAttempts: 3,
    score: 55,
    status: 'Failed',
    attemptDateTime: '2024-01-13 16:45:00',
    timeTaken: '52 minutes',
    questionwiseAnswers: [
      { question: 'When did WWII start?', answer: '1940', correct: false },
      { question: 'Who was the leader?', answer: 'Hitler', correct: true },
    ],
  },
];

const ExamineeReports = () => {
  const [reports, setReports] = useState(mockExamineeData);
  const [searchTerm, setSearchTerm] = useState('');
  const [examFilter, setExamFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [dateRange, setDateRange] = useState<DateRange | undefined>();
  const [selectedReport, setSelectedReport] = useState<ExamineeReport | null>(
    null,
  );

  const filteredReports = reports.filter(report => {
    const matchesSearch =
      report.examineeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      report.examName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesExam =
      examFilter === 'all' ||
      report.examName.toLowerCase().includes(examFilter.toLowerCase());
    const matchesStatus =
      statusFilter === 'all' ||
      report.status.toLowerCase() === statusFilter.toLowerCase();
    return matchesSearch && matchesExam && matchesStatus;
  });

  const getStatusBadge = (status: 'Passed' | 'Failed') => {
    return status === 'Passed' ? (
      <Badge>Passed</Badge>
    ) : (
      <Badge variant="destructive">Failed</Badge>
    );
  };

  return (
    <div className="content-space-y-6">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-foreground">
            Examinee Data & Reports
          </h1>
          <p className="content-text-muted-foreground">
            View and analyze examinee performance data and generate reports
          </p>
        </div>
        <Button variant="outline">
          <Download className="content-mr-2 content-size-4" />
          Export Reports
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <FileText className="content-size-5" />
            Examinee Performance Data
          </CardTitle>
          <CardDescription>
            Track examinee attempts, scores, and detailed performance metrics
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Filters */}
          <div className="content-mb-6 content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2 lg:content-grid-cols-5">
            <div className="content-space-y-2">
              <Label htmlFor="search">Search</Label>
              <div className="content-relative">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Input
                  id="search"
                  placeholder="Search examinees or exams..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                  className="content-pl-10"
                />
              </div>
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="exam-filter">Exam</Label>
              <Select value={examFilter} onValueChange={setExamFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All exams" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Exams</SelectItem>
                  <SelectItem value="mathematics">Mathematics</SelectItem>
                  <SelectItem value="science">Science</SelectItem>
                  <SelectItem value="history">History</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="status-filter">Status</Label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Statuses</SelectItem>
                  <SelectItem value="passed">Passed</SelectItem>
                  <SelectItem value="failed">Failed</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="content-space-y-2">
              <Label>Date Range</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      'content-w-full content-justify-start content-text-left content-font-normal',
                      !dateRange && 'content-text-muted-foreground',
                    )}
                  >
                    <Calendar className="content-mr-2 content-size-4" />
                    {dateRange?.from ? (
                      dateRange.to ? (
                        <>
                          {format(dateRange.from, 'LLL dd')} -{' '}
                          {format(dateRange.to, 'LLL dd')}
                        </>
                      ) : (
                        format(dateRange.from, 'LLL dd, y')
                      )
                    ) : (
                      <span>Select a date</span>
                    )}
                  </Button>
                </PopoverTrigger>
                <PopoverContent
                  className="content-w-auto content-p-0"
                  align="start"
                >
                  <CalendarComponent
                    initialFocus
                    mode="range"
                    defaultMonth={dateRange?.from}
                    selected={dateRange}
                    onSelect={setDateRange}
                    numberOfMonths={2}
                    className={cn('p-3 pointer-events-auto')}
                  />
                </PopoverContent>
              </Popover>
            </div>

            <div className="content-flex content-items-end content-space-y-2">
              <Button
                variant="outline"
                className="content-w-full"
                onClick={() => {
                  setSearchTerm('');
                  setExamFilter('all');
                  setStatusFilter('all');
                  setDateRange(undefined);
                }}
              >
                <Filter className="content-mr-2 content-size-4" />
                Clear
              </Button>
            </div>
          </div>

          {/* Reports Table */}

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Examinee Name</TableHead>
                <TableHead>Exam Name</TableHead>
                <TableHead>Attempts</TableHead>
                <TableHead>Score</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Date & Time</TableHead>
                <TableHead>Duration</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredReports.map(report => (
                <TableRow key={report.id}>
                  <TableCell className="content-font-medium">
                    {report.examineeName}
                  </TableCell>
                  <TableCell>{report.examName}</TableCell>
                  <TableCell>{report.totalAttempts}</TableCell>
                  <TableCell>
                    <Badge
                      variant={report.score >= 70 ? 'default' : 'secondary'}
                    >
                      {report.score}%
                    </Badge>
                  </TableCell>
                  <TableCell>{getStatusBadge(report.status)}</TableCell>
                  <TableCell className="content-text-sm">
                    {report.attemptDateTime}
                  </TableCell>
                  <TableCell>{report.timeTaken}</TableCell>
                  <TableCell>
                    <Dialog>
                      <DialogTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setSelectedReport(report)}
                        >
                          <Eye className="content-size-4" />
                        </Button>
                      </DialogTrigger>
                      <DialogContent className="content-max-w-2xl">
                        <DialogHeader>
                          <DialogTitle>Detailed Report</DialogTitle>
                          <DialogDescription>
                            Complete performance analysis for{' '}
                            {selectedReport?.examineeName}
                          </DialogDescription>
                        </DialogHeader>
                        {selectedReport && (
                          <div className="content-space-y-4">
                            <div className="content-grid content-grid-cols-2 content-gap-4">
                              <div>
                                <Label>Examinee</Label>
                                <p className="content-font-medium content-text-white">
                                  {selectedReport.examineeName}
                                </p>
                              </div>
                              <div>
                                <Label>Exam</Label>
                                <p className="content-font-medium content-text-white">
                                  {selectedReport.examName}
                                </p>
                              </div>
                              <div>
                                <Label>Total Attempts</Label>
                                <p className="content-font-medium content-text-white">
                                  {selectedReport.totalAttempts}
                                </p>
                              </div>
                              <div>
                                <Label>Score</Label>
                                <p className="content-font-medium content-text-white">
                                  {selectedReport.score}%
                                </p>
                              </div>
                              <div>
                                <Label className="content-mr-2">Status</Label>
                                {getStatusBadge(selectedReport.status)}
                              </div>
                              <div>
                                <Label>Time Taken</Label>
                                <p className="content-font-medium content-text-white">
                                  {selectedReport.timeTaken}
                                </p>
                              </div>
                            </div>

                            <div>
                              <Label>Question-wise Answers</Label>
                              <div className="content-mt-2 content-space-y-2">
                                {selectedReport.questionwiseAnswers.map(
                                  (qa, index) => (
                                    <div
                                      key={index}
                                      className="content-rounded content-border content-p-3"
                                    >
                                      <p className="content-text-sm content-font-medium content-text-white">
                                        {qa.question}
                                      </p>
                                      <p className="content-mt-1 content-text-sm content-text-muted-foreground">
                                        Answer: {qa.answer}
                                      </p>
                                      <Badge
                                        variant={
                                          qa.correct ? 'default' : 'destructive'
                                        }
                                        className="content-mt-1"
                                      >
                                        {qa.correct ? 'Correct' : 'Incorrect'}
                                      </Badge>
                                    </div>
                                  ),
                                )}
                              </div>
                            </div>
                          </div>
                        )}
                      </DialogContent>
                    </Dialog>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {filteredReports.length === 0 && (
            <div className="content-py-8 content-text-center content-text-muted-foreground">
              No examinee data found matching your criteria.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default ExamineeReports;

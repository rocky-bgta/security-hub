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
import { Switch } from 'common/Switch';
import { useState } from 'react';

import { Calendar as CalendarComponent } from 'common/Calendar';
import { Popover, PopoverContent, PopoverTrigger } from 'common/Popover';
import { format } from 'date-fns';
import { Calendar, Save, Settings } from 'lucide-react';
import { cn } from 'utils/Helper';

const ExamSettings = () => {
  const [examSettings, setExamSettings] = useState({
    title: '',
    product: '',
    topic: '',
    numberOfQuestions: '',
    selectionMode: '',
    difficultyFilter: '',
    duration: '',
    passPercentage: '',
    shufflingEnabled: false,
    showAnswersAfterSubmit: false,
    attemptsAllowed: '',
    status: 'draft',
  });
  const [startDate, setStartDate] = useState<Date>();
  const [endDate, setEndDate] = useState<Date>();

  const handleInputChange = (field: string, value: string | boolean) => {
    setExamSettings(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const saveSettings = () => {
    if (!examSettings.title || !examSettings.product || !examSettings.topic) {
      //   toast({
      //     title: 'Error',
      //     description: 'Please fill in all required fields.',
      //     variant: 'destructive',
      //   });
      return;
    }

    // toast({
    //   title: 'Settings Saved',
    //   description: 'Exam settings have been saved successfully.',
    // });
  };

  const publishExam = () => {
    if (!examSettings.title || !examSettings.product || !examSettings.topic) {
      //   toast({
      //     title: 'Error',
      //     description: 'Please complete all required fields before publishing.',
      //     variant: 'destructive',
      //   });
      return;
    }

    setExamSettings(prev => ({ ...prev, status: 'published' }));
    // toast({
    //   title: 'Exam Published',
    //   description: 'Exam has been published and is now active for users.',
    // });
  };

  return (
    <div className="content-space-y-6">
      <div>
        <h1 className="content-text-3xl content-font-bold content-text-foreground">
          Exam Settings
        </h1>
        <p className="content-text-muted-foreground">
          Configure and manage exam settings
        </p>
      </div>

      {/* Basic Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="content-flex content-items-center content-gap-2">
            <Settings className="content-size-5" />
            Basic Settings
          </CardTitle>
          <CardDescription>
            Configure exam title, product, and topic
          </CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
            <div className="content-space-y-2">
              <Label htmlFor="exam-title">Exam Title *</Label>
              <Input
                id="exam-title"
                placeholder="Enter exam title..."
                value={examSettings.title}
                onChange={e => handleInputChange('title', e.target.value)}
              />
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="product">Select Product *</Label>
              <Select
                value={examSettings.product}
                onValueChange={value => handleInputChange('product', value)}
              >
                <SelectTrigger>
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

            <div className="content-space-y-2">
              <Label htmlFor="topic">Select Topic *</Label>
              <Select
                value={examSettings.topic}
                onValueChange={value => handleInputChange('topic', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose a topic..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="algebra">Algebra</SelectItem>
                  <SelectItem value="geometry">Geometry</SelectItem>
                  <SelectItem value="calculus">Calculus</SelectItem>
                  <SelectItem value="physics">Physics</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="questions">Number of Questions *</Label>
              <Input
                id="questions"
                type="number"
                placeholder="e.g., 25"
                value={examSettings.numberOfQuestions}
                onChange={e =>
                  handleInputChange('numberOfQuestions', e.target.value)
                }
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Question Selection */}
      <Card>
        <CardHeader>
          <CardTitle>Question Selection</CardTitle>
          <CardDescription>
            Configure how questions are selected for the exam
          </CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
            <div className="content-space-y-2">
              <Label htmlFor="selection-mode">Selection Mode *</Label>
              <Select
                value={examSettings.selectionMode}
                onValueChange={value =>
                  handleInputChange('selectionMode', value)
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose selection mode..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="auto">
                    Auto - System selects questions
                  </SelectItem>
                  <SelectItem value="manual">
                    Manual - Admin selects questions
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>

            {examSettings.selectionMode === 'auto' && (
              <div className="content-space-y-2">
                <Label htmlFor="difficulty">Difficulty Filter</Label>
                <Select
                  value={examSettings.difficultyFilter}
                  onValueChange={value =>
                    handleInputChange('difficultyFilter', value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Choose difficulty..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="easy">Easy</SelectItem>
                    <SelectItem value="medium">Medium</SelectItem>
                    <SelectItem value="hard">Hard</SelectItem>
                    <SelectItem value="mixed">Mixed</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Exam Configuration */}
      <Card>
        <CardHeader>
          <CardTitle>Exam Configuration</CardTitle>
          <CardDescription>
            Set duration, pass percentage, and other exam parameters
          </CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-3">
            <div className="content-space-y-2">
              <Label htmlFor="duration">Duration (minutes) *</Label>
              <Input
                id="duration"
                type="number"
                placeholder="e.g., 60"
                value={examSettings.duration}
                onChange={e => handleInputChange('duration', e.target.value)}
              />
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="pass-percentage">Pass Percentage *</Label>
              <Input
                id="pass-percentage"
                type="number"
                placeholder="e.g., 70"
                min="0"
                max="100"
                value={examSettings.passPercentage}
                onChange={e =>
                  handleInputChange('passPercentage', e.target.value)
                }
              />
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="attempts">Number of Attempts *</Label>
              <Select
                value={examSettings.attemptsAllowed}
                onValueChange={value =>
                  handleInputChange('attemptsAllowed', value)
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose attempts..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">1 Attempt</SelectItem>
                  <SelectItem value="2">2 Attempts</SelectItem>
                  <SelectItem value="3">3 Attempts</SelectItem>
                  <SelectItem value="unlimited">Unlimited</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="content-grid content-grid-cols-1 content-gap-6 md:content-grid-cols-2">
            <div className="content-flex content-items-center content-space-x-2">
              <Switch
                id="shuffling"
                checked={examSettings.shufflingEnabled}
                onCheckedChange={checked =>
                  handleInputChange('shufflingEnabled', checked)
                }
              />
              <Label htmlFor="shuffling">Enable Question Shuffling</Label>
            </div>

            <div className="content-flex content-items-center content-space-x-2">
              <Switch
                id="show-answers"
                checked={examSettings.showAnswersAfterSubmit}
                onCheckedChange={checked =>
                  handleInputChange('showAnswersAfterSubmit', checked)
                }
              />
              <Label htmlFor="show-answers">Show Answers After Submit</Label>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Schedule Settings */}
      <Card>
        <CardHeader>
          <CardTitle>Schedule Settings</CardTitle>
          <CardDescription>
            Set start and end times for the exam (optional)
          </CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
            <div className="content-space-y-2">
              <Label>Start Date & Time</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      'content-w-full content-justify-start content-text-left content-font-normal',
                      !startDate && 'text-muted-foreground',
                    )}
                  >
                    <Calendar className="content-mr-2 content-size-4" />
                    {startDate ? (
                      format(startDate, 'PPP')
                    ) : (
                      <span>Pick start date</span>
                    )}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="content-w-auto content-p-0">
                  <CalendarComponent
                    mode="single"
                    selected={startDate}
                    onSelect={setStartDate}
                    initialFocus
                    className={cn('content-pointer-events-auto content-p-3')}
                  />
                </PopoverContent>
              </Popover>
            </div>

            <div className="content-space-y-2">
              <Label>End Date & Time</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn(
                      'content-w-full content-justify-start content-text-left content-font-normal',
                      !endDate && 'text-muted-foreground',
                    )}
                  >
                    <Calendar className="content-mr-2 content-size-4" />
                    {endDate ? (
                      format(endDate, 'PPP')
                    ) : (
                      <span>Pick end date</span>
                    )}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="content-w-auto content-p-0">
                  <CalendarComponent
                    mode="single"
                    selected={endDate}
                    onSelect={setEndDate}
                    className={cn('content-pointer-events-auto content-p-3')}
                  />
                </PopoverContent>
              </Popover>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Status and Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Exam Status</CardTitle>
          <CardDescription>
            Set the exam status and save your settings
          </CardDescription>
        </CardHeader>
        <CardContent className="content-space-y-4">
          <div className="content-space-y-2">
            <Label htmlFor="status">Current Status</Label>
            <Select
              value={examSettings.status}
              onValueChange={value => handleInputChange('status', value)}
            >
              <SelectTrigger className="content-w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="draft">Draft</SelectItem>
                <SelectItem value="published">Published</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="content-flex content-gap-4">
            <Button onClick={saveSettings}>
              <Save className="content-mr-2 content-size-4" />
              Save as Draft
            </Button>
            <Button onClick={publishExam} variant="default">
              Publish Exam
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ExamSettings;

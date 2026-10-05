import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  BarChart3,
  CheckCircle,
  Clock,
  Download,
  HelpCircle,
  Target,
  TrendingUp,
  Users,
} from 'lucide-react';
import { useState } from 'react';

const Analytics = () => {
  const [timeRange, setTimeRange] = useState('30');

  const analyticsData = {
    mostAttemptedExams: [
      { name: 'Mathematics - Algebra', attempts: 245 },
      { name: 'Science - Physics', attempts: 198 },
      { name: 'History - World War II', attempts: 156 },
      { name: 'English - Grammar', attempts: 142 },
      { name: 'Chemistry - Organic', attempts: 134 },
    ],
    averageScores: [
      { name: 'Mathematics - Algebra', score: 78.5 },
      { name: 'Science - Physics', score: 82.1 },
      { name: 'History - World War II', score: 69.3 },
      { name: 'English - Grammar', score: 85.7 },
      { name: 'Chemistry - Organic', score: 71.2 },
    ],
    passRates: [
      { name: 'Mathematics - Algebra', rate: 74.5 },
      { name: 'Science - Physics', rate: 81.2 },
      { name: 'History - World War II', rate: 65.8 },
      { name: 'English - Grammar', rate: 89.3 },
      { name: 'Chemistry - Organic', rate: 68.9 },
    ],
    mostMissedQuestions: [
      {
        question: 'What is the quadratic formula?',
        misses: 89,
        exam: 'Algebra',
      },
      {
        question: 'Calculate the derivative of x²',
        misses: 76,
        exam: 'Calculus',
      },
      { question: 'When did WWII end?', misses: 65, exam: 'History' },
      { question: 'Define photosynthesis', misses: 58, exam: 'Biology' },
      { question: "What is Newton's second law?", misses: 52, exam: 'Physics' },
    ],
    difficultySuccess: [
      { level: 'Easy', rate: 87.2, color: 'content-bg-green-500' },
      { level: 'Medium', rate: 72.8, color: 'content-bg-yellow-500' },
      { level: 'Hard', rate: 58.4, color: 'content-bg-red-500' },
    ],
    completionRates: [
      { name: 'Mathematics - Algebra', rate: 92.3 },
      { name: 'Science - Physics', rate: 89.7 },
      { name: 'History - World War II', rate: 85.4 },
      { name: 'English - Grammar', rate: 94.1 },
      { name: 'Chemistry - Organic', rate: 78.9 },
    ],
  };

  // Mock heatmap data
  const heatmapData = [
    {
      day: 'Mon',
      '9am': 12,
      '10am': 18,
      '11am': 25,
      '12pm': 15,
      '1pm': 8,
      '2pm': 22,
      '3pm': 28,
      '4pm': 20,
      '5pm': 10,
    },
    {
      day: 'Tue',
      '9am': 15,
      '10am': 22,
      '11am': 30,
      '12pm': 18,
      '1pm': 12,
      '2pm': 25,
      '3pm': 32,
      '4pm': 24,
      '5pm': 14,
    },
    {
      day: 'Wed',
      '9am': 18,
      '10am': 28,
      '11am': 35,
      '12pm': 22,
      '1pm': 16,
      '2pm': 30,
      '3pm': 38,
      '4pm': 28,
      '5pm': 18,
    },
    {
      day: 'Thu',
      '9am': 20,
      '10am': 25,
      '11am': 32,
      '12pm': 20,
      '1pm': 14,
      '2pm': 28,
      '3pm': 35,
      '4pm': 26,
      '5pm': 16,
    },
    {
      day: 'Fri',
      '9am': 16,
      '10am': 20,
      '11am': 28,
      '12pm': 18,
      '1pm': 10,
      '2pm': 24,
      '3pm': 30,
      '4pm': 22,
      '5pm': 12,
    },
  ];

  const getHeatmapIntensity = (value: number) => {
    if (value >= 30) return 'content-bg-blue-600';
    if (value >= 20) return 'content-bg-blue-400';
    if (value >= 10) return 'content-bg-blue-200';
    return 'content-bg-blue-100';
  };

  return (
    <div className="content-space-y-6">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-foreground">
            Analytics & Insights
          </h1>
          <p className="content-text-muted-foreground">
            Comprehensive exam performance analytics and insights
          </p>
        </div>
        <div className="content-flex content-gap-2">
          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger className="content-w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Last 7 days</SelectItem>
              <SelectItem value="30">Last 30 days</SelectItem>
              <SelectItem value="90">Last 90 days</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline">
            <Download className="content-mr-2 content-size-4" />
            Export
          </Button>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="content-grid content-grid-cols-1 content-gap-6 md:content-grid-cols-2 lg:content-grid-cols-4">
        <Card>
          <CardHeader className="content-flex !content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Total Exams
            </CardTitle>
            <BarChart3 className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="content-text-2xl content-font-bold content-text-white">
              127
            </div>
            <p className="content-text-xs content-text-muted-foreground">
              <span className="content-text-green-600">+12%</span> from last
              period
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="content-flex !content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Total Attempts
            </CardTitle>
            <Users className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="content-text-2xl content-font-bold content-text-white">
              2,847
            </div>
            <p className="content-text-xs content-text-muted-foreground">
              <span className="content-text-green-600">+8.2%</span> from last
              period
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="content-flex !content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Avg Completion Rate
            </CardTitle>
            <Target className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="content-text-2xl content-font-bold content-text-white">
              88.1%
            </div>
            <p className="content-text-xs content-text-muted-foreground">
              <span className="content-text-green-600">+2.4%</span> from last
              period
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="content-flex !content-flex-row content-items-center content-justify-between content-space-y-0 content-pb-2">
            <CardTitle className="content-text-sm content-font-medium">
              Avg Duration
            </CardTitle>
            <Clock className="content-size-4 content-text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="content-text-2xl content-font-bold content-text-white">
              42m
            </div>
            <p className="content-text-xs content-text-muted-foreground">
              <span className="content-text-yellow-600">-1.2%</span> from last
              period
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts Row 1 */}
      <div className="content-grid content-grid-cols-1 content-gap-6 lg:content-grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="content-flex content-items-center content-gap-2">
              <TrendingUp className="content-size-5" />
              Most Attempted Exams
            </CardTitle>
            <CardDescription className="content-text-xs content-text-muted-foreground">
              Top 5 exams by attempt count
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {analyticsData.mostAttemptedExams.map((exam, index) => (
                <div
                  key={index}
                  className="content-flex content-items-center content-justify-between"
                >
                  <div className="content-flex-1">
                    <p className="content-text-sm content-font-medium content-text-white">
                      {exam.name}
                    </p>
                    <div className="content-mt-1 content-h-2 content-w-full content-rounded-full content-bg-secondary">
                      <div
                        className="content-h-2 content-rounded-full content-bg-primary"
                        style={{ width: `${(exam.attempts / 245) * 100}%` }}
                      />
                    </div>
                  </div>
                  <Badge
                    variant="outline"
                    className="content-ml-4 content-text-white"
                  >
                    {exam.attempts}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="content-flex content-items-center content-gap-2">
              <Target className="content-size-5" />
              Average Scores
            </CardTitle>
            <CardDescription className="content-text-xs content-text-muted-foreground">
              Performance metrics by exam
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {analyticsData.averageScores.map((exam, index) => (
                <div
                  key={index}
                  className="content-flex content-items-center content-justify-between"
                >
                  <div className="content-flex-1">
                    <p className="content-text-sm content-font-medium content-text-white">
                      {exam.name}
                    </p>
                    <div className="content-mt-1 content-h-2 content-w-full content-rounded-full content-bg-secondary">
                      <div
                        className={`content-h-2 content-rounded-full ${exam.score >= 80 ? 'content-bg-green-500' : exam.score >= 70 ? 'content-bg-yellow-500' : 'content-bg-red-500'}`}
                        style={{ width: `${exam.score}%` }}
                      />
                    </div>
                  </div>
                  <Badge
                    variant="outline"
                    className="content-ml-4 content-text-white"
                  >
                    {exam.score}%
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts Row 2 */}
      <div className="content-grid content-grid-cols-1 content-gap-6 lg:content-grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="content-flex content-items-center content-gap-2">
              <CheckCircle className="content-size-5" />
              Pass Rates
            </CardTitle>
            <CardDescription className="content-text-xs content-text-muted-foreground">
              Success rates by exam
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {analyticsData.passRates.map((exam, index) => (
                <div
                  key={index}
                  className="content-flex content-items-center content-justify-between"
                >
                  <div className="content-flex-1">
                    <p className="content-text-sm content-font-medium content-text-white">
                      {exam.name}
                    </p>
                    <div className="content-mt-1 content-h-2 content-w-full content-rounded-full content-bg-secondary">
                      <div
                        className={`content-h-2 content-rounded-full ${exam.rate >= 80 ? 'content-bg-green-500' : exam.rate >= 70 ? 'content-bg-yellow-500' : 'content-bg-red-500'}`}
                        style={{ width: `${exam.rate}%` }}
                      />
                    </div>
                  </div>
                  <Badge
                    variant="outline"
                    className="content-ml-4 content-text-white"
                  >
                    {exam.rate}%
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="content-flex content-items-center content-gap-2">
              <HelpCircle className="content-size-5" />
              Most Missed Questions
            </CardTitle>
            <CardDescription className="content-text-xs content-text-muted-foreground">
              Questions with highest incorrect rate
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {analyticsData.mostMissedQuestions.map((question, index) => (
                <div key={index} className="content-space-y-2">
                  <div className="content-flex content-items-start content-justify-between">
                    <p className="content-flex-1 content-text-sm content-font-medium content-text-white">
                      {question.question}
                    </p>
                    <Badge variant="destructive" className="content-ml-2">
                      {question.misses}
                    </Badge>
                  </div>
                  <p className="content-text-xs content-text-muted-foreground">
                    From: {question.exam}
                  </p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Difficulty Analysis & Heatmap */}
      <div className="content-grid content-grid-cols-1 content-gap-6 lg:content-grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="content-text-base content-font-semibold">
              Difficulty-wise Success Rate
            </CardTitle>
            <CardDescription className="content-text-xs content-text-muted-foreground">
              Performance by question difficulty
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-4">
              {analyticsData.difficultySuccess.map((difficulty, index) => (
                <div key={index} className="content-space-y-2">
                  <div className="content-flex content-items-center content-justify-between">
                    <span className="content-text-sm content-font-medium content-text-white">
                      {difficulty.level}
                    </span>
                    <span className="content-text-sm content-font-bold content-text-white">
                      {difficulty.rate}%
                    </span>
                  </div>
                  <div className="content-h-3 content-w-full content-rounded-full content-bg-secondary">
                    <div
                      className={`content-h-3 content-rounded-full ${difficulty.color}`}
                      style={{ width: `${difficulty.rate}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="content-text-base content-font-semibold">
              Attempts Heatmap
            </CardTitle>
            <CardDescription className="content-text-xs content-text-muted-foreground">
              Peak usage times throughout the week
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="content-space-y-2">
              <div className="content-grid content-grid-cols-10 content-gap-1 content-text-xs">
                <div></div>
                {[
                  '9am',
                  '10am',
                  '11am',
                  '12pm',
                  '1pm',
                  '2pm',
                  '3pm',
                  '4pm',
                  '5pm',
                ].map(time => (
                  <div
                    key={time}
                    className="content-text-center content-font-medium content-text-white"
                  >
                    {time}
                  </div>
                ))}
              </div>
              {heatmapData.map((row, index) => (
                <div
                  key={index}
                  className="content-grid content-grid-cols-10 content-gap-1"
                >
                  <div className="content-flex content-items-center content-text-xs content-font-medium content-text-white">
                    {row.day}
                  </div>
                  {Object.entries(row)
                    .slice(1)
                    .map(([time, value]) => (
                      <div
                        key={time}
                        className={`content-h-6 content-rounded ${getHeatmapIntensity(value as number)} content-flex content-items-center content-justify-center content-text-xs content-font-medium content-text-white`}
                        title={`${row.day} ${time}: ${value} attempts`}
                      >
                        {value}
                      </div>
                    ))}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Analytics;

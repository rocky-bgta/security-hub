import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';

import { useEffect, useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import {
  ITopicAssignmentReportData,
  ITopicProgressItem,
} from 'models/ProductPackageReports';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

const StatCard = ({
  label,
  value,
  color,
}: {
  label: string;
  value: string | number;
  color?: string;
}) => (
  <Card>
    <CardContent className="p-4">
      <div className={`text-2xl font-bold ${color || ''}`}>{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{label}</div>
    </CardContent>
  </Card>
);

const TopicAssignmentReport = () => {
  const apiClient = useAPI();
  const [summary, setSummary] = useState({
    totalTopics: 0,
    activeAssignments: 0,
    completedTopics: 0,
    avgProgress: 0,
  });
  const [topics, setTopics] = useState<ITopicProgressItem[]>([]);

  useEffect(() => {
    const fetchTopicAssignmentReport = async () => {
      try {
        const response: IResponse<ITopicAssignmentReportData> =
          await apiClient.get(API_END_POINTS.GET_TOPIC_ASSIGNMENT_REPORT);

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message || 'API error');
        }

        const data = response.data;
        setSummary({
          totalTopics: data.totalTopics,
          activeAssignments: data.activeAssignments,
          completedTopics: data.completedTopics,
          avgProgress: data.avgProgress,
        });
        setTopics(data.topics || []);
      } catch (error) {
        console.error('Error fetching topic assignment report:', error);
      }
    };

    fetchTopicAssignmentReport();
  }, [apiClient]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label="Total Topics" value={summary.totalTopics} />
        <StatCard
          label="Active Assignments"
          value={summary.activeAssignments.toLocaleString()}
        />
        <StatCard
          label="Completed"
          value={summary.completedTopics.toLocaleString()}
          color="text-emerald-400"
        />
        <StatCard
          label="Avg Progress"
          value={`${summary.avgProgress}%`}
          color="text-primary"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Topic Progress</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {topics.map((topic, index) => (
              <div key={`${topic.topic}-${index}`} className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="font-medium">{topic.topic}</span>
                  <span className="text-muted-foreground">
                    {topic.completed}/{topic.assigned} completed
                  </span>
                </div>
                <Progress value={topic.avgProgress} className="h-2" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default TopicAssignmentReport;

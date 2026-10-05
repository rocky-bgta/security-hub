import NoDataText from 'common/NoDataText';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import Border from 'components/UserBorder';
import CourseTableLoader from 'components/skeleton/CourseTable';
import { useAPI } from 'hooks/UseAPI';
import { IList, IResponse } from 'models/Global';
import { IUserTranscript } from 'models/Users';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formatDateAndTime, objectToQueryString } from 'utils/Helper';

const Transcripts = () => {
  const [transcripts, setTranscripts] = useState<IUserTranscript[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    fetchTranscriptList();
  }, []);

  const fetchTranscriptList = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<IUserTranscript>> = await apiClient.get(
        API_END_POINTS.USER_TRANSCRIPT_LIST +
          objectToQueryString({
            offset: 0,
            pageSize: 20,
          }),
      );

      setTranscripts(response.data.items);
    } catch (error) {
      console.error('Error fetching course details:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {loading ? (
        <CourseTableLoader />
      ) : (
        <Border className="content-h-full">
          <div className="content-p-4 sm:content-p-6">
            <div className="content-flex content-items-center content-justify-between">
              <h2 className="content-text-lg content-font-semibold content-text-white sm:content-text-xl">
                Transcript Summary
              </h2>
            </div>

            <div className="content-mt-3 content-pr-1 lg:content-max-h-[340px] lg:content-overflow-auto">
              <div className="content-overflow-hidden content-rounded-2xl content-border content-border-card-border">
                <Table className="!content-border-none">
                  <TableHeader>
                    <TableRow>
                      <TableHead>Topic Name</TableHead>
                      <TableHead>Completed Date</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {transcripts?.length > 0 ? (
                      <>
                        {transcripts?.map((transcript, index) => (
                          <TableRow key={index}>
                            <TableCell>{transcript.topicName}</TableCell>
                            <TableCell>
                              {transcript.completionDate
                                ? formatDateAndTime(transcript.completionDate)
                                : 'N/A'}
                            </TableCell>
                          </TableRow>
                        ))}
                      </>
                    ) : (
                      <TableRow>
                        <TableCell colSpan={2}>
                          <NoDataText
                            text="No courses found"
                            className="content-p-4 content-text-center"
                          />
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
                {/* <table className="content-min-w-full content-text-white">
                  <thead className="content-bg-white content-bg-opacity-25">
                    <tr className="content-text-xs content-uppercase content-text-white">
                      <th className="content-px-3 content-py-3 content-text-left">
                        TOPICS NAME
                      </th>
                      <th className="content-py-3 content-text-left">
                        Completed date
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {courses?.length > 0 ? (
                      <>
                        {courses?.map((course, index) => (
                          <tr
                            key={index}
                            className="text-sm content-border-t content-border-card-border"
                          >
                            <td className="content-px-3 content-py-4">
                              {course.topicName}
                            </td>
                            <td className="content-px-1 content-py-4">
                              {course.publishDate
                                ? dayjs(course.publishDate).format(
                                    'YYYY-MM-DD HH:mm:ss',
                                  )
                                : 'N/A'}
                            </td>
                          </tr>
                        ))}
                      </>
                    ) : (
                      <tr>
                        <td colSpan={2}>
                          <NoDataText
                            text="No courses found"
                            className="content-p-4 content-text-center"
                          />
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table> */}
              </div>
            </div>
          </div>
        </Border>
      )}
    </div>
  );
};

export default Transcripts;

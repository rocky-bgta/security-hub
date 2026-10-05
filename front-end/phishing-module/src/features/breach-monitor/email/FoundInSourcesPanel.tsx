import {
  Share2,
  FolderOpen,
  Code2,
  FileText,
  Send,
  Hash,
  ShoppingCart,
  Database,
  Bug,
  Search,
} from 'lucide-react';
import { useEffect, useState } from 'react';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { useAPI } from 'hooks/UseAPI';
import {
  IGetListParams,
  IList,
  InitGetListParams,
  IResponse,
} from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';
import Pagination from 'common/Pagination';

const icons = {
  alinet: <Share2 className="size-4" />,
  dark_web_forums: <FolderOpen className="size-4" />,
  dark_web_apis: <Code2 className="size-4" />,
  paste_sites: <FileText className="size-4" />,
  telegram_channels: <Send className="size-4" />,
  irc_chat_rooms: <Hash className="size-4" />,
  underground_marketplaces: <ShoppingCart className="size-4" />,
  breach_databases: <Database className="size-4" />,
  malware_repositories: <Bug className="size-4" />,
};

const FoundInSourcesPanel = () => {
  const [breachedEmailSources, setBreachedEmailSources] = useState<
    IList<string>
  >({
    items: [],
    total: 0,
    pageSize: 0,
    offset: 0,
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
  });

  const apiclient = useAPI();

  useEffect(() => {
    const fetchBreachedEmailSources = async () => {
      try {
        const response: IResponse<IList<string>> = await apiclient.get(
          API_END_POINTS.GET_BREACH_MONITOR_BREACHED_EMAIL_SOURCES +
            objectToQueryString(queryParams),
        );
        setBreachedEmailSources(response.data);

        console.log(response.data);
      } catch (error) {
        console.error('Error fetching breached email sources:', error);
      }
    };

    fetchBreachedEmailSources();
  }, [apiclient, queryParams]);

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const getIconForSource = (source: string) => {
    const key = source.toLowerCase().replace(/\s+/g, '_');
    return icons[key as keyof typeof icons] || <Code2 className="size-4" />;
  };

  return (
    <Card>
      <CardHeader>
        <div>
          <CardTitle className="text-xl">Found In</CardTitle>
          <p className="text-sm text-muted-foreground">Multiple Sources</p>
        </div>
      </CardHeader>
      <CardContent>
        {breachedEmailSources.total === 0 && (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="mb-4 rounded-full bg-muted p-4">
              <Search className="size-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold">No breaches detected</h3>
          </div>
        )}

        {breachedEmailSources.total > 0 && (
          <>
            <div className="grid grid-cols-2 gap-3">
              {breachedEmailSources.items.map((source, i) => (
                <div
                  key={`${source}-${i}`}
                  className="flex items-center gap-3 rounded-lg border border-card-border bg-muted/50 px-4 py-3"
                >
                  <div className="rounded-md bg-primary/10 p-1.5 text-primary">
                    {getIconForSource(source)}
                  </div>
                  <span className="text-sm font-semibold">{source}</span>
                </div>
              ))}
            </div>
            {breachedEmailSources?.total > 0 && (
              <div className="my-6 flex justify-end">
                <Pagination
                  total={breachedEmailSources?.total || 0}
                  perPage={breachedEmailSources?.pageSize || 0}
                  onPageChange={onPageChangeHandler}
                />
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default FoundInSourcesPanel;

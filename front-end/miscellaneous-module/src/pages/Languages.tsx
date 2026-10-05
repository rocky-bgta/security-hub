import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import ActionLanguages from 'features/languages/ActionLanguages';
import DeleteLanguage from 'features/languages/DeleteLanguages';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { ILanguage } from 'models/Languages';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const Languages = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<ILanguage>>([]);
  const [selectedLanguage, setSelectedLanguage] = useState<ILanguage | null>(
    null,
  );
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<ILanguage>> = await apiClient.get(
        API_END_POINTS.GET_LANGUAGE_LIST,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching language data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitLanguage = (language: ILanguage) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, language];
      } else {
        return [...prevData.map(r => (r.id === language.id ? language : r))];
      }
    });

    setSelectedLanguage(null);
    setActionType(null);
  };

  const handleDeleteLanguage = () => {
    toast.success(
      `Language ${selectedLanguage?.displayName} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedLanguage?.id),
    ]);
    setSelectedLanguage(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Languages Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Language
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Language List
          </CardTitle>
          <CardDescription>View and manage all languages</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Language Display Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Language Code
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Status
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Created
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Updated
                </TableHead>
                <TableHead className="text-center font-semibold text-foreground">
                  Actions
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading || data.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={5}
                    className="text-center text-muted-foreground"
                  >
                    {loading ? 'Loading...' : 'No languages found'}
                  </TableCell>
                </TableRow>
              ) : (
                data?.map(language => (
                  <TableRow key={language.id}>
                    <TableCell className="font-medium text-foreground">
                      {language.displayName}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {language.code}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={language.active ? 'default' : 'secondary'}
                      >
                        {language.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(language.createdAt as string)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(language.updatedAt as string)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedLanguage(language);
                            setActionType('edit');
                          }}
                          className="hover:bg-muted"
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedLanguage(language);
                            setShowDeleteDialog(true);
                          }}
                          className="hover:bg-destructive/20 hover:text-destructive"
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <ActionLanguages
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedLanguage(null);
        }}
        language={selectedLanguage}
        onSubmit={handleSubmitLanguage}
      />

      {selectedLanguage && (
        <DeleteLanguage
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          language={selectedLanguage}
          onDeleteLanguage={handleDeleteLanguage}
        />
      )}
    </div>
  );
};

export default Languages;

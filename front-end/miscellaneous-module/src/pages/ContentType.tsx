import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

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
import ActionContentType from 'features/content-type/ActionContentType';
import DeleteContentType from 'features/content-type/DeleteContentType';
import { useAPI } from 'hooks/UseAPI';
import { IContentType } from 'models/ContentType';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';

const ContentType = () => {
  const [data, setData] = useState<Array<IContentType>>([]);

  const [selectedContentType, setSelectedContentType] =
    useState<IContentType | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    fetchContentTypeData();
  }, []);

  const fetchContentTypeData = async () => {
    try {
      const response: IResponse<Array<IContentType>> = await apiClient.get(
        API_END_POINTS.GET_CONTENT_TYPE_LIST,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching content type data:', error);
    }
  };

  const handleSubmitContentType = (contentType: IContentType) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, contentType];
      } else {
        return [
          ...prevData.map(r => (r.id === contentType.id ? contentType : r)),
        ];
      }
    });

    setSelectedContentType(null);
    setActionType(null);
  };

  const handleDeleteContentType = () => {
    toast.success(
      `Content Type ${selectedContentType?.typeName} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedContentType?.id),
    ]);
    setSelectedContentType(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Content Type Management
          </h1>
          <p className="text-muted-foreground">
            Manage content types and their properties
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Content Type
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Content Type List
          </CardTitle>
          <CardDescription>View and manage all content types</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border border-card-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-semibold text-foreground">
                    Content Type Name
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Description
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Sort Order
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Created
                  </TableHead>
                  <TableHead className="text-center font-semibold text-foreground">
                    Actions
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={5}
                      className="text-center text-muted-foreground"
                    >
                      No content types found
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map(contentType => (
                    <TableRow key={contentType.id}>
                      <TableCell className="font-medium text-foreground">
                        {contentType.typeName}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {contentType.description}
                      </TableCell>
                      <TableCell>{contentType.sortOrder}</TableCell>
                      <TableCell className="text-muted-foreground">
                        {new Date(
                          contentType.createdAt as string,
                        ).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedContentType(contentType);
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
                              setSelectedContentType(contentType);
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
          </div>
        </CardContent>
      </Card>

      <ActionContentType
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedContentType(null);
        }}
        contentType={selectedContentType}
        onSubmit={handleSubmitContentType}
      />

      {selectedContentType && (
        <DeleteContentType
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          contentType={selectedContentType}
          onDeleteContentType={handleDeleteContentType}
        />
      )}
    </div>
  );
};

export default ContentType;

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
import ActionCategory from 'features/category/ActionCategory';
import DeleteCategory from 'features/category/DeleteCategory';
import { useAPI } from 'hooks/UseAPI';
import { ICategory } from 'models/Category';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';

const Category = () => {
  const [data, setData] = useState<Array<ICategory>>([]);
  const [selectedCategory, setSelectedCategory] = useState<ICategory | null>(
    null,
  );
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response: IResponse<Array<ICategory>> = await apiClient.get(
          API_END_POINTS.GET_CATEGORY_LIST,
        );
        setData(response.data);
      } catch (error) {
        console.error('Error fetching category data:', error);
      }
    };

    fetchData();
  }, []);

  const handleSubmitCategory = (category: ICategory) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, category];
      } else {
        return [...prevData.map(r => (r.id === category.id ? category : r))];
      }
    });

    setSelectedCategory(null);
    setActionType(null);
  };

  const handleDeleteCategory = () => {
    toast.success(
      `Category ${selectedCategory?.categoryName} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedCategory?.id),
    ]);
    setSelectedCategory(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Category Management
          </h1>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Category
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Category List
          </CardTitle>
          <CardDescription>View and manage all categories</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border border-card-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-semibold text-foreground">
                    Category Name
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Description
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Sort Order
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Status
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
                      No categories found
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map(category => (
                    <TableRow key={category.id}>
                      <TableCell className="font-medium text-foreground">
                        {category.categoryName}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {category.description}
                      </TableCell>
                      <TableCell>{category.sortOrder}</TableCell>
                      <TableCell>
                        <Badge
                          variant={category.active ? 'default' : 'secondary'}
                          className={
                            category.active ? 'bg-primary text-white' : ''
                          }
                        >
                          {category.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {new Date(
                          category.createdAt as string,
                        ).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedCategory(category);
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
                              setSelectedCategory(category);
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

      <ActionCategory
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedCategory(null);
        }}
        category={selectedCategory}
        onSubmit={handleSubmitCategory}
      />

      {selectedCategory && (
        <DeleteCategory
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          category={selectedCategory}
          onDeleteCategory={handleDeleteCategory}
        />
      )}
    </div>
  );
};

export default Category;

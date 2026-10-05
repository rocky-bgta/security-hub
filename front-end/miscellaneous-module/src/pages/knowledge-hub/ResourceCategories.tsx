import { Edit, Plus, Power, Trash2 } from 'lucide-react';
import { FormEvent, useState } from 'react';
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
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import { Textarea } from 'common/Textarea';

// Mock data for categories
const mockCategories = [
  {
    id: 'CAT-001',
    name: 'Getting Started',
    description: 'Resources for beginners',
    sortOrder: 1,
    resourceCount: 15,
    status: 'Active' as const,
  },
  {
    id: 'CAT-002',
    name: 'Advanced Learning',
    description: 'Advanced topics and tutorials',
    sortOrder: 2,
    resourceCount: 23,
    status: 'Active' as const,
  },
  {
    id: 'CAT-003',
    name: 'Instructor Help',
    description: 'Resources for instructors',
    sortOrder: 3,
    resourceCount: 8,
    status: 'Inactive' as const,
  },
];

// Mock data for tags
const mockTags = [
  {
    id: 'TAG-001',
    name: 'Security',
    usageCount: 45,
    status: 'Active' as const,
  },
  {
    id: 'TAG-002',
    name: 'Video',
    usageCount: 23,
    status: 'Active' as const,
  },
  {
    id: 'TAG-003',
    name: 'Assessment Tips',
    usageCount: 12,
    status: 'Inactive' as const,
  },
];

const ResourceCategories = () => {
  const [newCategory, setNewCategory] = useState({
    name: '',
    description: '',
    sortOrder: '',
  });

  const [newTag, setNewTag] = useState({
    name: '',
  });

  const getStatusBadge = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return <Badge className="">{status}</Badge>;
      case 'completed':
        return <Badge className="">{status}</Badge>;
      case 'pending':
        return <Badge className="">{status}</Badge>;
      case 'overdue':
        return <Badge className="">{status}</Badge>;
      case 'failed':
        return <Badge className="">{status}</Badge>;
      case 'suspended':
        return <Badge className="">{status}</Badge>;
      case 'inactive':
        return (
          <Badge className="bg-muted text-muted-foreground">{status}</Badge>
        );
      default:
        return <Badge className="">{status}</Badge>;
    }
  };

  const handleCreateCategory = (e: FormEvent) => {
    e.preventDefault();

    if (!newCategory.name || !newCategory.sortOrder) {
      toast.error('Please fill in all required fields.');
      return;
    }

    toast.success('Category created successfully!');
    setNewCategory({ name: '', description: '', sortOrder: '' });
  };

  const handleCreateTag = (e: FormEvent) => {
    e.preventDefault();

    if (!newTag.name) {
      toast.error('Please enter a tag name.');
      return;
    }

    toast.success('Tag created successfully!');
    setNewTag({ name: '' });
  };

  const handleEdit = (type: string, id: string) => {};

  const handleDelete = (type: string, id: string) => {
    toast.success(`${type} Deleted`);
  };

  const handleStatusToggle = (
    type: string,
    id: string,
    currentStatus: string,
  ) => {
    toast.success(
      `${type} ${currentStatus === 'Active' ? 'deactivated' : 'activated'} successfully.`,
    );
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Categories & Tags Management
        </h1>
        <p className="text-muted-foreground">
          Manage categories and tags for organizing resources
        </p>
      </div>

      <Tabs defaultValue="categories" className="space-y-6">
        <TabsList>
          <TabsTrigger value="categories">Categories</TabsTrigger>
          <TabsTrigger value="tags">Tags</TabsTrigger>
        </TabsList>

        <TabsContent value="categories" className="space-y-6">
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <Card className="lg:col-span-1">
              <CardHeader>
                <CardTitle>Create New Category</CardTitle>
                <CardDescription>
                  Add a new category for organizing resources
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleCreateCategory} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="categoryName">Category Name *</Label>
                    <Input
                      id="categoryName"
                      value={newCategory.name}
                      onChange={e =>
                        setNewCategory(prev => ({
                          ...prev,
                          name: e.target.value,
                        }))
                      }
                      placeholder="e.g., Getting Started"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="categoryDescription">Description</Label>
                    <Textarea
                      id="categoryDescription"
                      value={newCategory.description}
                      onChange={e =>
                        setNewCategory(prev => ({
                          ...prev,
                          description: e.target.value,
                        }))
                      }
                      placeholder="Brief description of the category"
                      rows={3}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="sortOrder">Sort Order *</Label>
                    <Input
                      id="sortOrder"
                      type="number"
                      value={newCategory.sortOrder}
                      onChange={e =>
                        setNewCategory(prev => ({
                          ...prev,
                          sortOrder: e.target.value,
                        }))
                      }
                      placeholder="1"
                      required
                    />
                  </div>

                  <Button type="submit" className="w-full">
                    <Plus className="mr-2 size-4" />
                    Create Category
                  </Button>
                </form>
              </CardContent>
            </Card>

            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle>Categories List</CardTitle>
                <CardDescription>Manage existing categories</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Description</TableHead>
                      <TableHead>Resources</TableHead>
                      <TableHead>Order</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mockCategories.map(category => (
                      <TableRow key={category.id}>
                        <TableCell>
                          <div className="font-medium">{category.name}</div>
                        </TableCell>
                        <TableCell>{category.description}</TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {category.resourceCount}
                          </Badge>
                        </TableCell>
                        <TableCell>{category.sortOrder}</TableCell>
                        <TableCell>{getStatusBadge(category.status)}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                handleEdit('Category', category.id)
                              }
                            >
                              <Edit className="size-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                handleStatusToggle(
                                  'Category',
                                  category.id,
                                  category.status,
                                )
                              }
                            >
                              <Power className="size-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                handleDelete('Category', category.id)
                              }
                            >
                              <Trash2 className="size-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="tags" className="space-y-6">
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <Card className="lg:col-span-1">
              <CardHeader>
                <CardTitle>Create New Tag</CardTitle>
                <CardDescription>
                  Add a new tag for labeling resources
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleCreateTag} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="tagName">Tag Name *</Label>
                    <Input
                      id="tagName"
                      value={newTag.name}
                      onChange={e =>
                        setNewTag(prev => ({ ...prev, name: e.target.value }))
                      }
                      placeholder="e.g., Security"
                      required
                    />
                  </div>

                  <Button type="submit" className="w-full">
                    <Plus className="mr-2 size-4" />
                    Create Tag
                  </Button>
                </form>
              </CardContent>
            </Card>

            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle>Tags List</CardTitle>
                <CardDescription>Manage existing tags</CardDescription>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Usage Count</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mockTags.map(tag => (
                      <TableRow key={tag.id}>
                        <TableCell>
                          <Badge variant="secondary">{tag.name}</Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">{tag.usageCount}</Badge>
                        </TableCell>
                        <TableCell>{getStatusBadge(tag.status)}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEdit('Tag', tag.id)}
                            >
                              <Edit className="size-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                handleStatusToggle('Tag', tag.id, tag.status)
                              }
                            >
                              <Power className="size-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDelete('Tag', tag.id)}
                            >
                              <Trash2 className="size-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default ResourceCategories;

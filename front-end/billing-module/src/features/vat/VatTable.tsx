import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from 'common/AlertDialog';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import SpinnerLoader from 'common/loader/SpinnerLoader';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { Edit, Eye, Trash2 } from 'lucide-react';
import { VatConfiguration } from 'models/Vat';

interface VatTableProps {
  loading: boolean;
  data: VatConfiguration[];
  onEdit: (vat: VatConfiguration) => void;
  onDelete: (vatId: string) => void;
  onViewDetails: (vat: VatConfiguration) => void;
}

export const VatTable = ({
  loading,
  data,
  onEdit,
  onDelete,
  onViewDetails,
}: VatTableProps) => {
  const formatVatRate = (rate: number) => `${rate.toFixed(2)}%`;

  return (
    <div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>S.No</TableHead>
            <TableHead>Country</TableHead>
            <TableHead>Default VAT Rate</TableHead>
            <TableHead>Region Based</TableHead>
            <TableHead>Regions</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center">
                <SpinnerLoader />
              </TableCell>
            </TableRow>
          ) : (
            <>
              {data?.length > 0 ? (
                <>
                  {data?.map((vat, index) => (
                    <TableRow key={vat.id}>
                      <TableCell>{index + 1}</TableCell>
                      <TableCell className="font-medium">
                        {vat.countryName}
                      </TableCell>
                      <TableCell>{formatVatRate(vat.defaultVatRate)}</TableCell>
                      <TableCell>
                        <Badge
                          variant={vat.regionBased ? 'default' : 'secondary'}
                        >
                          {vat.regionBased ? 'Yes' : 'No'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {vat.regionBased ? (
                          <span className="text-sm text-muted-foreground">
                            {vat.regions.length} regions
                          </span>
                        ) : (
                          <span className="text-sm text-muted-foreground">
                            —
                          </span>
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onViewDetails(vat)}
                          >
                            <Eye className="size-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => onEdit(vat)}
                          >
                            <Edit className="size-4" />
                          </Button>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="ghost" size="sm">
                                <Trash2 className="size-4" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>
                                  Delete VAT Configuration
                                </AlertDialogTitle>
                                <AlertDialogDescription>
                                  Are you sure you want to delete the VAT
                                  configuration for {vat.countryName}? This
                                  action cannot be undone.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Cancel</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => onDelete(vat.id)}
                                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                >
                                  Delete
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </>
              ) : (
                <TableRow>
                  <TableCell colSpan={6} className="text-center">
                    <div className="text-center text-muted-foreground">
                      No VAT configurations found.
                    </div>
                  </TableCell>
                </TableRow>
              )}
            </>
          )}
        </TableBody>
      </Table>
    </div>
  );
};

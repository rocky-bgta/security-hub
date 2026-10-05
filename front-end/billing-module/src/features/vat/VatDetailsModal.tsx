import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Separator } from 'common/Separator';
import { Edit } from 'lucide-react';
import { VatConfiguration } from 'models/Vat';

interface VatDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  data: VatConfiguration | null;
  onEdit: (vat: VatConfiguration) => void;
}

export const VatDetailsModal = ({
  isOpen,
  onClose,
  data,
  onEdit,
}: VatDetailsModalProps) => {
  if (!data) return null;

  const formatVatRate = (rate: number) => `${rate.toFixed(2)}%`;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
        <DialogHeader className="mt-6 flex flex-row items-center justify-between">
          <DialogTitle>VAT Configuration Details</DialogTitle>
          <Button
            onClick={() => onEdit(data)}
            size="sm"
            className="flex items-center gap-2"
          >
            <Edit className="size-4" />
            Edit
          </Button>
        </DialogHeader>

        <div className="space-y-6">
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Basic Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">
                    Country
                  </label>
                  <p className="text-lg font-semibold">{data.countryName}</p>
                </div>
                {/* <div>
                  <label className="text-sm font-medium text-muted-foreground">
                    Country Code
                  </label>
                  <p className="text-lg font-semibold">
                    {data.id.toUpperCase()}
                  </p>
                </div> */}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">
                    Default VAT Rate
                  </label>
                  <p className="text-lg font-semibold">
                    {formatVatRate(data.defaultVatRate)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">
                    Region Based
                  </label>
                  <div className="pt-1">
                    <Badge variant={data.regionBased ? 'default' : 'secondary'}>
                      {data.regionBased ? 'Yes' : 'No'}
                    </Badge>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Regions */}
          {data.regionBased && (
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Regional VAT Rates</CardTitle>
              </CardHeader>
              <CardContent>
                {data.regions.length > 0 ? (
                  <div className="space-y-3">
                    {data.regions.map((region, index) => (
                      <div key={index}>
                        <div className="flex items-center justify-between py-3">
                          <div>
                            <p className="font-medium">{region.regionName}</p>
                            {/* <p className="text-sm text-muted-foreground">
                              Code: {region.id}
                            </p> */}
                          </div>
                          <Badge
                            variant="outline"
                            className="px-3 py-1 text-lg"
                          >
                            {formatVatRate(region.vatRate)}
                          </Badge>
                        </div>
                        {index < data.regions.length - 1 && <Separator />}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="py-8 text-center text-muted-foreground">
                    No regions configured
                  </p>
                )}
              </CardContent>
            </Card>
          )}
        </div>

        <div className="flex justify-end">
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

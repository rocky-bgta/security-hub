import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { ArrowRight, Cloud, Package, Shield, Users } from 'lucide-react';
import { useState } from 'react';

interface ProductSelectionStepProps {
  onNext: (data: ProductSelectionData) => void;
  onSkip: () => void;
}

export interface ProductSelectionData {
  selectedProducts: SelectedProduct[];
  totalAmount: number;
}

interface SelectedProduct {
  id: string;
  name: string;
  licenses: number;
  validityPeriod: string;
  validityType: 'months' | 'years';
  unitPrice: number;
  totalPrice: number;
}

interface Product {
  id: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  unitPrice: number;
  features: string[];
}

export const ProductSelectionStep: React.FC<ProductSelectionStepProps> = ({
  onNext,
  onSkip,
}) => {
  const [selectedProducts, setSelectedProducts] = useState<SelectedProduct[]>(
    [],
  );
  const [isLoading, setIsLoading] = useState(false);

  const products: Product[] = [
    {
      id: 'security-suite',
      name: 'Security Suite',
      description: 'Comprehensive security management and monitoring',
      icon: <Shield className="size-6" />,
      unitPrice: 15,
      features: [
        'Threat Detection',
        'Vulnerability Scanning',
        'Security Monitoring',
        '24/7 Support',
      ],
    },
    {
      id: 'cloud-management',
      name: 'Cloud Management',
      description: 'Multi-cloud infrastructure management platform',
      icon: <Cloud className="size-6" />,
      unitPrice: 25,
      features: [
        'Multi-Cloud Support',
        'Resource Optimization',
        'Cost Management',
        'Automated Backups',
      ],
    },
    {
      id: 'productivity-tools',
      name: 'Productivity Tools',
      description: 'Enhanced productivity and collaboration suite',
      icon: <Users className="size-6" />,
      unitPrice: 12,
      features: [
        'Team Collaboration',
        'Project Management',
        'File Sharing',
        'Communication Tools',
      ],
    },
  ];

  const months = Array.from({ length: 12 }, (_, i) => i + 1);
  const years = Array.from({ length: 10 }, (_, i) => i + 1);

  const handleProductToggle = (product: Product, checked: boolean) => {
    if (checked) {
      const newProduct: SelectedProduct = {
        id: product.id,
        name: product.name,
        licenses: 1,
        validityPeriod: '1',
        validityType: 'months',
        unitPrice: product.unitPrice,
        totalPrice: product.unitPrice,
      };
      setSelectedProducts(prev => [...prev, newProduct]);
    } else {
      setSelectedProducts(prev => prev.filter(p => p.id !== product.id));
    }
  };

  const updateProductDetails = (
    productId: string,
    field: keyof SelectedProduct,
    value: any,
  ) => {
    setSelectedProducts(prev =>
      prev.map(product => {
        if (product.id === productId) {
          const updated = { ...product, [field]: value };
          if (
            field === 'licenses' ||
            field === 'validityPeriod' ||
            field === 'validityType'
          ) {
            const multiplier = updated.validityType === 'years' ? 12 : 1;
            updated.totalPrice =
              updated.licenses *
              updated.unitPrice *
              parseInt(updated.validityPeriod) *
              multiplier;
          }
          return updated;
        }
        return product;
      }),
    );
  };

  const getTotalAmount = () => {
    return selectedProducts.reduce(
      (sum, product) => sum + product.totalPrice,
      0,
    );
  };

  const handleProceedWithPurchase = async () => {
    if (selectedProducts.length === 0) {
      //   toast({
      //     title: 'No Products Selected',
      //     description: 'Please select at least one product to proceed',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      onNext({
        selectedProducts,
        totalAmount: getTotalAmount(),
      });
    }, 1000);
  };

  const handleSkipPurchase = () => {
    onSkip();
    // toast({
    //   title: 'Purchase Skipped',
    //   description: 'You can add products later from your dashboard',
    // });
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <Package className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">Product Selection</h3>
        <p className="text-muted-foreground">
          Select the products and packages you want to purchase
        </p>
      </div>

      <div className="grid gap-6">
        {products.map(product => {
          const isSelected = selectedProducts.some(p => p.id === product.id);
          const selectedProduct = selectedProducts.find(
            p => p.id === product.id,
          );

          return (
            <Card key={product.id}>
              <CardHeader>
                <div className="flex items-center gap-4">
                  <Checkbox
                    checked={isSelected}
                    onCheckedChange={checked =>
                      handleProductToggle(product, checked as boolean)
                    }
                  />
                  <div className="flex items-center gap-3">
                    <div className="rounded-lg bg-primary/10 p-2 text-primary">
                      {product.icon}
                    </div>
                    <div>
                      <CardTitle className="text-lg">{product.name}</CardTitle>
                      <p className="text-sm text-muted-foreground">
                        {product.description}
                      </p>
                      <p className="text-sm font-medium text-primary">
                        ${product.unitPrice}/license/month
                      </p>
                    </div>
                  </div>
                </div>
              </CardHeader>

              {isSelected && selectedProduct && (
                <CardContent className="pt-0">
                  <div className="grid gap-4 md:grid-cols-3">
                    <div className="space-y-2">
                      <Label>Number of Licenses</Label>
                      <Input
                        type="number"
                        min="1"
                        value={selectedProduct.licenses}
                        onChange={e =>
                          updateProductDetails(
                            product.id,
                            'licenses',
                            parseInt(e.target.value) || 1,
                          )
                        }
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Validity Type</Label>
                      <Select
                        value={selectedProduct.validityType}
                        onValueChange={value =>
                          updateProductDetails(
                            product.id,
                            'validityType',
                            value,
                          )
                        }
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="months">Months</SelectItem>
                          <SelectItem value="years">Years</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label>Validity Period</Label>
                      <Select
                        value={selectedProduct.validityPeriod}
                        onValueChange={value =>
                          updateProductDetails(
                            product.id,
                            'validityPeriod',
                            value,
                          )
                        }
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {(selectedProduct.validityType === 'months'
                            ? months
                            : years
                          ).map(num => (
                            <SelectItem key={num} value={num.toString()}>
                              {num}{' '}
                              {selectedProduct.validityType === 'months'
                                ? 'Month(s)'
                                : 'Year(s)'}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div className="mt-4 rounded-lg bg-muted p-3">
                    <div className="flex items-center justify-between">
                      <span className="font-medium">
                        Total for {product.name}:
                      </span>
                      <span className="text-lg font-bold text-primary">
                        ${selectedProduct.totalPrice}
                      </span>
                    </div>
                  </div>

                  <div className="mt-3">
                    <h4 className="mb-2 font-medium">Features Included:</h4>
                    <ul className="space-y-1 text-sm text-muted-foreground">
                      {product.features.map((feature, index) => (
                        <li key={index} className="flex items-center gap-2">
                          <div className="size-1.5 rounded-full bg-primary" />
                          {feature}
                        </li>
                      ))}
                    </ul>
                  </div>
                </CardContent>
              )}
            </Card>
          );
        })}
      </div>

      {selectedProducts.length > 0 && (
        <Card className="bg-gradient-to-r from-primary/5 to-secondary/5">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between text-lg font-semibold">
              <span>Total Amount:</span>
              <span className="text-2xl text-primary">${getTotalAmount()}</span>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex gap-4">
        <Button
          variant="outline"
          className="h-12 flex-1"
          onClick={handleSkipPurchase}
        >
          Skip Purchase Now
        </Button>

        <Button
          className="h-12 flex-1 bg-primary transition-opacity hover:opacity-90"
          onClick={handleProceedWithPurchase}
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Processing...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Proceed with Purchase
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </div>
    </div>
  );
};

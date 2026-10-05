import { Card } from 'common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from 'common/Table';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';

const ProductListSkeleton = () => {
  return (
    <div>
      <SkeletonTheme baseColor="#7C7C7C" highlightColor="#9A9A9A" duration={1}>
        {[...Array(2)].map((_, index) => (
          <Card key={index} className="content-mb-6 content-w-full content-p-6">
            <div>
              <div>
                <Skeleton height={20} width={'50%'} />
              </div>
              <Skeleton height={12} width={'20%'} />

              <Skeleton height={16} width={'20%'} className="content-mt-6" />

              <Table className="content-mt-4">
                <TableHeader>
                  <TableRow>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                    <TableCell>
                      <Skeleton height={16} width={'20%'} />
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </div>
          </Card>
        ))}
      </SkeletonTheme>
    </div>
  );
};

export default ProductListSkeleton;

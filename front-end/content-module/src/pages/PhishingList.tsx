import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import Border from 'components/UserBorder';
import { Fragment } from 'react';
import { cn } from 'utils/Helper';

interface IPhishingData {
  name: string;
  date: string;
  time: string;
  result: string;
}

const phishingData: IPhishingData[] = [
  {
    name: 'Amazon-Black-Friday',
    date: '5/11/2026',
    time: '11:49 pm',
    result: 'Not Started',
  },
  {
    name: 'Password-Change',
    date: '4/28/2026',
    time: '02:10 pm',
    result: 'Not Started',
  },
  {
    name: 'Gmail-Blocked-Login',
    date: '3/30/2026',
    time: '07:38 am',
    result: 'Not Started',
  },
  {
    name: 'QR-Code-Office-365',
    date: '2/16/2026',
    time: '05:14 pm',
    result: 'Not Started',
  },
  {
    name: 'Office365-Password-Reset',
    date: '1/18/2026',
    time: '10:41 pm',
    result: 'Not Started',
  },
  {
    name: 'OneDrive-Word-Share',
    date: '12/4/2026',
    time: '01:08 pm',
    result: 'Not Started',
  },
];

const PhishingList = () => {
  return (
    <Fragment>
      <p className="content-text-ash-gray">Phishing Report</p>
      <Border className="content-mt-4 content-p-6">
        <p className="content-mb-6 content-text-xl content-text-white">
          Phishing Report
        </p>

        <div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>PHISHING NAME</TableHead>
                <TableHead>PHISHING DATE</TableHead>
                <TableHead>TIME</TableHead>
                <TableHead>results</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {phishingData.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} className="content-text-center">
                    <p className="content-text-ash-gray">
                      No data not available
                    </p>
                  </TableCell>
                </TableRow>
              ) : (
                phishingData.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>{item.name}</TableCell>
                    <TableCell>{item.date}</TableCell>
                    <TableCell>{item.time}</TableCell>
                    <TableCell
                      className={cn(
                        item.result === 'Pass'
                          ? 'content-text-success'
                          : 'content-text-vibrant-red',
                      )}
                    >
                      {item.result}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </Border>
    </Fragment>
  );
};

export default PhishingList;

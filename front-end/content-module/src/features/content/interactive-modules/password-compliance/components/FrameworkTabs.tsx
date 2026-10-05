import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tab';
import {
  FrameworkCategory,
  IFramework,
  IFrameworkEvaluation,
} from '../types';
import FrameworkCard from './FrameworkCard';

interface IProps {
  activeTab: FrameworkCategory;
  onTabChange: (tab: FrameworkCategory) => void;
  frameworks: {
    security: Array<IFramework>;
    privacy: Array<IFramework>;
    other: Array<IFramework>;
  };
  evaluationsByCategory: {
    security: Array<IFrameworkEvaluation>;
    privacy: Array<IFrameworkEvaluation>;
    other: Array<IFrameworkEvaluation>;
  };
  hasPassword: boolean;
}

const TAB_CONFIG: Array<{
  value: FrameworkCategory;
  label: string;
  heading: string;
}> = [
  {
    value: 'security',
    label: 'Security Frameworks',
    heading: 'Security Frameworks',
  },
  {
    value: 'privacy',
    label: 'Privacy Frameworks',
    heading: 'Privacy Frameworks',
  },
  {
    value: 'other',
    label: 'Other Compliance',
    heading: 'Other Compliance Frameworks',
  },
];

const FrameworkTabs = ({
  activeTab,
  onTabChange,
  frameworks,
  evaluationsByCategory,
  hasPassword,
}: IProps) => {
  return (
    <Tabs
      value={activeTab}
      onValueChange={value => onTabChange(value as FrameworkCategory)}
    >
      <div className="content-overflow-x-auto content-pb-1">
        <TabsList className="content-inline-flex content-min-w-full content-justify-start content-gap-1 sm:content-min-w-0">
          {TAB_CONFIG.map(tab => (
            <TabsTrigger key={tab.value} value={tab.value} className="content-shrink-0">
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </div>

      {TAB_CONFIG.map(tab => (
        <TabsContent
          key={tab.value}
          value={tab.value}
          className="content-mt-3 content-overflow-y-auto content-pr-1"
        >
          <h2 className="content-sr-only">{tab.heading}</h2>
          <div className="content-grid content-grid-cols-1 content-gap-3 md:content-grid-cols-2 lg:content-grid-cols-3">
            {frameworks[tab.value].map((framework, index) => (
              <FrameworkCard
                key={`${tab.value}-${framework.name}`}
                framework={framework}
                evaluation={evaluationsByCategory[tab.value][index]}
                hasPassword={hasPassword}
              />
            ))}
          </div>
        </TabsContent>
      ))}
    </Tabs>
  );
};

export default FrameworkTabs;

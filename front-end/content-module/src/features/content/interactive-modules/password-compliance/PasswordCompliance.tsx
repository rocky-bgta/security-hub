import ComplianceScoreCard from './components/ComplianceScoreCard';
import FrameworkTabs from './components/FrameworkTabs';
import PasswordControls from './components/PasswordControls';
import StrengthBar from './components/StrengthBar';
import { usePasswordCompliance } from './hooks/usePasswordCompliance';
import { cn } from 'utils/Helper';

export interface PasswordComplianceProps {
  title?: string;
  description?: string;
  className?: string;
}

const PasswordCompliance = ({
  title = 'Password Compliance Checker',
  description = 'Validate your password against multiple security and privacy frameworks',
  className,
}: PasswordComplianceProps) => {
  const {
    password,
    setPassword,
    usesMfa,
    setUsesMfa,
    changeFrequency,
    setChangeFrequency,
    activeTab,
    setActiveTab,
    hasPassword,
    strength,
    strengthLabel,
    complianceScore,
    frameworks,
    evaluationsByCategory,
  } = usePasswordCompliance();

  return (
    <div
      className={cn(
        'content-mx-auto content-w-full content-max-w-7xl content-p-3 md:content-p-4',
        className,
      )}
    >
      <header className="content-mb-3 content-text-center">
        <h1 className="content-mb-1 content-text-xl content-font-bold content-text-white md:content-text-2xl">
          {title}
        </h1>
        {description ? (
          <p className="content-text-sm content-leading-snug content-text-muted-foreground">
            {description}
          </p>
        ) : null}
      </header>

      <section
        className="content-mb-3 content-rounded-lg content-border content-border-white/10 content-bg-card-background/80 content-p-3 content-shadow-sm content-backdrop-blur-sm"
        aria-labelledby="password-controls-heading"
      >
        <h2 id="password-controls-heading" className="content-sr-only">
          Password and compliance inputs
        </h2>

        <PasswordControls
          password={password}
          onPasswordChange={setPassword}
          usesMfa={usesMfa}
          onUsesMfaChange={setUsesMfa}
          changeFrequency={changeFrequency}
          onChangeFrequencyChange={setChangeFrequency}
        />

        <div className="content-mt-3 content-space-y-3">
          <StrengthBar strength={strength} label={strengthLabel} />
          <ComplianceScoreCard score={complianceScore} />
        </div>
      </section>

      <section
        className="content-rounded-lg content-border content-border-white/10 content-bg-card-background/80 content-p-3 content-shadow-sm content-backdrop-blur-sm"
        aria-label="Framework compliance results"
      >
        <FrameworkTabs
          activeTab={activeTab}
          onTabChange={setActiveTab}
          frameworks={frameworks}
          evaluationsByCategory={evaluationsByCategory}
          hasPassword={hasPassword}
        />
      </section>

      <footer
        id="password-compliance-privacy"
        className="content-mt-3 content-text-center content-text-xs content-text-muted-foreground"
      >
        <p>
          This tool helps you check password compliance against various
          frameworks. Your password is not stored or transmitted.
        </p>
      </footer>
    </div>
  );
};

export default PasswordCompliance;

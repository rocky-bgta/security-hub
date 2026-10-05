import ProviderConfigurationView from 'features/provider-credential/ProviderConfigurationView';

const ProviderConfiguration = () => (
  <ProviderConfigurationView
    copy={{
      pageTitle: 'Provider configuration',
      pageDescription:
        'Manage API credentials for deepfake voice cloning and video rendering providers.',
      emptyStateHint:
        'Add ElevenLabs, Fish Audio, or HeyGen credentials to get started.',
    }}
  />
);

export default ProviderConfiguration;

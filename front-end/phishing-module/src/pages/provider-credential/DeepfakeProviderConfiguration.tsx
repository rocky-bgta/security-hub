import ProviderConfigurationView from 'features/provider-credential/ProviderConfigurationView';

const DeepfakeProviderConfiguration = () => (
  <ProviderConfigurationView
    copy={{
      pageTitle: 'Deepfake provider configuration',
      pageDescription:
        'Manage API credentials for deepfake voice cloning and video rendering providers.',
      emptyStateHint:
        'Add ElevenLabs, Fish Audio, or HeyGen credentials for deepfake generation.',
    }}
  />
);

export default DeepfakeProviderConfiguration;

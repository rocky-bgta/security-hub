import ProviderConfigurationView from 'features/provider-credential/ProviderConfigurationView';

const VishingProviderConfiguration = () => (
  <ProviderConfigurationView
    copy={{
      pageTitle: 'Vishing provider configuration',
      pageDescription:
        'Manage API credentials for voice synthesis providers used in vishing simulations.',
      emptyStateHint:
        'Add ElevenLabs or Fish Audio credentials to get started.',
    }}
  />
);

export default VishingProviderConfiguration;

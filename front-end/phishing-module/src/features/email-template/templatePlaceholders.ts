export type PlaceholderField = {
  token: string;
  label: string;
  description: string;
};

export type PlaceholderGroup = {
  title: string;
  fields: PlaceholderField[];
};

export const PLACEHOLDER_GROUPS: PlaceholderGroup[] = [
  {
    title: 'User Info',
    fields: [
      {
        token: '{{FIRST_NAME}}',
        label: 'First Name',
        description: "User's first name",
      },
      {
        token: '{{LAST_NAME}}',
        label: 'Last Name',
        description: "User's last name",
      },
      {
        token: '{{EMAIL_ADDRESS}}',
        label: 'Email',
        description: "User's email address",
      },
      {
        token: '{{PHONE_NUMBER}}',
        label: 'Phone',
        description: "User's phone number",
      },
      {
        token: '{{LOCATION}}',
        label: 'Location',
        description: "User's location",
      },
      {
        token: '{{DEPARTMENT}}',
        label: 'Department',
        description: "User's department",
      },
    ],
  },
  {
    title: 'System',
    fields: [
      {
        token: '{{PHISHING_LINK}}',
        label: 'Phishing Link',
        description: 'The phishing landing page link',
      },
    ],
  },
];

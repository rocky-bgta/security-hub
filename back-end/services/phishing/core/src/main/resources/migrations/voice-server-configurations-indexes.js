// MongoDB indexes for voice_server_configurations (mirror sms-server-configurations-indexes.js)
db.voice_server_configurations.createIndex({ clientId: 1, isDefault: 1 }, { name: "client_default_idx" });
db.voice_server_configurations.createIndex({ clientId: 1, name: 1 }, { name: "client_name_idx", unique: true });

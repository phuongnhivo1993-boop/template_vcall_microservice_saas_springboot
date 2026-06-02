-- Insert default channel configs for Zalo and Facebook
INSERT INTO channel_configs (channel, display_name, is_enabled, config_json, created_at, is_deleted)
VALUES 
('zalo', 'Zalo OA', false, '{"appId":"","appSecret":"","oaId":"","webhookUrl":"","callbackUrl":""}', NOW(), false),
('facebook', 'Facebook Messenger', false, '{"pageId":"","appId":"","appSecret":"","pageAccessToken":"","webhookVerifyToken":""}', NOW(), false)
ON CONFLICT (channel) DO NOTHING;

#!/bin/bash
set -e

for db in vcall_iam vcall_agent vcall_customer vcall_crm vcall_call vcall_sip vcall_pbx vcall_recording vcall_omnichannel vcall_chat vcall_email vcall_sms vcall_ticket vcall_campaign vcall_billing vcall_cdr vcall_reporting vcall_notification vcall_audit; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE $db;
    GRANT ALL PRIVILEGES ON DATABASE $db TO $POSTGRES_USER;
EOSQL
done

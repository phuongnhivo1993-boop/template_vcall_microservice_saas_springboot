#!/bin/bash
# =============================================================================
# VCall Contact Center - E2E Flow Test Script
# =============================================================================
# Flow: Create Host -> Login -> Create Tenant -> Login Tenant -> CRUD Services
# =============================================================================

set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
API_PREFIX="/api/v1"

PASS=0
FAIL=0
TESTS=()

color_green='\033[0;32m'
color_red='\033[0;31m'
color_yellow='\033[1;33m'
color_cyan='\033[0;36m'
color_reset='\033[0m'

log_info()  { echo -e "${color_cyan}[INFO]${color_reset}  $*"; }
log_ok()    { echo -e "${color_green}[OK]${color_reset}    $*"; }
log_fail()  { echo -e "${color_red}[FAIL]${color_reset}  $*"; }
log_step()  { echo -e "\n${color_yellow}=== $* ===${color_reset}"; }

assert_status() {
    local test_name="$1" expected="$2" actual="$3"
    if [ "$actual" -eq "$expected" ]; then
        PASS=$((PASS + 1))
        TESTS+=("PASS: $test_name")
        log_ok "$test_name"
    else
        FAIL=$((FAIL + 1))
        TESTS+=("FAIL: $test_name (expected=$expected, actual=$actual)")
        log_fail "$test_name (expected=$expected, actual=$actual)"
    fi
}

assert_contains() {
    local test_name="$1" haystack="$2" needle="$3"
    if echo "$haystack" | grep -q "$needle"; then
        PASS=$((PASS + 1))
        TESTS+=("PASS: $test_name")
        log_ok "$test_name"
    else
        FAIL=$((FAIL + 1))
        TESTS+=("FAIL: $test_name (missing: $needle)")
        log_fail "$test_name (missing: $needle)"
    fi
}

api_get() {
    local url="$1" token="$2"
    curl -s -w "\n%{http_code}" -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" "$url"
}

api_post() {
    local url="$1" data="$2" token="${3:-}"
    if [ -n "$token" ]; then
        curl -s -w "\n%{http_code}" -X POST \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data" "$url"
    else
        curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$data" "$url"
    fi
}

api_put() {
    local url="$1" data="$2" token="$3"
    curl -s -w "\n%{http_code}" -X PUT \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$data" "$url"
}

api_delete() {
    local url="$1" token="$2"
    curl -s -w "\n%{http_code}" -X DELETE \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        "$url"
}

api_patch() {
    local url="$1" data="$2" token="$3"
    curl -s -w "\n%{http_code}" -X PATCH \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$data" "$url"
}

extract_http_code() {
    echo "$1" | tail -1
}

extract_body() {
    echo "$1" | sed '$d'
}

extract_field() {
    echo "$1" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data', {}).get('$2','') if 'data' in d else '')" 2>/dev/null || echo ""
}

# =============================================================================
echo -e "${color_cyan}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║       VCall Contact Center - E2E Flow Test Suite            ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${color_reset}"
echo "Gateway: $GATEWAY_URL"
echo "Date:    $(date)"
echo ""

# ------------------------------------------------------------------
# PREREQUISITES
# ------------------------------------------------------------------
log_step "PREREQUISITES: Check Gateway Health"

HEALTH_RESP=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/actuator/health" 2>/dev/null || echo "000")
assert_status "Gateway health check" 200 "$HEALTH_RESP"

if [ "$HEALTH_RESP" != "200" ]; then
    log_fail "Gateway is not running at $GATEWAY_URL! Please start infrastructure first."
    log_info "Run: make infra-up && make up"
    exit 1
fi

# ------------------------------------------------------------------
# STEP 1: Create SUPER_ADMIN user (HOST)
# ------------------------------------------------------------------
log_step "STEP 1: Create SUPER_ADMIN Host User"

HOST_USER='{
    "username": "host_admin",
    "password": "Admin@123456",
    "email": "host@vcall.com",
    "phone": "0900000001",
    "fullName": "Host Administrator",
    "roles": ["SUPER_ADMIN"]
}'

CREATE_HOST_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/users" "$HOST_USER")
CREATE_HOST_CODE=$(extract_http_code "$CREATE_HOST_RESP")
CREATE_HOST_BODY=$(extract_body "$CREATE_HOST_RESP")

assert_status "Create host user" 201 "$CREATE_HOST_CODE"

HOST_USER_ID=$(extract_field "$CREATE_HOST_BODY" "id")
log_info "Host user ID: $HOST_USER_ID"

# ------------------------------------------------------------------
# STEP 2: Login as HOST
# ------------------------------------------------------------------
log_step "STEP 2: Login as Host"

LOGIN_DATA='{"username": "host_admin", "password": "Admin@123456"}'
LOGIN_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/auth/login" "$LOGIN_DATA")
LOGIN_CODE=$(extract_http_code "$LOGIN_RESP")
LOGIN_BODY=$(extract_body "$LOGIN_RESP")

assert_status "Login as host" 200 "$LOGIN_CODE"
assert_contains "Login response has accessToken" "$LOGIN_BODY" "accessToken"

HOST_TOKEN=$(echo "$LOGIN_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('accessToken',''))" 2>/dev/null)
log_info "Host token obtained: ${HOST_TOKEN:0:20}..."

# ------------------------------------------------------------------
# STEP 3: Create Roles (SUPERVISOR, AGENT)
# ------------------------------------------------------------------
log_step "STEP 3: Create Roles"

ROLE_AGENT='{"name": "AGENT", "description": "Agent role"}'
ROLE_AGENT_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/roles" "$ROLE_AGENT" "$HOST_TOKEN")
assert_status "Create AGENT role" 201 "$(extract_http_code "$ROLE_AGENT_RESP")"

ROLE_SUPERVISOR='{"name": "SUPERVISOR", "description": "Supervisor role"}'
ROLE_SUPERVISOR_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/roles" "$ROLE_SUPERVISOR" "$HOST_TOKEN")
assert_status "Create SUPERVISOR role" 201 "$(extract_http_code "$ROLE_SUPERVISOR_RESP")"

# ------------------------------------------------------------------
# STEP 4: Create Tenant Users (Supervisor + Agent)
# ------------------------------------------------------------------
log_step "STEP 4: Create Tenant Users"

TENANT_ADMIN='{
    "username": "tenant_admin",
    "password": "Tenant@123456",
    "email": "admin@tenant1.com",
    "phone": "0900000010",
    "fullName": "Tenant Administrator",
    "roles": ["SUPERVISOR"]
}'
CREATE_TA_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/users" "$TENANT_ADMIN" "$HOST_TOKEN")
assert_status "Create tenant admin user" 201 "$(extract_http_code "$CREATE_TA_RESP")"
TENANT_ADMIN_ID=$(extract_body "$CREATE_TA_RESP" | extract_field "id")

TENANT_AGENT='{
    "username": "tenant_agent",
    "password": "Agent@123456",
    "email": "agent@tenant1.com",
    "phone": "0900000011",
    "fullName": "Tenant Agent User",
    "roles": ["AGENT"]
}'
CREATE_AG_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/users" "$TENANT_AGENT" "$HOST_TOKEN")
assert_status "Create tenant agent user" 201 "$(extract_http_code "$CREATE_AG_RESP")"
TENANT_AGENT_ID=$(extract_body "$CREATE_AG_RESP" | extract_field "id")

# ------------------------------------------------------------------
# STEP 5: Login as Tenant Admin
# ------------------------------------------------------------------
log_step "STEP 5: Login as Tenant Admin"

TA_LOGIN_DATA='{"username": "tenant_admin", "password": "Tenant@123456"}'
TA_LOGIN_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/auth/login" "$TA_LOGIN_DATA")
assert_status "Login as tenant admin" 200 "$(extract_http_code "$TA_LOGIN_RESP")"
TENANT_TOKEN=$(extract_body "$TA_LOGIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('accessToken',''))" 2>/dev/null)
log_info "Tenant token obtained: ${TENANT_TOKEN:0:20}..."

# ------------------------------------------------------------------
# STEP 6: CRUD - Agents
# ------------------------------------------------------------------
log_step "STEP 6: CRUD - Agent Service"

log_info "Creating agent..."
AGENT_CREATE='{
    "userId": "'"$TENANT_AGENT_ID"'",
    "agentCode": "AGT001",
    "fullName": "Nguyen Van A",
    "email": "nguyenvana@tenant1.com",
    "phone": "0900000020",
    "skill": "CS_CATEGORY_1"
}'
AGENT_CREATE_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/agents" "$AGENT_CREATE" "$TENANT_TOKEN")
assert_status "Create agent" 201 "$(extract_http_code "$AGENT_CREATE_RESP")"
AGENT_ID=$(extract_body "$AGENT_CREATE_RESP" | extract_field "id")
log_info "Agent ID: $AGENT_ID"

log_info "Listing agents..."
AGENT_LIST_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/agents" "$TENANT_TOKEN")
assert_status "List agents" 200 "$(extract_http_code "$AGENT_LIST_RESP")"

log_info "Getting agent by ID..."
AGENT_GET_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/agents/$AGENT_ID" "$TENANT_TOKEN")
assert_status "Get agent by ID" 200 "$(extract_http_code "$AGENT_GET_RESP")"

log_info "Updating agent..."
AGENT_UPDATE='{
    "userId": "'"$TENANT_AGENT_ID"'",
    "agentCode": "AGT001",
    "fullName": "Nguyen Van A - Updated",
    "email": "nguyenvana@tenant1.com",
    "phone": "0900000020",
    "skill": "CS_CATEGORY_2"
}'
AGENT_UPDATE_RESP=$(api_put "$GATEWAY_URL$API_PREFIX/agents/$AGENT_ID" "$AGENT_UPDATE" "$TENANT_TOKEN")
assert_status "Update agent" 200 "$(extract_http_code "$AGENT_UPDATE_RESP")"

log_info "Updating agent status..."
AGENT_STATUS='{"status": "ONLINE"}'
AGENT_STATUS_RESP=$(api_patch "$GATEWAY_URL$API_PREFIX/agents/$AGENT_ID/status" "$AGENT_STATUS" "$TENANT_TOKEN")
assert_status "Update agent status" 200 "$(extract_http_code "$AGENT_STATUS_RESP")"

log_info "Getting agents by status..."
AGENT_BY_STATUS_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/agents/status/ONLINE" "$TENANT_TOKEN")
assert_status "Get agents by status" 200 "$(extract_http_code "$AGENT_BY_STATUS_RESP")"

log_info "Deleting agent..."
AGENT_DEL_RESP=$(api_delete "$GATEWAY_URL$API_PREFIX/agents/$AGENT_ID" "$TENANT_TOKEN")
assert_status "Delete agent" 200 "$(extract_http_code "$AGENT_DEL_RESP")"

log_info "Re-creating agent for subsequent tests..."
AGENT_CREATE_RESP2=$(api_post "$GATEWAY_URL$API_PREFIX/agents" "$AGENT_CREATE" "$TENANT_TOKEN")
AGENT_ID2=$(extract_body "$AGENT_CREATE_RESP2" | extract_field "id")
AGENT_ID="$AGENT_ID2"
assert_status "Re-create agent" 201 "$(extract_http_code "$AGENT_CREATE_RESP2")"

# ------------------------------------------------------------------
# STEP 7: CRUD - Customers
# ------------------------------------------------------------------
log_step "STEP 7: CRUD - Customer Service"

log_info "Creating customer..."
CUSTOMER_CREATE='{
    "fullName": "Tran Thi B",
    "email": "tranthib@example.com",
    "phone": "0900000030",
    "gender": "FEMALE",
    "company": "ABC Corp"
}'
CUST_CREATE_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/customers" "$CUSTOMER_CREATE" "$TENANT_TOKEN")
assert_status "Create customer" 201 "$(extract_http_code "$CUST_CREATE_RESP")"
CUSTOMER_ID=$(extract_body "$CUST_CREATE_RESP" | extract_field "id")
log_info "Customer ID: $CUSTOMER_ID"

log_info "Listing customers..."
CUST_LIST_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/customers?page=0&size=10" "$TENANT_TOKEN")
assert_status "List customers" 200 "$(extract_http_code "$CUST_LIST_RESP")"

log_info "Getting customer by ID..."
CUST_GET_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/customers/$CUSTOMER_ID" "$TENANT_TOKEN")
assert_status "Get customer by ID" 200 "$(extract_http_code "$CUST_GET_RESP")"

log_info "Updating customer..."
CUSTOMER_UPDATE='{
    "fullName": "Tran Thi B - Updated",
    "email": "tranthib@example.com",
    "phone": "0900000031",
    "gender": "FEMALE",
    "company": "XYZ Ltd"
}'
CUST_UPDATE_RESP=$(api_put "$GATEWAY_URL$API_PREFIX/customers/$CUSTOMER_ID" "$CUSTOMER_UPDATE" "$TENANT_TOKEN")
assert_status "Update customer" 200 "$(extract_http_code "$CUST_UPDATE_RESP")"

log_info "Searching customers..."
CUST_SEARCH_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/customers/search?keyword=Tran" "$TENANT_TOKEN")
assert_status "Search customers" 200 "$(extract_http_code "$CUST_SEARCH_RESP")"

log_info "Deleting customer..."
CUST_DEL_RESP=$(api_delete "$GATEWAY_URL$API_PREFIX/customers/$CUSTOMER_ID" "$TENANT_TOKEN")
assert_status "Delete customer" 200 "$(extract_http_code "$CUST_DEL_RESP")"

log_info "Re-creating customer for subsequent tests..."
CUST_CREATE_RESP2=$(api_post "$GATEWAY_URL$API_PREFIX/customers" "$CUSTOMER_CREATE" "$TENANT_TOKEN")
CUSTOMER_ID2=$(extract_body "$CUST_CREATE_RESP2" | extract_field "id")
CUSTOMER_ID="$CUSTOMER_ID2"
assert_status "Re-create customer" 201 "$(extract_http_code "$CUST_CREATE_RESP2")"

# ------------------------------------------------------------------
# STEP 8: CRUD - Tickets
# ------------------------------------------------------------------
log_step "STEP 8: CRUD - Ticket Service"

log_info "Creating ticket..."
TICKET_CREATE='{
    "title": "Support request #001",
    "description": "Customer needs help with account setup",
    "customerId": "'"$CUSTOMER_ID"'",
    "source": "PORTAL",
    "category": "ACCOUNT_SETUP",
    "priority": "HIGH",
    "assignedTo": "'"$TENANT_AGENT_ID"'"
}'
TICKET_CREATE_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/tickets" "$TICKET_CREATE" "$TENANT_TOKEN")
assert_status "Create ticket" 201 "$(extract_http_code "$TICKET_CREATE_RESP")"
TICKET_ID=$(extract_body "$TICKET_CREATE_RESP" | extract_field "id")
log_info "Ticket ID: $TICKET_ID"

log_info "Listing tickets..."
TICKET_LIST_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/tickets?page=0&size=10" "$TENANT_TOKEN")
assert_status "List tickets" 200 "$(extract_http_code "$TICKET_LIST_RESP")"

log_info "Getting ticket by ID..."
TICKET_GET_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/tickets/$TICKET_ID" "$TENANT_TOKEN")
assert_status "Get ticket by ID" 200 "$(extract_http_code "$TICKET_GET_RESP")"

log_info "Updating ticket..."
TICKET_UPDATE='{
    "title": "Support request #001 - Updated",
    "description": "Customer issue resolved",
    "customerId": "'"$CUSTOMER_ID"'",
    "source": "PORTAL",
    "category": "ACCOUNT_SETUP",
    "priority": "LOW"
}'
TICKET_UPDATE_RESP=$(api_put "$GATEWAY_URL$API_PREFIX/tickets/$TICKET_ID" "$TICKET_UPDATE" "$TENANT_TOKEN")
assert_status "Update ticket" 200 "$(extract_http_code "$TICKET_UPDATE_RESP")"

log_info "Updating ticket status..."
TICKET_STATUS_UPDATE='{"status": "IN_PROGRESS"}'
TICKET_STATUS_RESP=$(api_patch "$GATEWAY_URL$API_PREFIX/tickets/$TICKET_ID/status" "$TICKET_STATUS_UPDATE" "$TENANT_TOKEN")
assert_status "Update ticket status" 200 "$(extract_http_code "$TICKET_STATUS_RESP")"

log_info "Assigning ticket..."
TICKET_ASSIGN='{"agentId": "'"$TENANT_AGENT_ID"'"}'
TICKET_ASSIGN_RESP=$(api_put "$GATEWAY_URL$API_PREFIX/tickets/$TICKET_ID/assign" "$TICKET_ASSIGN" "$TENANT_TOKEN")
assert_status "Assign ticket" 200 "$(extract_http_code "$TICKET_ASSIGN_RESP")"

log_info "Getting ticket stats..."
TICKET_STATS_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/tickets/stats/by-status" "$TENANT_TOKEN")
assert_status "Get ticket stats" 200 "$(extract_http_code "$TICKET_STATS_RESP")"

log_info "Deleting ticket..."
TICKET_DEL_RESP=$(api_delete "$GATEWAY_URL$API_PREFIX/tickets/$TICKET_ID" "$TENANT_TOKEN")
assert_status "Delete ticket" 200 "$(extract_http_code "$TICKET_DEL_RESP")"

# ------------------------------------------------------------------
# STEP 9: CRUD - Campaigns
# ------------------------------------------------------------------
log_step "STEP 9: CRUD - Campaign Service"

log_info "Creating campaign..."
CAMPAIGN_CREATE='{
    "name": "Summer Promotion 2026",
    "description": "Call campaign for summer promotion",
    "type": "OUTBOUND",
    "strategy": "SEQUENTIAL",
    "maxAttempts": 3
}'
CAMP_CREATE_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/campaigns" "$CAMPAIGN_CREATE" "$TENANT_TOKEN")
assert_status "Create campaign" 201 "$(extract_http_code "$CAMP_CREATE_RESP")"
CAMPAIGN_ID=$(extract_body "$CAMP_CREATE_RESP" | extract_field "id")
log_info "Campaign ID: $CAMPAIGN_ID"

log_info "Listing campaigns..."
CAMP_LIST_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/campaigns" "$TENANT_TOKEN")
assert_status "List campaigns" 200 "$(extract_http_code "$CAMP_LIST_RESP")"

log_info "Getting campaign by ID..."
CAMP_GET_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/campaigns/$CAMPAIGN_ID" "$TENANT_TOKEN")
assert_status "Get campaign by ID" 200 "$(extract_http_code "$CAMP_GET_RESP")"

log_info "Updating campaign..."
CAMPAIGN_UPDATE='{
    "name": "Summer Promotion 2026 - Extended",
    "description": "Extended summer campaign",
    "type": "OUTBOUND",
    "strategy": "PREDICTIVE"
}'
CAMP_UPDATE_RESP=$(api_put "$GATEWAY_URL$API_PREFIX/campaigns/$CAMPAIGN_ID" "$CAMPAIGN_UPDATE" "$TENANT_TOKEN")
assert_status "Update campaign" 200 "$(extract_http_code "$CAMP_UPDATE_RESP")"

log_info "Starting campaign..."
CAMP_START_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/campaigns/$CAMPAIGN_ID/start" '{}' "$TENANT_TOKEN")
assert_status "Start campaign" 200 "$(extract_http_code "$CAMP_START_RESP")"

log_info "Pausing campaign..."
CAMP_PAUSE_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/campaigns/$CAMPAIGN_ID/pause" '{}' "$TENANT_TOKEN")
assert_status "Pause campaign" 200 "$(extract_http_code "$CAMP_PAUSE_RESP")"

log_info "Stopping campaign..."
CAMP_STOP_RESP=$(api_post "$GATEWAY_URL$API_PREFIX/campaigns/$CAMPAIGN_ID/stop" '{}' "$TENANT_TOKEN")
assert_status "Stop campaign" 200 "$(extract_http_code "$CAMP_STOP_RESP")"

log_info "Deleting campaign..."
CAMP_DEL_RESP=$(api_delete "$GATEWAY_URL$API_PREFIX/campaigns/$CAMPAIGN_ID" "$TENANT_TOKEN")
assert_status "Delete campaign" 200 "$(extract_http_code "$CAMP_DEL_RESP")"

# ------------------------------------------------------------------
# STEP 10: CRUD - Users (IAM)
# ------------------------------------------------------------------
log_step "STEP 10: CRUD - IAM Users"

log_info "Listing users..."
USER_LIST_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/users?page=0&size=20" "$TENANT_TOKEN")
assert_status "List users" 200 "$(extract_http_code "$USER_LIST_RESP")"

log_info "Getting user by ID..."
USER_GET_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/users/$TENANT_AGENT_ID" "$TENANT_TOKEN")
assert_status "Get user by ID" 200 "$(extract_http_code "$USER_GET_RESP")"

log_info "Getting user by username..."
USER_BY_UN_RESP=$(api_get "$GATEWAY_URL$API_PREFIX/users/by-username?username=tenant_agent" "$TENANT_TOKEN")
assert_status "Get user by username" 200 "$(extract_http_code "$USER_BY_UN_RESP")"

# ------------------------------------------------------------------
# SUMMARY
# ------------------------------------------------------------------
log_step "TEST SUMMARY"
TOTAL=$((PASS + FAIL))
echo ""
echo -e "${color_green}PASSED: $PASS${color_reset}"
echo -e "${color_red}FAILED: $FAIL${color_reset}"
echo "TOTAL:  $TOTAL"
echo ""

if [ "$FAIL" -eq 0 ]; then
    echo -e "${color_green}╔════════════════════════════════════════════════╗${color_reset}"
    echo -e "${color_green}║         ALL TESTS PASSED SUCCESSFULLY         ║${color_reset}"
    echo -e "${color_green}╚════════════════════════════════════════════════╝${color_reset}"
else
    echo -e "${color_red}╔════════════════════════════════════════════════╗${color_reset}"
    echo -e "${color_red}║            SOME TESTS FAILED                  ║${color_reset}"
    echo -e "${color_red}╚════════════════════════════════════════════════╝${color_reset}"
    echo ""
    echo "Failed tests:"
    for t in "${TESTS[@]}"; do
        if [[ "$t" == FAIL* ]]; then
            echo "  - $t"
        fi
    done
fi

echo ""
echo "Date: $(date)"

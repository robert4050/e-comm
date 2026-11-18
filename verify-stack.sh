#!/usr/bin/env bash
# ==============================================================================
# e-comm Stack Verification Script (Bash/cURL)
# ==============================================================================

GATEWAY_URL="http://localhost:8080"
KEYCLOAK_URL="http://localhost:8180"

echo "=========================================================="
echo " verifying e-comm Platform Services"
echo "=========================================================="

echo -e "\n1. Checking API Gateway Health..."
curl -s "${GATEWAY_URL}/actuator/health" | grep -q "UP" && echo "   [SUCCESS] Gateway is UP" || echo "   [WARN] Gateway not reachable"

echo -e "\n2. Checking Keycloak Realm..."
curl -s "${KEYCLOAK_URL}/realms/e-comm" | grep -q "e-comm" && echo "   [SUCCESS] Keycloak Realm is UP" || echo "   [WARN] Keycloak not reachable"

echo -e "\n3. Checking Public Product Catalog..."
curl -s "${GATEWAY_URL}/api/v1/products" && echo -e "\n   [SUCCESS] Catalog endpoint responding"

echo -e "\n4. Getting Manager JWT Token..."
TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/e-comm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=admin-app&username=manager&password=manager123" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
  echo "   [SUCCESS] Obtained JWT Access Token"
  echo -e "\n5. Creating Product via Gateway with Bearer Token..."
  curl -s -X POST "${GATEWAY_URL}/api/v1/products" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{
      "sku": "SKU-CURL-001",
      "name": "Verified cURL Product",
      "description": "Created via Bash verification script",
      "price": 129.99,
      "category": "Electronics",
      "stockQuantity": 15,
      "imageUrl": "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600"
    }'
  echo -e "\n   [SUCCESS] Product created"
else
  echo "   [WARN] Could not retrieve token"
fi

echo -e "\n=========================================================="
echo " Verification Complete"
echo "=========================================================="

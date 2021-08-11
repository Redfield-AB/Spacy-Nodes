#!/bin/bash

set -e

# we require jq
command -v jq >/dev/null 2>&1 || { echo >&2 "'jq' is required."; exit 1; }

if [ "$#" -ne 4 ]; then
  echo "Usage: $0 <url> <edit-token> <product.md> <changelog.md>" >&2; exit 1
fi

URL=$1
TOKEN=$2
PRODUCT=$(<$3)
CHANGELOG=$(<$4)

# https://stackoverflow.com/a/13466143
json_escape () {
  printf '%s' "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

ESCAPED_PRODUCT=`json_escape "$PRODUCT"`
ESCAPED_CHANGELOG=`json_escape "$CHANGELOG"`

JSON=$(echo "{\"text\":$ESCAPED_PRODUCT}" "{\"changelog\":$ESCAPED_CHANGELOG}" | jq -s add)

# TODO Define and provide more product details within the 
# repository e.g name, style, vendor, icon, hidden, faq.

# https://stackoverflow.com/a/34887246
STATUS=`curl \
  -X PATCH "${URL}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  --data "@-" \
  --fail \
  --silent \
  --write-out "%{http_code}\n" \
  --output /dev/null \
  <<<"$JSON"`
# require HTTP status 2xx
# (consider 3xx an error as well)
if [ "$STATUS" -lt 200 ] || [ "$STATUS" -ge 300 ]; then
  echo "Received unexpected HTTP status $STATUS" >&2; exit 2
fi

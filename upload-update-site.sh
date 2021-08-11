#!/bin/bash

set -e

# we require sshpass to pass the password to sftp
# https://stackoverflow.com/a/5386587
# https://stackoverflow.com/a/62623099 (when running on Mac)
command -v sshpass >/dev/null 2>&1 || { echo >&2 "'sshpass' is required."; exit 1; }

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <sftp-user> <sftp-password> <branch>" >&2; exit 1
fi

SFTP_USER=$1
SFTP_PASSWORD=$2
BRANCH=$3

export SSHPASS=${SFTP_PASSWORD}

# commands prefixed with - are allowed to fail (and cause no error)
BATCH=$(cat <<END
put ./p2/target/*.p2-*.zip update-site/$BRANCH.zip
# need to empty directory by directory (features, plugins)
# because sftp does not allow recursive deletion
-rm update-site/$BRANCH/features/*
-rm update-site/$BRANCH/plugins/*
-rm update-site/$BRANCH/*
-mkdir update-site/$BRANCH
put -r ./p2/target/repository/* update-site/$BRANCH
END
)

# https://stackoverflow.com/a/21494235/388827
echo "$BATCH" | sshpass -e sftp -v -P 2222 -oStrictHostKeyChecking=no -oBatchMode=no -b - ${SFTP_USER}@nodepit.com

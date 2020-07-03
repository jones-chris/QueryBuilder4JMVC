#!/bin/bash

# Use password-stdin so that password does not go to shell's logs.
echo "$DOCKERHUB_TOKEN" | docker login --username joneschris --password-stdin

docker push joneschris/qb4j-mvc:"$PROJECT_VERSION"

# Upload code coverage files.
#      - curl -s https://codecov.io/bash > codecov.sh
#      - bash codecov.sh -t $CODECOV_TOKEN

echo "ENV environment variable is $ENV"

# If DEV env variable, then deploy to Lightsail.
# If PROD env variable, then deploy CloudFormation.
if [ "$ENV" == "dev" ]; then
    chmod +x ./cicd/deployment/deployment_dev.sh
    sh ./cicd/deployment/deployment_dev.sh "$DOCKERHUB_TOKEN"
elif [ "$ENV" == "prod" ]; then
    chmod +x ./cicd/deployment/deployment_prod.sh
    sh ./cicd/deployment/deployment_prod.sh
else
    echo "Did not recognize the ENV, $ENV.  Not deploying."
fi

#!/bin/bash

# Upload code coverage files.
#      - curl -s https://codecov.io/bash > codecov.sh
#      - bash codecov.sh -t $CODECOV_TOKEN

DOCKERHUB_TOKEN=$1
PROJECT_VERSION=$2
IMAGE_EXISTS=$3

# If the previous script, 0_build.bash, exited without a 0 status, then don't deploy.
#if [ "$?" != 0 ]; then
#    echo "Exiting 1_post_build.bash because exit status of previous script is not 0"
#    exit 1
#fi

# If image does not already exist, then log into DockerHub and push image that was built by 0_build.bash.
if [ "$IMAGE_EXISTS" == "false" ]; then
    echo "The docker image tag does not already exist, so logging into DockerHub and push the image"
    echo "$DOCKERHUB_TOKEN" | docker login --username joneschris --password-stdin
    docker push joneschris/qb4j-mvc:"$PROJECT_VERSION"
fi

echo "ENV environment variable is $ENV"

# If DEV env variable, then deploy to Lightsail.
# If PROD env variable, then deploy CloudFormation.
if [ "$ENV" == "dev" ]; then
    echo "Deploying to DEV"
    chmod +x ./cicd/deployment/deployment_dev.sh
    sh ./cicd/deployment/deployment_dev.sh "$PROJECT_VERSION"
elif [ "$ENV" == "prod" ]; then
    echo "Deploying to PROD"
    chmod +x ./cicd/deployment/deployment_prod.sh
    sh ./cicd/deployment/deployment_prod.sh
else
    echo "Did not recognize the ENV, $ENV.  Not deploying."
fi

#!/bin/bash

# Builds uber jar.  Includes running unit tests.
mvn clean install

# Check if the image exists in docker hub already.
IMAGE_EXISTS=$(docker pull joneschris/qb4j-mvc:"$PROJECT_VERSION" > /dev/null && echo "true" || echo "false")

echo "Does the image exist?  $IMAGE_EXISTS"

# If image already exists, then stop script execution and fail the pipeline build.
if [ "$IMAGE_EXISTS" == "true" ]; then
    echo joneschris/qb4j-mvc:$PROJECT_VERSION already exists
    exit 1
else
    docker image build -t joneschris/qb4j-mvc:"$PROJECT_VERSION" --build-arg environment=prod .
    exit 0
fi

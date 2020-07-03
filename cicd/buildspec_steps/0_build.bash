#!/bin/bash

# Give command line arguments user-friendly names.
IMAGE_EXISTS=$1

# Build the uber jar and run unit tests.
mvn clean install

# If image already exists, then stop script execution and fail the pipeline build.
if [ "$IMAGE_EXISTS" == "true" ]; then #todo:  build image locally regardless but don't push if image tag already exists?
    echo "joneschris/qb4j-mvc:$PROJECT_VERSION already exists"
else
    docker image build -t joneschris/qb4j-mvc:"$PROJECT_VERSION" --build-arg environment=prod .
fi

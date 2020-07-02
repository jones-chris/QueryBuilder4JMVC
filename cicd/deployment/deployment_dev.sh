#!/bin/bash

# This script should log into the lightsail instance using the SSH private key, stop the qb4j-api docker container, pull down the new image, start the qb4j-api docker container.
DOCKER_IMAGE_TAG=$1

# Get the private key, user name, and IP address for the lightsail instance to ssh into the lightsail instance.
PRIVATE_KEY=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/ssh_key --with-decryption --output text --query Parameter.Value)
USER_NAME=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/user_name --with-decryption --output text --query Parameter.Value)
IP_ADDRESS=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/ip_address --with-decryption --output text --query Parameter.Value)

# Put the private key in a txt file.
echo "$PRIVATE_KEY" > private_key.txt

chmod 600 private_key.txt

ssh -i private_key.txt "$USER_NAME@$IP_ADDRESS"

# Pull the new docker image down onto the lightsail virtual server.
sudo docker pull joneschris/qb4j-mvc:"$DOCKER_IMAGE_TAG"

# Get the container id to stop.
DOCKER_CONTAINER_ID_TO_STOP=$(sudo docker ps | grep 'qb4j-mvc' | awk '{ print $1 }')

# Stop the docker container.
sudo docker container stop "$DOCKER_CONTAINER_ID_TO_STOP"

# Start the docker container.
sudo nohup docker container run --publish 8080:8080 --detach joneschris/qb4j-mvc:$"DOCKER_IMAGE_TAG"

# Exit ssh session.
exit

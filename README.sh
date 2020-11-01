#!/bin/bash

if [ "$1" = "cleanup" ]
then
  docker-compose down -v
  mvn clean
  docker image rm juplo/jdbc:outbox
  docker image rm juplo/outbox:polling
  exit
fi

if [[
  $(docker image ls -q juplo/jdbc:outbox) == "" ||
  $(docker image ls -q juplo/outbox:polling) == "" ||
  "$1" = "build"
]]
then
  mvn package || exit
else
  echo "Using image existing images:"
  docker image ls juplo/jdbc:outbox
  docker image ls juplo/outbox:polling
fi

trap 'kill $(jobs -p)' EXIT

docker-compose up -d zookeeper kafka

while ! [[ $(zookeeper-shell zookeeper:2181 ls /brokers/ids 2> /dev/null) =~ 1001 ]];
do
  echo "Waiting for kafka...";
  sleep 1;
done

kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 3 --topic outbox


docker-compose up -d jdbc outbox

while ! [[ $(http :8080/actuator/health 2>/dev/null | jq -r .status) == "UP" ]];
do
  echo "Waiting for User-Service...";
  sleep 1;
done


kafkacat -C -b localhost:9092 -t outbox &

echo peter | http :8080/users
echo franz | http :8080/users
echo beate | http :8080/users
http :8080/users
http DELETE :8080/users/franz
http :8080/users

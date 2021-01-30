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

while ! [[ $(docker-compose exec kafka zookeeper-shell zookeeper:2181 ls /brokers/ids 2> /dev/null) =~ 1001 ]];
do
  echo "Waiting for kafka...";
  sleep 1;
done

docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --create --if-not-exists --replication-factor 1 --partitions 3 --topic outbox


docker-compose up -d jdbc outbox kafkacat

while ! [[ $(http :8080/actuator/health 2>/dev/null | jq -r .status) == "UP" ]];
do
  echo "Waiting for User-Service...";
  sleep 1;
done


docker-compose logs -f kafkacat &

for i in `seq 1 20`;
do
  echo peter$i | http :8080/users
  echo uwe$i | http :8080/users
  echo simone$i | http :8080/users
  http DELETE :8080/users/franz$i
  http DELETE :8080/users/simone$i
  echo franz$i | http :8080/users
  echo beate$i | http :8080/users
  http DELETE :8080/users/uwe$i
  sleep 1
done;

sleep 2

#!/bin/bash

if [ "$1" = "cleanup" ]
then
  docker-compose down -v
  mvn clean
  docker image rm juplo/data-jdbc:polling-outbox-2-SNAPSHOT
  docker image rm juplo/outbox-delivery:polling-outbox-2-SNAPSHOT
  exit
fi

docker-compose up -d zookeeper kafka

if [[
  $(docker image ls -q juplo/data-jdbc:polling-outbox-2-SNAPSHOT) == "" ||
  $(docker image ls -q juplo/outbox-delivery:polling-outbox-2-SNAPSHOT) == "" ||
  "$1" = "build"
]]
then
  mvn install || exit
else
  echo "Using image existing images:"
  docker image ls juplo/data-jdbc:polling-outbox-2-SNAPSHOT
  docker image ls juplo/outbox-delivery:polling-outbox-2-SNAPSHOT
fi

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


docker-compose logs --tail=0 -f jdbc kafkacat &

for i in `seq 1 7`;
do
  echo peter$i | http :8080/users
  echo uwe$i | http :8080/users
  echo peter$i | http :8080/users
  echo simone$i | http :8080/users
  echo beate$i | http :8080/users
  http DELETE :8080/users/franz$i
  http DELETE :8080/users/simone$i
  echo beate$i | http :8080/users
  http DELETE :8080/users/beate$i
  echo franz$i | http :8080/users
  echo franz$i | http :8080/users
  echo beate$i | http :8080/users
  http DELETE :8080/users/uwe$i
  sleep 1
done;

docker-compose exec postgres psql -Uoutbox -c'SELECT * FROM outbox;' -Ppager=0  outbox
# "kill" the executions of "docker-compose logs ..."
docker-compose stop jdbc kafkacat

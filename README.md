# Implementation of the Outbox-Pattern for Apache Kafka

This Repository holds the source code for
[a blog-series that explains a technically complete implementation of the outbox-pattern for Apache Kafka](https://juplo.de/implementing-the-outbox-pattern-with-kafka-part-1-the-outbox-table/).

Execute [README.sh](README.sh) in a shell to demonstrate the example:

    ./README.sh

The script will...

* compile the two components,
* package them as Docker-Images,
* start up the components and a minimal Kafka Cluster as containers in a [Compose-Setup](docker-compose.yml),
* execute example-queries (CREATE / DELETE) against the API of [the example-project](https://juplo.de/implementing-the-outbox-pattern-with-kafka-part-0-the-example/) and
* tail the logs of the containers `jdbc` and `kafkacat` to show what is going on.

You can verify the expected outcome of the demonstration by running a command like the following:

    $ docker-compose logs kafkacat | grep peter
    kafkacat_1   | peter1:"CREATED"
    kafkacat_1   | peter2:"CREATED"
    kafkacat_1   | peter3:"CREATED"
    kafkacat_1   | peter4:"CREATED"
    kafkacat_1   | peter5:"CREATED"
    kafkacat_1   | peter6:"CREATED"
    kafkacat_1   | peter7:"CREATED"
    $

The example-output shows, that the CREATE-event for users with "peter" in their username are only issued exactly once, although the script issues several requests for each of these users.

Be aware, that the outcome of the script will be different, if you run it several times.
In order to reproduce the same behaviour, you have to shut down the Compose-Setup before rerunning the script:

    docker-compose down -v
    ./README.sh

To clean up all created artifacts one can run:

    ./README.sh cleanup

#!/bin/bash

echo "Waiting for Kafka Connect to start..."
while [ $(curl -s -o /dev/null -w %{http_code} http://localhost:8083/connectors) -ne 200 ]; do
  echo "Kafka Connect is not ready yet. Waiting..."
  sleep 5
done

echo "Setting up Outbox connector..."
curl -X POST -H "Content-Type: application/json" --data @/etc/kafka-connect/connectors/postgres.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @/etc/kafka-connect/connectors/mongodb.json http://localhost:8083/connectors

echo "Connector setup complete."
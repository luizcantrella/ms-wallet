transfer

- sourceId
- destinationId
- amount
- currency
- timestamp


-> getWalletSource (error if not exists)
-> getWalletDestination (error if not exists)
-> validate if source balance has amount for transaction
-> withdraw source balance
-> deposit source balance   


history


- deposit
- withdraw
- transfer

{
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "walletId" : "123e4567-e89b-12d3-a456-426614174003",
    "type": "deposit",
    "amount": 100.00,
    "currency": "BRL",
    "timestamp": "2023-01-01T12:00:00Z"
}

{
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "walletId" : "123e4567-e89b-12d3-a456-426614174003",
    "type": "withdraw",
    "amount": 50.00,
    "currency": "BRL",
    "timestamp": "2023-01-01T12:00:00Z"
}

{
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "walletSourceId" : "123e4567-e89b-12d3-a456-426614174003",
    "walletDestinationId" : "123e4567-e89b-12d3-a456-426614174003",
    "type": "transfer",
    "amount": 50.00,
    "currency": "BRL",
    "timestamp": "2023-01-01T12:00:00Z"
}


# infra
- Postgres
- Redis
- Kafka
- MongoDB

# plus
- keycloak
- ELK

flow

# ms-wallet
-> crateWallet

-> deposit
--> invalid wallet cache
-> withdraw
--> invalid wallet cache
-> transfer
--> invalid wallet cache

-> getBallance
--> get in DB
--> save balance in cache
-> getHistory
--> get in DB
--> save history in cache

# ms-polling
-> read outBox
-> send to kafka

# ms-transaction-consumer
-> read from kafka
-> save in mongoDB



trans type
T -> destinationId 
D -> sem destinationId
W -> sem destinationId

T -> sem destinationId
D -> destinationId



docker run -d --name control-center --hostname control-center -p 9021:9021 -e CONTROL_CENTER_BOOTSTRAP_SERVERS='kafka-1:9092' -e CONTROL_CENTER_REPLICATION_FACTOR=1 -e CONTROL_CENTER_CONNECT_CLUSTER=http://kafka-connect-1:8083 -e PORT=9021 --add-host host.docker.internal:172.17.0.1 confluentinc/cp-enterprise-control-center:6.0.1



name=mongo-sink-from-postgresql
connector.class=com.mongodb.kafka.connect.MongoSinkConnector
task.max=1
topics=postgresql.public.wallet
connection.uri=mongodb://root:root@mongodb/
database=wallet
transforms=extractAddress
transforms.extractAddress.type=org.apache.kafka.connect.transforms.ExtractField$Value
transforms.extractAddress.field=after


curl -X POST  -H  "Content-Type:application/json" http://localhost:8083/connectors -d @connectors/postgres.json
curl -X POST  -H  "Content-Type:application/json" http://localhost:8083/connectors -d @connectors/mongodb.json

curl -X GET http://localhost:8083/connectors


docker run -d -p 27017:27014 --name mongo -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=root mongo:4.4.4


{ "$and" : [{ "$or" : [{ "source_wallet_id" : "251811f2-0353-4d4d-86aa-b293a74957ba"}, { "destination_wallet_id" : "251811f2-0353-4d4d-86aa-b293a74957ba"}]}, { "timestamp" : { "$lte" : 1748314799000}}]}


1748314799000

1748275042891556
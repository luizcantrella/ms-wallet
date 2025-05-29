## Project Stack

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [Spring Security](https://spring.io/projects/spring-security)
- [Postgres](https://www.postgresql.org)
- [MongoDB](https://www.mongodb.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Kafka](https://kafka.apache.org)
- [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html)
- [Debezium PostgresSQL Source Connector](https://docs.confluent.io/kafka-connectors/debezium-postgres-source/current/overview.html)
- [MongoDB Sink Connector](https://www.mongodb.com/pt-br/docs/kafka-connector/current/sink-connector/)
- [KeyCloak](https://www.keycloak.org/)

## How execute application

- Init the environment with the API by running the docker-compose:
```
docker-compose up --build -d
```

- Load in [Postman](https://www.postman.com/) the collection and environment files in source root:
```
./postman
```

- In Postman you will find a *Validate Connector* request, before start making others request perform the
*validate connectors* and the result expected is:
```
[
    "postgresql-connector",
    "mongo-sink-from-postgresql"
]
```

- After the connectors are configured in Kafka Connect and the ms-wallet is ready to receive request, you can perform
the others requests to access the ms-wallet endpoints and validate the functionalities as:
  - Create Wallet
  - Deposit
  - Withdraw
  - Transfer
  - Consult balance
  - Consult balance at a point in time

- All endpoints need authorization token, so each request have the TAB Authorization configured with Oauth2 using
Password Credentials to retrieve a valid JWT token to access the ms-wallet, to get a token do this:
  - click in the bottom **Get New Access Token** -> **Proceed** -> **User Token**

## Functional and Non-Functional Requirements - Approach to meet them 

### Functional

- Postgres to persist wallet and transactions (the table transactions is working as an outbox table)
- Transactions implemented as Event Sourcing
- Postgres connector (source) send to kafka events of each transaction
- MongoDB replicate transactions from Postgres with a connector sink that listen from kafka topic that hold the transactions events
- To consult a balance at a point in time is performed a query that retrieve all transactions until that time,
and the balance is calculated by transactions history
- All endpoints require authorization, and the user to create a Wallet and retrieve a wallet id to make transactions
are gotten from the access token (JWT)
- Using pessimist lock to not allow two parallel requests that want to change the same resource (wallet) generate
inconsistencies
- Exception handling to show a better response to client that consume the API
- Using ports and adapters design to improve testability e segregate responsibilities between layers in the project
(domain, application and infrastructure)
- Using rich domains to improve how the system works and is protected from business rules that break the system.

### Non-Functional

- To guarantee the traceability of all operations I choose lead with transactions using the Event Sourcing,
with this each transaction is represented as a registry in database having the wallet identifier and a timestamp
to show when that transaction happens
- Due to mission-critical I chose to have two databases (postgres and mongoDB) to apply a command query responsibility segregation (CQRS).
With this the postgres can be dedicated to persist the wallet creations and transactions persistence, and the mongoDB
will deal with the heavy reading, loading the transactions to reconstruct the balance at a moment in the past without
overload the postgres database.
- Using a CDC with connectors from kafka connect we have a better result in replicate data from postgres to mongoDB,
because the source connect listen the chances from database log (WAL), what is faster and don't overload the postgres
as a pooling service/process.
- Another thing that helps the application don't have downtime is the use of Redis to cache data, with that
after a consult in database, the last balance is saved in cache and still their until a new transaction is made,
and the history balance calculation for a point in time is saved their too, so repeated request that don't need
to go to the database or perform a reconstruction from transactions events consume less resource from the service
and database because retrieve the saved data from cache
- Using the mongoDB to perform heavy ready is a good choice due to his horizontal scalability, and as a document oriented
database, if we need to insert new data to have a better auditing, like analyze risk signals, we don't need to
change the database schema, and the process to retrieve the history will not be affected

## Trade-offs due to time constraints

- The monolithic architecture
  - The best approach here is segregate in microservices, we can have one to deal with wallets, other to deal with
  transactions, and specially the transaction server I would apply CQRS, one server to create the transactions,
  and other to retrieve the data
- Balance history
  - Due to the need to reconstruct the transactions to know the balance, having a batching process to take snapshots
  and save the snapshot in database will save time of process, because we can see in snapshot database with that
  balance that user is requesting we already have the value calculated, if not we take a closer snapshot and only
  calculate the difference between snapshot and the data/time that the user is asking
- Kafka connect
  - The kafka connect need a tuning in his configuration to be able to make retries
- Rate limit
  - The application today don't have a rete limit configured, that helps avoid the service been overload with a huge
  amount of request in a short period of time
- Observability
  - Add a log aggregator, specially going to a solution with microservice, add a metrics services like prometheus with
  grafana to create dashboards to monitor the health of services and some business metrics, having a trace in the
  services to identify the data flow between services and process
- Health check
  - add and configure health check with spring actuator to handle the API health in a environment with service discovery
  or kubernetes
- Pessimist lock
  - move pessimist lock to a shared cache, with this we can respond early to requests that are locked, and avoid
  database validations to see if a registry is locked or not
- Idempotency
  - add a idempotency header or a lock to guaranty idempotency in transactions
- API Contract
  - create a openApi 3.0 to better document the application


## Time Tracking

- **Project Setup & Initial Configuration**
  - Initialize Spring Boot project
  - Configure dependencies, folders, and base packages
  - *Time Spent: 1.5h*

- **Digital Wallet Core Features**
  - Implement wallet creation endpoint
  - Develop deposit, withdrawal, and transfer endpoints
  - Business logic validation & transaction handling
  - Unit tests
  - *Time Spent: 4h*
  
- **Kafka Connect Configuration**
  - Set up Kafka Connect with PostgreSQL (source)
  - Configure MongoDB sink connector
  - Test data streaming between Postgres → Kafka → MongoDB 
  - *Time Spent: 2h*

-  **Wallet Balance & Event Sourcing** 
  - Implement balance inquiry endpoint 
  - Develop event-sourcing processor (reconstruct balance from MongoDB transactions)
  - *Time Spent: 1.5h*

- **Security (Keycloak + Spring Security)**
  - Dockerized Keycloak for JWT authentication
  - Integrate Spring Security with Keycloak 
  - Extract token claims for resource access control 
  - *Time Spent: 2h*

- **Testing** 
  - Unit tests (application layer)
  - Integration tests with Testcontainers (Redis, MongoDB) and H2 in memory
  - Mock Spring Security/JWT for authenticated tests 
  - *Time Spent: 2h*

- **Exception Handling & Validation**
  - Custom exceptions
  - Global exception handler (HTTP status codes, error messages)
  - *Time Spent: 1h*

- **Docker Compose Setup**
  - Define services (API, Postgres, Kafka, MongoDB, Keycloak)
  - Configure networking and dependencies
  - Test full stack initialization
  - *Time Spent: 1h*
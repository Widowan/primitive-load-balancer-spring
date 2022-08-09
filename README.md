### Primitive load balancer POC using Spring
##### (See microhttp version [here](https://github.com/Widowan/primitive-load-balancer))

Usage with docker:
```bash
$ ./mvnw clean spring-boot:build-image
$ cd target-service
$ ./mvnw clean spring-boot:build-image
# Running balancer itself (up to 5 targets 8081..8085 by default, or specify more):
$ docker run --network=host -p 8080:8080 load-balancer-sprint:0.0.1-SNAPSHOT [--target.ports="8081 8082 8083"]
# Running target service:
$ docker run -p 8081:8081 target-service:0.0.1-SNAPSHOT --server.port=8081
```

Usage without docker:
```bash
# To run balancer:
$ ./mvnw clean spring-boot:run [-Dspring-boot.run.arguments=--target-ports="8081 8082 8083"]
# To run target:
$ cd target-service
$ ./mvnw clean spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```
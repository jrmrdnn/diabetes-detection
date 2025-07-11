# Diabetes detection with Spring Boot

The project involves developing an application to detect the risk of type 2 diabetes in patients, divided into three
sprints. Each sprint adds specific features and uses Spring Boot-based microservices.

# Add keys to project

```bash
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in private_key.pem -out public_key.pem
```
#!/bin/bash

minikube start
eval "$(minikube docker-env)"

# This script is used to launch the application on the server using the docker-compose file and the docker-compose command.
docker build -t suvedg-user-service ./user
docker build -t suvedg-booking-service ./booking
docker build -t suvedg-wallet-service ./wallet
docker build -t suvedg-h2db-service ./h2db

# alias for kubectl
alias kubectl="minikube kubectl --"

# Deploy images to minikube
  # Database Service
minikube kubectl -- apply -f h2db/h2db-deployment.yaml
minikube kubectl -- apply -f h2db/h2db-service.yaml

  # Movie Ticket Booking Services
minikube kubectl -- apply -f user/deployment/deployment.yaml
minikube kubectl -- apply -f booking/deployment/deployment.yaml
minikube kubectl -- apply -f wallet/deployment/deployment.yaml

  # Expose the services
minikube kubectl -- expose deployment suvedg-user-service --type=LoadBalancer --port=8080
minikube kubectl -- expose deployment suvedg-booking-service --type=LoadBalancer --port=8081
minikube kubectl -- expose deployment suvedg-wallet-service --type=LoadBalancer --port=8082

# Wait for the services to be up and running
sleep 30
minikube kubectl -- port-forward service/suvedg-user-service 8080:8080 &
minikube kubectl -- port-forward service/suvedg-booking-service 8081:8081 &
minikube kubectl -- port-forward service/suvedg-wallet-service 8082:8082 &

# Start tunnel
# minikube tunnel
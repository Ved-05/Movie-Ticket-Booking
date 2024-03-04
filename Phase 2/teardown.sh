#!/bin/bash

# Assume minikube is started
eval "$(minikube docker-env)"

# Delete the services
minikube kubectl -- delete service suvedg-user-service
minikube kubectl -- delete service suvedg-booking-service
minikube kubectl -- delete service suvedg-wallet-service
minikube kubectl -- delete service suvedg-h2db-service

# Delete the deployments
minikube kubectl -- delete deployment suvedg-user-service
minikube kubectl -- delete deployment suvedg-booking-service
minikube kubectl -- delete deployment suvedg-wallet-service
minikube kubectl -- delete deployment suvedg-h2db-service

# Delete hpa
minikube kubectl -- delete hpa suvedg-booking-service-hpa

# Delete the images
docker image rm suvedg-user-service
docker image rm suvedg-booking-service
docker image rm suvedg-wallet-service
docker image rm suvedg-h2db-service

# Minikube stop
minikube stop
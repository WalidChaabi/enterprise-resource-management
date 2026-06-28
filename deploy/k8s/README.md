# ERM platform — example Kubernetes manifests (minikube / kind)
#
# Prerequisites:
#   - kubectl, minikube or kind
#   - Docker images erm-backend:latest and erm-front:latest loaded into the cluster
#     (minikube: eval $(minikube docker-env) && build from docker/)
#
# Deploy:
#   kubectl apply -f namespace.yaml
#   kubectl apply -f configmap.yaml
#   cp secrets.example.yaml secrets.yaml   # edit placeholders first
#   kubectl apply -f secrets.yaml
#   kubectl apply -f postgres.yaml
#   kubectl apply -f backend.yaml
#   kubectl apply -f frontend.yaml
#   kubectl apply -f ingress.yaml          # optional; requires ingress controller
#
# Port-forward (without ingress):
#   kubectl -n erm port-forward svc/erm-front 4200:80
#
# Add to /etc/hosts when using ingress: 127.0.0.1 erm.local

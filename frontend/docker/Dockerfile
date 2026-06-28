# Build stage
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration=production

# Serve stage
FROM nginx:alpine

COPY docker/nginx.conf /etc/nginx/nginx.http.conf
COPY docker/nginx.ssl.conf /etc/nginx/nginx.ssl.conf
COPY docker/proxy-params.conf /etc/nginx/proxy-params.conf
COPY docker/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

COPY --from=build /app/dist/enterprise-resource-management-frontend/browser /usr/share/nginx/html

EXPOSE 80 443

ENTRYPOINT ["/docker-entrypoint.sh"]

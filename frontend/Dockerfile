# ==============================================================================
# Dockerfile — Frontend Angular + PrimeNG (consular-frontend)
# Regla 05: Multi-etapa node:22-alpine -> nginx:1.27-alpine
# ==============================================================================

FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY docker-entrypoint.d/40-config.sh /docker-entrypoint.d/40-config.sh
RUN sed -i 's/\r$//' /docker-entrypoint.d/40-config.sh && chmod +x /docker-entrypoint.d/40-config.sh
COPY --from=build /app/dist/consular-frontend/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=5 \
  CMD wget -qO- http://127.0.0.1:80/assets/config.json | grep -q apiBaseUrl || exit 1


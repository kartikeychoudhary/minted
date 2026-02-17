# Stage 1: Build Angular application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY minted-web/package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY minted-web/ .

# Build for production
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:1.27-alpine

# Install gettext for envsubst
RUN apk add --no-cache gettext

# Copy nginx configuration template
COPY nginx.conf /etc/nginx/conf.d/default.conf.template

# Copy custom entrypoint
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Copy built application
COPY --from=build /app/dist/minted-web/browser /usr/share/nginx/html

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Use custom entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

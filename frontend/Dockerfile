# ==========================================
# Multi-stage Dockerfile for React Frontend
# ==========================================

# Stage 1: Build stage with Node 20
FROM node:20-alpine AS build
WORKDIR /app

# Copy dependency definitions and install
COPY package.json package-lock.json* ./
RUN npm ci || npm install

# Copy source and build production bundle
COPY . .
RUN npm run build

# Stage 2: Serve with lightweight Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

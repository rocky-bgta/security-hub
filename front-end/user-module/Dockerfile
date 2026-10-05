# Stage 1: Build the application
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json yarn.lock ./

RUN yarn install --frozen-lockfile

COPY . .

RUN yarn build

# Stage 2: Production image with Nginx
FROM nginx:1.25-alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets with correct ownership
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 5020

CMD ["nginx", "-g", "daemon off;"]

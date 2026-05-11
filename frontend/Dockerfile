# Frontend Dockerfile - Multi-stage build

# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package files
COPY package*.json ./

# 配置淘宝镜像并安装依赖
RUN npm config set registry https://registry.npmmirror.com
RUN npm install

# Copy source code
COPY . .

# Build for production
RUN npm run build

# 生产阶段 - Nginx
FROM nginx:alpine

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]

# Docker Compose Setup for Nginx Reverse Proxy

This setup provides an nginx reverse proxy that forwards requests to your Spring Boot application running on the host and automatically adds the `X_AUTH_USER: john.doe` header.

## Setup

1. **Start your Spring Boot application** on port 8080:

   ```bash
   ./mvnw spring-boot:run
   ```

2. **Start the nginx reverse proxy**:

   ```bash
   docker-compose up -d
   ```

3. **Access your application** through the nginx proxy:
   - Open <http://localhost> (port 80)
   - The nginx proxy will automatically add the `X_AUTH_USER: john.doe` header
   - You should be automatically redirected to the dashboard

## How it works

- **nginx** runs in a Docker container on port 80
- **Spring Boot app** runs on the host on port 8080 (for easy debugging)
- nginx forwards all requests to localhost:8080 and adds the header `X_AUTH_USER: john.doe`
- The HeaderAuthenticationFilter detects the header and automatically authenticates the user
- If accessing `/` or `/login` with the header, the user is redirected to `/dashboard`

## Testing

1. **Direct access** (without header):

   ```bash
   curl http://localhost:8080/
   # Will show login page
   ```

2. **Through nginx proxy** (with header):

   ```bash
   curl http://localhost/
   # Will redirect to dashboard (302 redirect)
   ```

3. **API access through proxy**:

   ```bash
   curl http://localhost/api/user/info
   # Returns user info for john.doe
   ```

## User Information

The application creates a demo user during startup:

- **Username**: john.doe
- **Email**: <john.doe@company.com>
- **Name**: John Doe
- **Role**: USER
- **Auth Method**: HEADER

## Stopping

To stop the nginx proxy:

```bash
docker-compose down
```

Your Spring Boot application will continue running on the host for debugging.

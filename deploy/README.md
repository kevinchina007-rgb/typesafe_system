# Production Deployment

This repository can now be deployed behind a public reverse proxy without changing the business code path.

## Topology

- Public traffic terminates at Caddy on `80/443`
- Caddy reverse proxies to the backend on `127.0.0.1:19095`
- The backend serves:
  - `/api/*`
  - `/uploads/*`
  - `/pay/*`
  - the built frontend dist

This keeps the application same-origin in production, which simplifies session cookies, uploads, and payment links.

## Prerequisites

- A public DNS name pointing to this machine
- Ports `80` and `443` reachable from the internet
- PostgreSQL running and reachable by the backend
- Node, npm, Java, and sbt installed on the host
- Caddy installed on the host

## Environment

Copy [E:\typesafe\template\deploy\.env.production.example](E:/typesafe/template/deploy/.env.production.example) into a real environment file and set:

- `TRAVEL_PUBLIC_HOST`
- `TRAVEL_DB_PASSWORD`
- `TRAVEL_PAYMENT_LINK_SECRET`
- `TRAVEL_SESSION_COOKIE_DOMAIN`

## Start the app

Run:

```powershell
$env:TRAVEL_PUBLIC_HOST='your-domain.example.com'
$env:TRAVEL_DB_PASSWORD='replace-with-a-strong-password'
$env:TRAVEL_PAYMENT_LINK_SECRET='replace-with-a-long-random-secret'
$env:TRAVEL_SESSION_COOKIE_DOMAIN='your-domain.example.com'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'E:\typesafe\template\scripts\start-travel-platform-production.ps1'
```

This will:

- build the frontend dist
- switch runtime config to same-origin mode
- start the backend on `127.0.0.1:19095`

## Start Caddy

Use [E:\typesafe\template\deploy\Caddyfile](E:/typesafe/template/deploy/Caddyfile) and expose:

- `80/tcp`
- `443/tcp`

Minimal example:

```powershell
$env:TRAVEL_PUBLIC_HOST='your-domain.example.com'
$env:TRAVEL_ACME_EMAIL='ops@your-domain.example.com'
caddy run --config E:\typesafe\template\deploy\Caddyfile
```

## Result

After DNS and Caddy are working, the public address is:

- `https://your-domain.example.com`

The backend remains internal:

- `http://127.0.0.1:19095`

## What this does not solve automatically

- OS firewall rules
- router / cloud security group exposure
- DNS ownership
- Caddy installation
- PostgreSQL backup / monitoring / rotation
- DDoS / rate limiting / WAF

Those are infrastructure tasks and must still be completed outside the repository.

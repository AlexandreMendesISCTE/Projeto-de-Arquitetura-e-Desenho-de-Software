# Deployment Directory

This directory contains Docker deployment configuration files.

## Files

- `docker-compose.yml` - Docker Compose configuration
- `Dockerfile` - Multi-stage Docker build file

## Usage

### Important: Environment Variables

The `.env` file must be in the **project root** (not in this directory). Docker Compose will read it automatically when using the `--env-file` flag.

### Commands

All docker-compose commands should be run from the **project root** with the `--env-file .env` flag:

```bash
# From project root:
docker-compose -f deployment/docker-compose.yml --env-file .env up -d
docker-compose -f deployment/docker-compose.yml --env-file .env down
docker-compose -f deployment/docker-compose.yml --env-file .env build
```

### Using the Deploy Script

The easiest way to deploy is using the script in `scripts/deploy.sh`:

```bash
./scripts/deploy.sh
```

This script automatically uses the correct paths and `.env` file.

## Environment Variables

The following environment variables are read from `.env` in the project root:

- `VITE_N8N_WEBHOOK_URL` - n8n webhook URL
- `VITE_GOOGLE_MAPS_API_KEY` - Google Maps API key (required)
- `VITE_OSRM_BASE_URL` - OSRM routing service URL
- `VITE_NOMINATIM_BASE_URL` - Nominatim geocoding service URL
- `VITE_MAP_DEFAULT_CENTER_LAT` - Default map center latitude
- `VITE_MAP_DEFAULT_CENTER_LNG` - Default map center longitude
- `VITE_MAP_DEFAULT_ZOOM` - Default map zoom level

**Note:** These variables are baked into the JavaScript bundle at BUILD TIME. If you change `.env` values, you must rebuild the Docker image:

```bash
docker-compose -f deployment/docker-compose.yml --env-file .env build --no-cache
docker-compose -f deployment/docker-compose.yml --env-file .env up -d
```

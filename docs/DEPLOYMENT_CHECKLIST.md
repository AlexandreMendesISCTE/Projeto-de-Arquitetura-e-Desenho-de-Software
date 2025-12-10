# Deployment Checklist - Map Route Explorer

## ✅ Changes Made for Git Commit

### Files Modified:

- ✅ `docker-compose.yml` - Removed Traefik labels, changed port from 81 to 8082, updated n8n webhook URL
- ✅ `DEPLOY.md` - Updated with correct ports and URLs
- ✅ `N8N_WORKFLOW_GUIDE.md` - Updated webhook URLs
- ✅ `.gitignore` - Added nginx-logs directory

### Files Created:

- ✅ `DEPLOY_PORTAINER.md` - Complete Portainer deployment guide
- ✅ `DEPLOYMENT_CHECKLIST.md` - This file

## 📋 Pre-Deployment Checklist

### 1. Git Commit

- [ ] Review all changes
- [ ] Commit changes:

  ```bash
  git add .
  git commit -m "fix: update docker-compose for Portainer deployment

  - Remove Traefik labels (not used)
  - Change port from 81 to 8082 to avoid conflicts
  - Update n8n webhook URL to port 5678
  - Add Portainer deployment guide
  - Update documentation with correct URLs"
  git push
  ```

### 2. Server Preparation

- [ ] SSH access to Ubuntu server (192.168.100.178)
- [ ] Docker and Docker Compose installed
- [ ] Port 8082 available
- [ ] n8n running and accessible on port 5678

### 3. Environment Variables

- [ ] Google Maps API Key obtained
- [ ] n8n webhook URL confirmed (port 5678 or 81 via NPM)
- [ ] All environment variables documented

## 🚀 Deployment Steps

### Step 1: Clone Repository on Server

```bash
# SSH to server
ssh yocoms@192.168.100.178

# Create project directory
mkdir -p ~/projects/map-route-explorer
cd ~/projects/map-route-explorer

# Clone repository
git clone <your-repo-url> .
```

### Step 2: Create .env File

```bash
# Create .env file
nano .env
```

Add:

```env
VITE_N8N_WEBHOOK_URL=http://192.168.100.178:5678/webhook/chat
VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1
VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org
VITE_MAP_DEFAULT_CENTER_LAT=38.7223
VITE_MAP_DEFAULT_CENTER_LNG=-9.1393
VITE_MAP_DEFAULT_ZOOM=13
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
```

### Step 3: Deploy via Portainer

**Option A: Repository Method (Recommended)**

1. Open Portainer: `http://192.168.100.178:9000`
2. Go to **Stacks** → **Add Stack**
3. Name: `map-route-explorer`
4. Method: **Repository**
5. Repository URL: Your Git repo URL
6. Compose Path: `docker-compose.yml`
7. Add environment variables (from .env file)
8. Click **Deploy the stack**

**Option B: Web Editor Method**

1. Open Portainer: `http://192.168.100.178:9000`
2. Go to **Stacks** → **Add Stack**
3. Name: `map-route-explorer`
4. Method: **Web editor**
5. Paste `docker-compose.yml` content
6. Add environment variables
7. Click **Deploy the stack**

### Step 4: Verify Deployment

```bash
# Check containers
docker ps | grep map-route-explorer

# Check logs
docker logs map-route-explorer-app
docker logs map-route-explorer-nginx

# Test health endpoint
curl http://192.168.100.178:8082/nginx/proxy/health
```

### Step 5: Access Application

- Direct: `http://192.168.100.178:8082/nginx/proxy`
- Via Nginx Proxy Manager: Configure proxy to `http://192.168.100.178:8082/nginx/proxy`

### Step 6: Configure n8n Workflow

1. Open n8n: `http://192.168.100.178:5678`
2. Create new workflow
3. Add **Webhook** node:
   - Method: POST
   - Path: `/webhook/chat`
   - Response: When Last Node Finishes
4. Add processing nodes (see `N8N_WORKFLOW_GUIDE.md`)
5. Activate workflow
6. Test webhook:
   ```bash
   curl -X POST http://192.168.100.178:5678/webhook/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"test","currentRoute":{"origin":null,"destination":null,"waypoints":[]},"waitingForInput":null,"timestamp":"2025-01-15T10:30:00.000Z"}'
   ```

## 🔧 Troubleshooting

### Container won't start

- Check logs in Portainer
- Verify environment variables
- Check port 8082 availability

### Application not loading

- Verify containers are running: `docker ps`
- Check nginx logs: `docker logs map-route-explorer-nginx`
- Test health endpoint

### Chat not working

- Verify n8n webhook URL in environment variables
- Check n8n workflow is active
- Test webhook directly with curl
- Check browser console for errors

### Build fails

- Verify Google Maps API Key is set
- Check Docker build logs
- Ensure all dependencies are in package.json

## 📚 Documentation

- `DEPLOY.md` - General deployment guide
- `DEPLOY_PORTAINER.md` - Portainer-specific guide
- `N8N_WORKFLOW_GUIDE.md` - n8n workflow configuration
- `README.md` - Project overview

## 🎯 Next Steps After Deployment

1. ✅ Configure n8n workflow for chatbot
2. ✅ Test route calculation with Google Maps API
3. ✅ Test chatbot integration
4. ✅ Configure Nginx Proxy Manager (optional)
5. ✅ Set up SSL/HTTPS (optional, recommended)
6. ✅ Monitor resource usage

## 📝 Notes

- Environment variables are injected at **build time**, not runtime
- To change environment variables, rebuild the image
- First build may take 5-10 minutes
- Google Maps API Key is **required** for route calculation
- n8n workflow must be active for chatbot to work

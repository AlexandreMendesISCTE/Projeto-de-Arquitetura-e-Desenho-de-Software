#!/bin/bash

# Deploy Script for Map Route Explorer
# This script rebuilds and deploys the application with updated webhook URL

set -e  # Exit on error

echo "🚀 Map Route Explorer - Deploy Script"
echo "======================================"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found!"
    echo ""
    echo "Creating .env from template..."
    
    if [ -f env.template ]; then
        cp env.template .env
        echo "✅ Created .env file from template"
        echo ""
        echo "⚠️  Please review .env file and update if needed:"
        echo "   nano .env"
        echo ""
        read -p "Press Enter to continue or Ctrl+C to exit and edit .env first..."
    else
        echo "❌ env.template not found!"
        echo "Please create .env file manually with:"
        echo ""
        echo "VITE_N8N_WEBHOOK_URL=https://yocomsn8n.duckdns.org/webhook/67c709f2-7099-4b5e-ba28-1f669b3487b1"
        echo "VITE_OSRM_BASE_URL=http://router.project-osrm.org/route/v1"
        echo "VITE_NOMINATIM_BASE_URL=https://nominatim.openstreetmap.org"
        echo "VITE_MAP_DEFAULT_CENTER_LAT=38.7223"
        echo "VITE_MAP_DEFAULT_CENTER_LNG=-9.1393"
        echo "VITE_MAP_DEFAULT_ZOOM=13"
        echo "VITE_GOOGLE_MAPS_API_KEY="
        echo ""
        exit 1
    fi
fi

echo "📋 Current configuration:"
echo "------------------------"
grep "VITE_N8N_WEBHOOK_URL" .env || echo "VITE_N8N_WEBHOOK_URL not set"
echo ""

# Stop existing container
echo "🛑 Stopping existing container..."
docker-compose down || true
echo ""

# Remove old images (optional)
read -p "🗑️  Remove old Docker images? (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Removing old images..."
    docker-compose down --rmi local || true
    echo ""
fi

# Build new image
echo "🔨 Building Docker image..."
docker-compose build --no-cache
echo ""

# Start container
echo "▶️  Starting container..."
docker-compose up -d
echo ""

# Wait for container to be healthy
echo "⏳ Waiting for container to be ready..."
sleep 5

# Check container status
echo "📊 Container status:"
docker ps | grep map-route-explorer || echo "⚠️  Container not running!"
echo ""

# Show logs
echo "📜 Recent logs:"
echo "---------------"
docker logs map-route-explorer-app --tail 20
echo ""

# Test webhook
echo "🧪 Testing webhook..."
WEBHOOK_URL=$(grep "VITE_N8N_WEBHOOK_URL" .env | cut -d '=' -f2)

if [ ! -z "$WEBHOOK_URL" ]; then
    echo "Testing: $WEBHOOK_URL"
    
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$WEBHOOK_URL" \
        -H "Content-Type: application/json" \
        -d '{
            "message": "Deploy test",
            "currentRoute": {
                "origin": null,
                "destination": null,
                "waypoints": []
            },
            "waitingForInput": null,
            "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.000Z)'"
        }' 2>&1)
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
        echo "✅ Webhook test successful! (HTTP $HTTP_CODE)"
        echo "Response: $BODY"
    else
        echo "⚠️  Webhook returned HTTP $HTTP_CODE"
        echo "Response: $BODY"
    fi
else
    echo "⚠️  VITE_N8N_WEBHOOK_URL not found in .env"
fi

echo ""
echo "======================================"
echo "✅ Deployment complete!"
echo ""
echo "📍 Access your application at:"
echo "   - Local: http://localhost:8082"
echo "   - Server: http://192.168.100.178:8082"
echo "   - Public: https://yocomsmap.duckdns.org"
echo ""
echo "🔍 Useful commands:"
echo "   - View logs: docker logs map-route-explorer-app -f"
echo "   - Restart: docker-compose restart"
echo "   - Stop: docker-compose down"
echo "   - Rebuild: docker-compose build --no-cache"
echo ""
echo "📚 Documentation:"
echo "   - QUICK_DEPLOY.md - Quick deployment guide"
echo "   - WEBHOOK_UPDATE_GUIDE.md - Complete webhook guide"
echo "   - CHANGES_SUMMARY.md - Summary of changes"
echo ""


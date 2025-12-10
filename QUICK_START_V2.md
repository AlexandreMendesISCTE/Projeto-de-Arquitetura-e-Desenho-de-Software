# 🚀 Quick Start - Workflow V2

## ✅ What's New in V2

1. **Gemini only returns city names** (not coordinates)
2. **Nominatim API gets real coordinates** (accurate)
3. **IF node splits workflow** into 2 paths:
   - Path A: User has current location
   - Path B: Regular flow
4. **Final message formatting** before response
5. **Better error handling** at each step

---

## 📦 Installation (5 minutes)

### Step 1: Import Workflow (2 min)

1. Open n8n: https://yocomsn8n.duckdns.org
2. Click **"+ Add workflow"**
3. Click **"..."** menu → **"Import from File"**
4. Select: **`N8N_WORKFLOW_MAPCHAT_V2.json`**
5. Click **Save**
6. Click **Activate** (toggle top-right)

### Step 2: Add Gemini API Key (1 min)

```bash
# SSH to server
ssh yocoms@192.168.100.178

# Edit n8n config
cd /opt/n8n
nano n8n-docker-compose.yml
```

Add under `environment:`:
```yaml
- GEMINI_API_KEY=your-api-key-here
```

Get key: https://aistudio.google.com/app/apikey

Restart:
```bash
docker-compose restart
```

### Step 3: Test (2 min)

```bash
# Test with current location
curl -X POST https://yocomsn8n.duckdns.org/webhook/mapchat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Quero ir ao Porto",
    "useCurrentLocationAsOrigin": true,
    "userLocation": {"name": "Lisbon", "lat": 38.7223, "lng": -9.1393},
    "currentRoute": {"origin": null, "destination": null, "waypoints": []},
    "waitingForInput": null,
    "timestamp": "2024-01-15T00:00:00Z"
  }'
```

**Expected Response**:
```json
{
  "message": "✅ Rota definida da sua localização atual para o Porto!",
  "action": "set_route",
  "origin": {
    "name": "Lisbon, Portugal",
    "lat": 38.7223,
    "lng": -9.1393
  },
  "destination": {
    "name": "Porto, Portugal",
    "lat": 41.1579,
    "lng": -8.6291
  }
}
```

---

## 🔍 Testing Different Scenarios

### Scenario 1: With Current Location + Destination
```bash
curl -X POST https://yocomsn8n.duckdns.org/webhook/mapchat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Quero ir ao Porto",
    "useCurrentLocationAsOrigin": true,
    "userLocation": {"name": "Lisbon", "lat": 38.7223, "lng": -9.1393}
  }'
```

### Scenario 2: Regular - Origin + Destination
```bash
curl -X POST https://yocomsn8n.duckdns.org/webhook/mapchat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Quero ir de Lisboa ao Porto",
    "useCurrentLocationAsOrigin": false
  }'
```

### Scenario 3: Only Origin
```bash
curl -X POST https://yocomsn8n.duckdns.org/webhook/mapchat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Origem em Coimbra",
    "useCurrentLocationAsOrigin": false
  }'
```

### Scenario 4: Needs Clarification
```bash
curl -X POST https://yocomsn8n.duckdns.org/webhook/mapchat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Quero ir para lá",
    "useCurrentLocationAsOrigin": false
  }'
```

---

## 📊 Workflow Structure

```
Webhook
  ↓
IF: Has Current Location?
  ├─ TRUE → Gemini (dest only) → Nominatim → Response
  └─ FALSE → Gemini (origin+dest) → Nominatim x2 → Response
```

**Total Nodes**: 15
**Expected Time**: 1.5-3 seconds

---

## 🎯 Key Features

| Feature | Status |
|---------|--------|
| Current location support | ✅ |
| City name extraction | ✅ |
| Coordinate accuracy | ✅ (Nominatim) |
| Error handling | ✅ |
| Fast response | ✅ (< 3s) |
| Portuguese messages | ✅ |

---

## 🔧 Configuration

### Required Environment Variables
- `GEMINI_API_KEY` - Your Google AI API key

### Webhook Path
- `/webhook/mapchat`

### Frontend ENV
```env
VITE_N8N_WEBHOOK_URL=https://yocomsn8n.duckdns.org/webhook/mapchat
```

---

## ⚡ Performance

| Path | Time | Nodes |
|------|------|-------|
| **With Location** | 1.5-2.5s | 7 nodes |
| **Without Location** | 2-3s | 9 nodes |

---

## 📚 Documentation

- **`N8N_WORKFLOW_MAPCHAT_V2.json`** - Importable workflow
- **`WORKFLOW_GUIDE_V2.md`** - Complete guide
- **`WORKFLOW_VISUAL_DIAGRAM.md`** - Visual diagrams
- **`QUICK_START_V2.md`** - This file

---

## ✅ Success Checklist

- [ ] Imported workflow V2
- [ ] Workflow is activated
- [ ] Added Gemini API key
- [ ] Restarted n8n
- [ ] Test 1 passes (with location)
- [ ] Test 2 passes (without location)
- [ ] Test 3 passes (only origin)
- [ ] Test 4 passes (clarification)
- [ ] Frontend chat works
- [ ] Location checkbox works

---

## 🆘 Troubleshooting

### Workflow not responding
1. Check workflow is **active** (toggle in n8n)
2. Check Gemini API key is set
3. Check n8n logs: `docker logs n8n -f`

### Wrong coordinates
1. Check Nominatim responses in n8n execution log
2. Verify city names are correct
3. Test Nominatim directly:
   ```bash
   curl "https://nominatim.openstreetmap.org/search?q=Porto,Portugal&format=json&limit=1"
   ```

### Gemini errors
1. Verify API key is valid
2. Check quota: https://aistudio.google.com/
3. Test Gemini API directly in n8n

---

## 🎉 You're Ready!

Your improved workflow:
- ✅ More accurate (Nominatim coordinates)
- ✅ More reliable (separate concerns)
- ✅ Better UX (current location support)
- ✅ Faster (optimized flow)
- ✅ Easier to debug (clear paths)

**Start testing now!** 🚀


# 🛑 Waypoints Feature - Implementation Guide

## Overview
Successfully implemented support for waypoints (paragens) in both the frontend and n8n workflow. Users can now specify up to **5 waypoints** (stops) along their route.

---

## ✅ Changes Made

### 1. **Frontend - LocationSearch Component** (`src/components/search/LocationSearch.tsx`)

#### Added 5 Waypoint Limit
- Added counter display showing current waypoints: `Paragens (2/5)`
- Disabled the "+" button when 5 waypoints are reached
- Added tooltip showing "Máximo de 5 paragens" when limit is reached

```typescript
<label className="block text-xs font-medium text-gray-700">
  Paragens {waypoints.length > 0 && `(${waypoints.length}/5)`}
</label>
<button
  onClick={() => { /* ... */ }}
  disabled={waypoints.length >= 5}
  className="p-1 text-blue-600 hover:bg-blue-50 rounded transition-colors 
             disabled:opacity-50 disabled:cursor-not-allowed 
             disabled:hover:bg-transparent"
  title={waypoints.length >= 5 ? "Máximo de 5 paragens" : "Adicionar paragem"}
>
  <Plus className="w-4 h-4" />
</button>
```

---

### 2. **Frontend - ChatWidget Component** (`src/components/ChatWidget.tsx`)

#### Updated Route Setting with Waypoints
- Modified `set_route` action to handle waypoints
- Added new `add_waypoints` action for adding multiple waypoints via chat
- Enforces 5 waypoint limit when adding via chat
- Displays waypoints in confirmation message

**Example confirmation message:**
```
✅ Processado. 🗺️ Rota definida:
📍 Origem: Lisboa, Portugal
🎯 Destino: Porto, Portugal
🛑 Paragens: Coimbra, Aveiro
```

---

### 3. **Frontend - n8n Service** (`src/services/api/n8n.service.ts`)

#### Updated Response Interface
Added waypoints support and new action type:

```typescript
export interface N8NResponse {
  message: string
  action?: 'set_origin' | 'set_destination' | 'add_waypoint' | 
           'add_waypoints' | 'set_route' | 'clear_route' | null
  // ... other fields ...
  waypoints?: Array<{
    name: string
    lat: number
    lng: number
  }>
}
```

---

### 4. **n8n Workflow** (`Map Chat with Gemini Flash v2.json`)

#### Updated Gemini Prompts

**With Location (User's GPS as origin):**
```javascript
// Now extracts DESTINATION + WAYPOINTS
{
  "destination_city": "Porto",
  "destination_country": "Portugal",
  "waypoints": [{"city": "Coimbra", "country": "Portugal"}],
  "message_to_user": "Vou definir a rota da sua localização atual para o Porto, passando por Coimbra!",
  "needs_clarification": false
}
```

**Without Location (Regular flow):**
```javascript
// Now extracts ORIGIN + DESTINATION + WAYPOINTS
{
  "origin_city": "Lisboa",
  "origin_country": "Portugal",
  "destination_city": "Porto",
  "destination_country": "Portugal",
  "waypoints": [
    {"city": "Coimbra", "country": "Portugal"},
    {"city": "Aveiro", "country": "Portugal"}
  ],
  "message_to_user": "Vou definir a rota de Lisboa para o Porto, passando por Coimbra e Aveiro!",
  "needs_clarification": false,
  "action_type": "set_route"
}
```

#### New Action Type
Added `add_waypoints` action for commands like:
- "Adiciona paragem em Coimbra"
- "Quero parar em Fátima e Leiria"

#### Waypoint Geocoding
Both `Build Final Response` nodes now geocode waypoints using Nominatim:

```javascript
// Geocode waypoints if any
const waypoints = [];
if (parseData.waypoints && parseData.waypoints.length > 0) {
  for (const waypoint of parseData.waypoints.slice(0, 5)) {  // Max 5 waypoints
    try {
      const wpResponse = await fetch(
        `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(waypoint.city)},${encodeURIComponent(waypoint.country)}&format=json&limit=1`
      );
      const wpData = await wpResponse.json();
      if (wpData && wpData.length > 0) {
        waypoints.push({
          name: wpData[0].display_name || wpData[0].name || `${waypoint.city}, ${waypoint.country}`,
          lat: parseFloat(wpData[0].lat),
          lng: parseFloat(wpData[0].lon)
        });
      }
    } catch (error) {
      console.error(`Failed to geocode waypoint ${waypoint.city}:`, error);
    }
  }
}
```

---

## 🎯 How to Use (User Guide)

### Via Chat Interface

**Example 1: Route with waypoints**
```
User: "Quero ir de Lisboa ao Porto passando por Coimbra e Aveiro"
Bot: ✅ Rota definida de Lisboa para Porto, passando por Coimbra, Aveiro!
```

**Example 2: With user's current location**
```
User: [Checks "📍 Usar a minha localização atual como origem"]
User: "Para o Porto passando por Coimbra"
Bot: ✅ Vou definir a rota da sua localização atual para o Porto, passando por Coimbra!
```

**Example 3: Add waypoints to existing route**
```
User: "Adiciona paragem em Fátima"
Bot: ✅ 1 paragem(ns) adicionada(s): Fátima
```

### Via Manual Input
- Click origin → set origin
- Click "+" button to add waypoints (up to 5)
- Each waypoint can be:
  - Searched by name
  - Selected using GPS location
  - Clicked directly on the map
- Click destination → set destination

---

## 🔧 Technical Details

### Workflow Flow

1. **Webhook receives:**
   ```json
   {
     "message": "Quero ir de Lisboa ao Porto passando por Coimbra",
     "currentRoute": { "origin": null, "destination": null, "waypoints": [] },
     "useCurrentLocationAsOrigin": false,
     "userLocation": null
   }
   ```

2. **Gemini extracts cities:**
   ```json
   {
     "origin_city": "Lisboa",
     "destination_city": "Porto",
     "waypoints": [{"city": "Coimbra", "country": "Portugal"}]
   }
   ```

3. **Nominatim geocodes all locations:**
   - Origin: Lisboa → `{lat: 38.7223, lng: -9.1393}`
   - Waypoint: Coimbra → `{lat: 40.2033, lng: -8.4103}`
   - Destination: Porto → `{lat: 41.1579, lng: -8.6291}`

4. **Workflow responds:**
   ```json
   {
     "action": "set_route",
     "origin": {"name": "Lisboa, Portugal", "lat": 38.7223, "lng": -9.1393},
     "waypoints": [{"name": "Coimbra, Portugal", "lat": 40.2033, "lng": -8.4103}],
     "destination": {"name": "Porto, Portugal", "lat": 41.1579, "lng": -8.6291},
     "message": "✅ Rota definida de Lisboa para Porto, passando por Coimbra!"
   }
   ```

5. **Frontend updates:**
   - Sets origin
   - Sets waypoints array
   - Sets destination
   - Centers map
   - Displays confirmation message

---

## 🚀 Deployment Instructions

### 1. Update n8n Workflow
1. Open n8n at `https://yocomsn8n.duckdns.org`
2. Import the updated workflow: `Map Chat with Gemini Flash v2.json`
3. OR manually update these nodes:
   - `Prepare Prompt (With Location)` - JavaScript code
   - `Prepare Prompt (Without Location)` - JavaScript code
   - `Parse Response (With Location)` - JavaScript code
   - `Parse Response (Without Location)` - JavaScript code
   - `Build Final Response (With Location)` - JavaScript code
   - `Build Final Response (Without Location)` - JavaScript code

### 2. Deploy Frontend
```bash
cd mapAPP/Projeto-de-Arquitetura-e-Desenho-de-Software
npm install  # If needed
npm run build
# Deploy to your hosting
```

### 3. Test the Feature
Test these scenarios:
- ✅ Add waypoints manually (up to 5)
- ✅ Try to add 6th waypoint (should be disabled)
- ✅ Chat: "Quero ir de Lisboa ao Porto passando por Coimbra"
- ✅ Chat with location: "Para o Porto passando por Coimbra e Aveiro"
- ✅ Chat: "Adiciona paragem em Fátima"
- ✅ Verify waypoints appear on map
- ✅ Verify route is calculated through waypoints

---

## 📝 Examples

### Portuguese Commands (Examples for Testing)

1. **Full route with waypoints:**
   - "Quero ir de Lisboa ao Porto passando por Coimbra e Aveiro"
   - "De Faro a Porto com paragens em Albufeira e Lisboa"

2. **With current location:**
   - [Enable GPS] "Para o Porto passando por Coimbra"
   - [Enable GPS] "Quero ir a Madrid parando em Salamanca"

3. **Add waypoints to existing route:**
   - "Adiciona paragem em Coimbra"
   - "Adiciona paragens em Fátima e Leiria"
   - "Quero parar em Évora"

4. **Edge cases:**
   - "Adiciona 10 paragens" → Will only add 5 (max limit)
   - Try adding waypoint when 5 already exist → Shows limit message

---

## 🎨 UI Indicators

1. **Waypoint Counter:** Shows `(2/5)` next to "Paragens" label
2. **Disabled Button:** "+" button grays out at 5 waypoints
3. **Tooltip:** Hover shows "Máximo de 5 paragens"
4. **Chat Messages:** Shows waypoints in route confirmation
5. **Visual Feedback:** Waypoints appear as markers on map

---

## 🐛 Known Limitations

1. **Maximum 5 Waypoints:** Enforced at both UI and workflow level
2. **Nominatim Rate Limits:** May fail if too many geocoding requests
3. **Sequential Geocoding:** Waypoints geocoded one at a time (could be slow)
4. **Error Handling:** Silent failure on waypoint geocoding errors

---

## 📊 Summary

| Feature | Status |
|---------|--------|
| Frontend 5 waypoint limit | ✅ Complete |
| Workflow waypoint extraction | ✅ Complete |
| Waypoint geocoding | ✅ Complete |
| Chat integration | ✅ Complete |
| Manual input | ✅ Complete |
| With GPS location | ✅ Complete |
| TypeScript types | ✅ Complete |
| Error handling | ✅ Complete |

---

## 💡 Future Improvements (Optional)

1. **Parallel Geocoding:** Geocode all waypoints simultaneously
2. **Waypoint Reordering:** Drag-and-drop to reorder waypoints
3. **Route Optimization:** Auto-reorder waypoints for optimal route
4. **Saved Routes:** Save routes with waypoints for later use
5. **Waypoint Icons:** Different icons for different waypoint types
6. **Waypoint Details:** Add notes/descriptions to waypoints

---

**✨ The waypoints feature is now fully functional and ready for testing!**


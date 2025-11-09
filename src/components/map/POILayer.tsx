import { Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import { useRouteStore } from '../../store/route.store'
import { usePOIStore } from '../../store/poi.store'
import { usePOIs } from '../../hooks/usePOIs'
import { POI } from '../../services/api/poi.service'

const createPOIIcon = (category?: string) => {
  const getColor = () => {
    switch (category) {
      case 'restaurant':
      case 'cafe':
      case 'fast_food':
        return '#ef4444' // red
      case 'fuel':
        return '#3b82f6' // blue
      case 'parking':
        return '#10b981' // green
      case 'attraction':
      case 'tourism':
      case 'museum':
      case 'hotel':
        return '#f59e0b' // orange
      default:
        return '#6b7280' // gray
    }
  }

  return L.divIcon({
    className: 'poi-marker',
    html: `<div style="background-color: ${getColor()}; width: 16px; height: 16px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8],
  })
}

const POILayer = () => {
  const { route } = useRouteStore()
  const { enabled } = usePOIStore()
  const { data: pois = [], isLoading } = usePOIs(route)

  // Don't render if POIs are disabled or no route
  if (!enabled || isLoading || !route || pois.length === 0) {
    return null
  }

  return (
    <>
      {pois.map((poi: POI) => (
        <Marker key={poi.id} position={[poi.lat, poi.lng]} icon={createPOIIcon(poi.category)}>
          <Popup>
            <div className="font-semibold text-sm">{poi.name}</div>
            {poi.category && (
              <div className="text-xs text-gray-500 capitalize">{poi.category}</div>
            )}
          </Popup>
        </Marker>
      ))}
    </>
  )
}

export default POILayer


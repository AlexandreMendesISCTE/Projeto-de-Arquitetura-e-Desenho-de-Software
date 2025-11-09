import { Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import { useRouteStore } from '../../store/route.store'

// Fix for default marker icons in React Leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

const createCustomIcon = (color: string) => {
  return L.divIcon({
    className: 'custom-marker',
    html: `<div style="background-color: ${color}; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
    iconSize: [24, 24],
    iconAnchor: [12, 12],
  })
}

const MarkerLayer = () => {
  const { origin, destination, waypoints } = useRouteStore()

  return (
    <>
      {origin && origin.lat !== 0 && origin.lng !== 0 && (
        <Marker position={[origin.lat, origin.lng]} icon={createCustomIcon('#10b981')}>
          <Popup>
            <div className="font-semibold">Origem</div>
            {origin.name && <div className="text-sm">{origin.name}</div>}
            {origin.address && <div className="text-xs text-gray-500">{origin.address}</div>}
          </Popup>
        </Marker>
      )}
      
      {waypoints.map((waypoint, index) => {
        // Only render waypoints that are not empty (not 0,0)
        if (waypoint.lat === 0 && waypoint.lng === 0) return null
        
        return (
          <Marker 
            key={`waypoint-${index}`} 
            position={[waypoint.lat, waypoint.lng]} 
            icon={createCustomIcon('#3b82f6')}
          >
            <Popup>
              <div className="font-semibold">Paragem {index + 1}</div>
              {waypoint.name && <div className="text-sm">{waypoint.name}</div>}
              {waypoint.address && <div className="text-xs text-gray-500">{waypoint.address}</div>}
            </Popup>
          </Marker>
        )
      })}
      
      {destination && destination.lat !== 0 && destination.lng !== 0 && (
        <Marker position={[destination.lat, destination.lng]} icon={createCustomIcon('#ef4444')}>
          <Popup>
            <div className="font-semibold">Destino</div>
            {destination.name && <div className="text-sm">{destination.name}</div>}
            {destination.address && <div className="text-xs text-gray-500">{destination.address}</div>}
          </Popup>
        </Marker>
      )}
    </>
  )
}

export default MarkerLayer


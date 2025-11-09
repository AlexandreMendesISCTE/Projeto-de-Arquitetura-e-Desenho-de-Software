import { MapContainer as LeafletMap, TileLayer, useMapEvents } from 'react-leaflet'
import { useMapStore } from '../../store/map.store'
import { useRouteStore } from '../../store/route.store'
import RouteLayer from './RouteLayer'
import MarkerLayer from './MarkerLayer'
import POILayer from './POILayer'
import { useRef } from 'react'
import 'leaflet/dist/leaflet.css'

function MapEvents() {
  const { setCenter, setZoom, setSelectedPoint, center, waitingForInput, clearWaitingForInput } = useMapStore()
  const { setOrigin, setDestination, origin, destination, waypoints, setWaypoints } = useRouteStore()
  const isUpdatingRef = useRef(false)

  useMapEvents({
    click: (e) => {
      const { lat, lng } = e.latlng
      const location = { lat, lng }

      // If waiting for specific input type, set that
      if (waitingForInput === 'origin') {
        setOrigin(location)
        setSelectedPoint(location)
        clearWaitingForInput()
        return
      }
      
      if (waitingForInput === 'destination') {
        setDestination(location)
        setSelectedPoint(location)
        clearWaitingForInput()
        return
      }
      
      if (waitingForInput === 'waypoint') {
        // Find which waypoint is empty (has 0,0 coordinates) or add new one
        const emptyIndex = waypoints.findIndex(wp => wp.lat === 0 && wp.lng === 0)
        if (emptyIndex !== -1) {
          const newWaypoints = [...waypoints]
          newWaypoints[emptyIndex] = location
          setWaypoints(newWaypoints)
          setSelectedPoint(location)
          clearWaitingForInput()
          return
        } else {
          // No empty waypoint found, add new one
          const { addWaypoint } = useRouteStore.getState()
          addWaypoint(location)
          setSelectedPoint(location)
          clearWaitingForInput()
          return
        }
      }

      // Default behavior: Toggle between origin and destination
      if (!origin) {
        setOrigin(location)
        setSelectedPoint(location)
      } else if (!destination) {
        setDestination(location)
        setSelectedPoint(location)
      } else {
        // Reset and set new origin
        setOrigin(location)
        setDestination(null)
        setSelectedPoint(location)
      }
    },
    moveend: (e) => {
      // Prevent infinite loop by checking if center actually changed
      if (isUpdatingRef.current) {
        return
      }
      
      const newCenter = e.target.getCenter()
      const [currentLat, currentLng] = center
      
      // Only update if center actually changed (more than 0.0001 degrees difference)
      if (Math.abs(newCenter.lat - currentLat) > 0.0001 || Math.abs(newCenter.lng - currentLng) > 0.0001) {
        isUpdatingRef.current = true
        setCenter([newCenter.lat, newCenter.lng])
        // Reset flag after a short delay
        setTimeout(() => {
          isUpdatingRef.current = false
        }, 100)
      }
    },
    zoomend: (e) => {
      setZoom(e.target.getZoom())
    },
  })

  return null
}

const MapContainer = () => {
  const { center, zoom } = useMapStore()

  return (
    <LeafletMap
      center={center}
      zoom={zoom}
      style={{ height: '100vh', width: '100%' }}
      zoomControl={true}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        maxZoom={19}
      />
      <MapEvents />
      <RouteLayer />
      <MarkerLayer />
      <POILayer />
    </LeafletMap>
  )
}

export default MapContainer


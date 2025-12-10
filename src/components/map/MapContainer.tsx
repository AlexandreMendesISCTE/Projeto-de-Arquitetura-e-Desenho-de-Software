// Leaflet map wrapper: bounds enforcement, click-to-set points, and overlays
import { MapContainer as LeafletMap, TileLayer, useMapEvents, useMap } from 'react-leaflet'
import { useMapStore } from '../../store/map.store'
import { useRouteStore } from '../../store/route.store'
import RouteLayer from './RouteLayer'
import MarkerLayer from './MarkerLayer'
import POILayer from './POILayer'
import AutoFitBounds from './AutoFitBounds'
import { useRef, useEffect } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Handles user interactions to update map/route state based on clicks and viewport changes
function MapEvents() {
  const { setCenter, setZoom, setSelectedPoint, center, waitingForInput, clearWaitingForInput } =
    useMapStore()
  const { setOrigin, setDestination, origin, destination, waypoints, setWaypoints } =
    useRouteStore()
  const isUpdatingRef = useRef(false)

  useMapEvents({
    click: (e) => {
      const { lat, lng } = e.latlng

      // Normalize longitude to -180 to 180 range
      let normalizedLng = lng
      while (normalizedLng > 180) normalizedLng -= 360
      while (normalizedLng < -180) normalizedLng += 360

      // Clamp latitude to valid range
      const normalizedLat = Math.max(-85, Math.min(85, lat))

      const location = { lat: normalizedLat, lng: normalizedLng }

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
        const emptyIndex = waypoints.findIndex((wp) => wp.lat === 0 && wp.lng === 0)
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
      if (
        Math.abs(newCenter.lat - currentLat) > 0.0001 ||
        Math.abs(newCenter.lng - currentLng) > 0.0001
      ) {
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

/**
 * Component to enforce map bounds and prevent dragging to repeated world copies
 */
function BoundsEnforcer() {
  const map = useMap()
  const { setCenter } = useMapStore()

  useEffect(() => {
    // Set max bounds to prevent dragging to repeated world copies
    const maxBounds = L.latLngBounds(L.latLng(-85, -180), L.latLng(85, 180))

    map.setMaxBounds(maxBounds)

    // Handle dragend to ensure center stays within bounds
    const handleDragEnd = () => {
      const center = map.getCenter()

      // Check if center is outside valid longitude range
      let newLng = center.lng
      if (newLng < -180) {
        newLng = -180
      } else if (newLng > 180) {
        newLng = 180
      }

      // Normalize longitude to -180 to 180 range
      if (newLng !== center.lng) {
        map.setView([center.lat, newLng], map.getZoom(), { animate: false })
        setCenter([center.lat, newLng])
      }

      // Ensure latitude is within valid range
      let newLat = center.lat
      if (newLat < -85) {
        newLat = -85
      } else if (newLat > 85) {
        newLat = 85
      }

      if (newLat !== center.lat) {
        map.setView([newLat, center.lng], map.getZoom(), { animate: false })
        setCenter([newLat, center.lng])
      }
    }

    // Handle moveend to check bounds
    const handleMoveEnd = () => {
      const center = map.getCenter()

      // Normalize longitude to -180 to 180 range
      let normalizedLng = center.lng
      while (normalizedLng > 180) normalizedLng -= 360
      while (normalizedLng < -180) normalizedLng += 360

      if (Math.abs(normalizedLng - center.lng) > 0.01) {
        map.setView([center.lat, normalizedLng], map.getZoom(), { animate: false })
        setCenter([center.lat, normalizedLng])
      }
    }

    map.on('dragend', handleDragEnd)
    map.on('moveend', handleMoveEnd)

    return () => {
      map.off('dragend', handleDragEnd)
      map.off('moveend', handleMoveEnd)
    }
  }, [map, setCenter])

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
      minZoom={3}
      maxZoom={19}
      worldCopyJump={false}
      maxBounds={[
        [-85, -180],
        [85, 180],
      ]}
      maxBoundsViscosity={1.0}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        maxZoom={19}
        minZoom={3}
      />
      <BoundsEnforcer />
      <MapEvents />
      <AutoFitBounds />
      <RouteLayer />
      <MarkerLayer />
      <POILayer />
    </LeafletMap>
  )
}

export default MapContainer

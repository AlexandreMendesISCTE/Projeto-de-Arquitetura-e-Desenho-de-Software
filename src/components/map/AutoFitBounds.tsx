// Automatically fit map bounds to show origin, destination, and waypoints when route is set
import { useEffect, useRef } from 'react'
import { useMap } from 'react-leaflet'
import { useRouteStore } from '../../store/route.store'
import L from 'leaflet'

const AutoFitBounds = () => {
  const map = useMap()
  const { origin, destination, waypoints, route } = useRouteStore()
  const previousRouteRef = useRef<typeof route>(null)

  useEffect(() => {
    // Only auto-fit when we have a route and both origin and destination
    if (!route || !origin || !destination) {
      previousRouteRef.current = route
      return
    }

    // Only auto-fit if route actually changed (not just a re-render)
    // Check if route distance changed significantly (more than 100m difference)
    const routeChanged =
      !previousRouteRef.current ||
      Math.abs((previousRouteRef.current.totalDistance || 0) - route.totalDistance) > 100 ||
      previousRouteRef.current.transportMode !== route.transportMode

    if (!routeChanged) {
      return
    }

    previousRouteRef.current = route

    // Collect all points to include in bounds
    const points: L.LatLng[] = []

    // Add origin
    points.push(L.latLng(origin.lat, origin.lng))

    // Add destination
    points.push(L.latLng(destination.lat, destination.lng))

    // Add valid waypoints (filter out empty ones with 0,0 coordinates)
    waypoints.forEach((waypoint) => {
      if (waypoint.lat !== 0 || waypoint.lng !== 0) {
        points.push(L.latLng(waypoint.lat, waypoint.lng))
      }
    })

    // Only fit bounds if we have at least 2 points
    if (points.length >= 2) {
      // Create bounds from all points
      const bounds = L.latLngBounds(points)

      // Fit map to bounds with padding (in pixels)
      // This ensures markers aren't right at the edge
      map.fitBounds(bounds, {
        padding: [50, 50], // 50px padding on all sides
        maxZoom: 15, // Don't zoom in too much if points are close
        animate: true, // Smooth animation
        duration: 0.5, // Animation duration in seconds
      })
    }
  }, [map, route, origin, destination, waypoints])

  return null
}

export default AutoFitBounds

import { Location, TransportMode } from '../types/route.types'

/**
 * Opens route in Google Maps app
 * Uses Google Maps URLs that work on both web and mobile
 */
export const openRouteInGoogleMaps = (
  origin: Location,
  destination: Location,
  mode: TransportMode,
  waypoints: Location[] = []
): void => {
  // Convert transport mode to Google Maps travel mode
  const googleMode =
    mode === TransportMode.DRIVING
      ? 'driving'
      : mode === TransportMode.BICYCLING
        ? 'bicycling'
        : mode === TransportMode.TRANSIT
          ? 'transit'
          : 'walking'

  // Format coordinates
  const originStr = `${origin.lat},${origin.lng}`
  const destStr = `${destination.lat},${destination.lng}`

  // Build waypoints parameter if present
  let waypointsParam = ''
  if (waypoints.length > 0) {
    const waypointsStr = waypoints.map((wp) => `${wp.lat},${wp.lng}`).join('|')
    waypointsParam = `&waypoints=${encodeURIComponent(waypointsStr)}`
  }

  // Google Maps URL format
  // https://www.google.com/maps/dir/?api=1&origin=lat,lng&destination=lat,lng&travelmode=mode&waypoints=lat1,lng1|lat2,lng2
  const url = `https://www.google.com/maps/dir/?api=1&origin=${originStr}&destination=${destStr}&travelmode=${googleMode}${waypointsParam}`

  // Open in new tab/window
  window.open(url, '_blank')
}

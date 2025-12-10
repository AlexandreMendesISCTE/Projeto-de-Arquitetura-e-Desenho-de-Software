import { Route, Location } from '../types/route.types'

/**
 * Exports route to GPX format
 */
export const exportRouteToGPX = (route: Route, origin: Location, destination: Location): void => {
  const waypoints = route.waypoints.length > 0 ? route.waypoints : [origin, destination]

  let gpx = `<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="Map Route Explorer">
  <metadata>
    <name>Route ${route.transportMode}</name>
    <desc>Distance: ${(route.totalDistance / 1000).toFixed(2)} km, Duration: ${Math.round(route.totalDuration / 60)} min</desc>
  </metadata>
  <rte>
    <name>Route</name>
    <rtept lat="${origin.lat}" lon="${origin.lng}">
      <name>Origin</name>
    </rtept>`

  waypoints.slice(1, -1).forEach((point, index) => {
    gpx += `
    <rtept lat="${point.lat}" lon="${point.lng}">
      <name>Waypoint ${index + 1}</name>
    </rtept>`
  })

  gpx += `
    <rtept lat="${destination.lat}" lon="${destination.lng}">
      <name>Destination</name>
    </rtept>
  </rte>
  <trk>
    <name>Route Track</name>
    <trkseg>`

  route.geometry.coordinates.forEach(([lng, lat]) => {
    gpx += `
      <trkpt lat="${lat}" lon="${lng}"></trkpt>`
  })

  gpx += `
    </trkseg>
  </trk>
</gpx>`

  const blob = new Blob([gpx], { type: 'application/gpx+xml' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `route-${route.transportMode}-${Date.now()}.gpx`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * Exports route to JSON format
 */
export const exportRouteToJSON = (route: Route, origin: Location, destination: Location): void => {
  const exportData = {
    origin,
    destination,
    route: {
      waypoints: route.waypoints,
      totalDistance: route.totalDistance,
      totalDuration: route.totalDuration,
      transportMode: route.transportMode,
      geometry: route.geometry,
      instructions: route.instructions,
    },
    exportedAt: new Date().toISOString(),
  }

  const json = JSON.stringify(exportData, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `route-${route.transportMode}-${Date.now()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

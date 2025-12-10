import { Location, Route, TransportMode } from '../../types/route.types'

const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || ''

// Declare Google Maps types
declare global {
  interface Window {
    google: typeof google
    googleMapsLoaded: boolean
    googleMapsReadyCallback?: () => void
    initGoogleMaps: () => void
  }
}

// Decode Google Maps polyline to coordinates
function decodePolyline(encoded: string): [number, number][] {
  if (!encoded || typeof encoded !== 'string' || encoded.length === 0) {
    return []
  }

  const poly: [number, number][] = []
  let index = 0
  const len = encoded.length
  let lat = 0
  let lng = 0

  while (index < len) {
    let b: number
    let shift = 0
    let result = 0
    do {
      b = encoded.charCodeAt(index++) - 63
      result |= (b & 0x1f) << shift
      shift += 5
    } while (b >= 0x20)
    const dlat = (result & 1) !== 0 ? ~(result >> 1) : result >> 1
    lat += dlat

    shift = 0
    result = 0
    do {
      b = encoded.charCodeAt(index++) - 63
      result |= (b & 0x1f) << shift
      shift += 5
    } while (b >= 0x20)
    const dlng = (result & 1) !== 0 ? ~(result >> 1) : result >> 1
    lng += dlng

    poly.push([lng / 1e5, lat / 1e5])
  }

  return poly
}

// Load Google Maps JavaScript API
function loadGoogleMapsAPI(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (window.google && window.google.maps) {
      resolve()
      return
    }

    if (window.googleMapsLoaded) {
      resolve()
      return
    }

    if (!GOOGLE_MAPS_API_KEY) {
      reject(new Error('Google Maps API key is not configured'))
      return
    }

    window.googleMapsReadyCallback = () => {
      resolve()
    }

    // Prevent double-loading the Google Maps script (avoids custom element redefinition warnings)
    const existingScript = document.querySelector<HTMLScriptElement>(
      'script[data-google-maps-loader]'
    )
    if (existingScript) {
      return
    }

    const script = document.createElement('script')
    script.src = `https://maps.googleapis.com/maps/api/js?key=${GOOGLE_MAPS_API_KEY}&libraries=places&loading=async&callback=initGoogleMaps`
    script.async = true
    script.defer = true
    script.setAttribute('data-google-maps-loader', 'true')
    script.onerror = () => {
      reject(new Error('Failed to load Google Maps API'))
    }
    document.head.appendChild(script)
  })
}

class GoogleMapsService {
  private directionsService: google.maps.DirectionsService | null = null
  private initialized = false

  private async ensureInitialized(): Promise<void> {
    if (this.initialized && this.directionsService) {
      return
    }

    await loadGoogleMapsAPI()
    this.directionsService = new google.maps.DirectionsService()
    this.initialized = true
  }

  private getTravelMode(mode: TransportMode): google.maps.TravelMode {
    const modes = {
      [TransportMode.DRIVING]: google.maps.TravelMode.DRIVING,
      [TransportMode.BICYCLING]: google.maps.TravelMode.BICYCLING,
      [TransportMode.WALKING]: google.maps.TravelMode.WALKING,
      [TransportMode.TRANSIT]: google.maps.TravelMode.TRANSIT,
    }
    return modes[mode]
  }

  async calculateRoute(
    origin: Location,
    destination: Location,
    mode: TransportMode = TransportMode.DRIVING,
    waypoints: Location[] = []
  ): Promise<Route> {
    if (!GOOGLE_MAPS_API_KEY) {
      throw new Error(
        'Google Maps API key is not configured. Please set VITE_GOOGLE_MAPS_API_KEY in your .env file'
      )
    }

    await this.ensureInitialized()

    if (!this.directionsService) {
      throw new Error('DirectionsService not initialized')
    }

    const request: google.maps.DirectionsRequest = {
      origin: new google.maps.LatLng(origin.lat, origin.lng),
      destination: new google.maps.LatLng(destination.lat, destination.lng),
      travelMode: this.getTravelMode(mode),
      unitSystem: google.maps.UnitSystem.METRIC,
      language: 'pt',
      ...(waypoints.length > 0 && {
        waypoints: waypoints.map((wp) => ({
          location: new google.maps.LatLng(wp.lat, wp.lng),
          stopover: true,
        })),
        optimizeWaypoints: false, // Keep waypoints in order
      }),
      ...(mode === TransportMode.DRIVING && {
        drivingOptions: {
          departureTime: new Date(),
          trafficModel: google.maps.TrafficModel.BEST_GUESS,
        },
      }),
    }

    return new Promise((resolve, reject) => {
      this.directionsService!.route(request, (result, status) => {
        if (status !== google.maps.DirectionsStatus.OK || !result || result.routes.length === 0) {
          reject(new Error(`Google Maps API error: ${status}`))
          return
        }

        const routeData = result.routes[0]

        // Calculate total distance and duration across all legs
        let totalDistance = 0
        let totalDuration = 0
        const allWaypoints: Location[] = [origin]
        const allInstructions: string[] = []

        routeData.legs.forEach((leg) => {
          if (!leg.distance || !leg.duration) {
            return
          }

          totalDistance += leg.distance.value
          // Use duration_in_traffic for driving if available, otherwise use duration
          const legDuration =
            mode === TransportMode.DRIVING && leg.duration_in_traffic
              ? leg.duration_in_traffic.value
              : leg.duration.value
          totalDuration += legDuration

          // Add waypoints from steps
          leg.steps.forEach((step) => {
            allWaypoints.push({
              lat: step.end_location.lat(),
              lng: step.end_location.lng(),
            })
            // Extract instructions
            const text = step.instructions.replace(/<[^>]*>/g, '')
            allInstructions.push(text)
          })
        })

        allWaypoints.push(destination)

        if (totalDistance === 0 || totalDuration === 0) {
          reject(new Error('Invalid route data'))
          return
        }

        // Decode polyline to get coordinates
        // Handle different polyline formats from Google Maps
        let encodedPolyline = ''

        if (routeData.overview_polyline) {
          const polyline =
            typeof routeData.overview_polyline === 'string'
              ? routeData.overview_polyline
              : (routeData.overview_polyline as google.maps.DirectionsPolyline | undefined)
          // Polyline can be an object with .points property or a string
          if (typeof polyline === 'string') {
            encodedPolyline = polyline
          } else if (polyline && typeof polyline.points === 'string') {
            encodedPolyline = polyline.points
          }
        }

        // Fallback: use step polylines if overview_polyline is not available
        if (
          !encodedPolyline &&
          routeData.legs.length > 0 &&
          routeData.legs[0].steps &&
          routeData.legs[0].steps.length > 0
        ) {
          // Use first step's polyline as fallback
          const firstStepPolyline = routeData.legs[0].steps[0].polyline as
            | google.maps.DirectionsPolyline
            | undefined
          if (firstStepPolyline && typeof firstStepPolyline.points === 'string') {
            encodedPolyline = firstStepPolyline.points
          }
        }

        if (!encodedPolyline) {
          console.error('No polyline data found in route', routeData)
          reject(new Error('No polyline data in route'))
          return
        }

        const coordinates = decodePolyline(encodedPolyline)

        if (coordinates.length === 0) {
          console.error('Failed to decode polyline', encodedPolyline)
          reject(new Error('Failed to decode route polyline'))
          return
        }

        resolve({
          waypoints: allWaypoints,
          totalDistance, // meters
          totalDuration, // seconds (from Google Maps - accurate!)
          transportMode: mode,
          geometry: {
            type: 'LineString',
            coordinates: coordinates.map(([lng, lat]) => [lng, lat]),
          },
          instructions: allInstructions,
        })
      })
    })
  }
}

export default new GoogleMapsService()

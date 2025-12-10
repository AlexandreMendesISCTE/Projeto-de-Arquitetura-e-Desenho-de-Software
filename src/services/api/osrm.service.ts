// OSRM fallback client for routing when Google Maps is unavailable
import axios, { AxiosInstance } from 'axios'
import { OSRM_BASE_URL, HTTP_TIMEOUT } from '../../constants/api.constants'
import { Location, Route, TransportMode } from '../../types/route.types'
import { adjustDuration } from '../../utils/route.utils'

interface OSRMResponse {
  code: string
  routes: Array<{
    distance: number
    duration: number
    geometry: {
      coordinates: [number, number][]
    }
    legs: Array<{
      steps: Array<{
        maneuver: {
          instruction: string
        }
      }>
    }>
  }>
  waypoints: Array<{
    location: [number, number]
  }>
}

class OSRMService {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: OSRM_BASE_URL,
      timeout: HTTP_TIMEOUT,
      headers: {
        'User-Agent': 'MapRouteExplorer/3.0.0',
      },
    })
  }

  private getProfile(mode: TransportMode): string {
    const profiles: Record<TransportMode, string> = {
      [TransportMode.DRIVING]: 'driving',
      [TransportMode.BICYCLING]: 'cycling',
      [TransportMode.WALKING]: 'walking',
      // OSRM não suporta transit; usar driving como fallback razoável para evitar quebra
      [TransportMode.TRANSIT]: 'driving',
    }
    return profiles[mode]
  }

  async calculateRoute(
    origin: Location,
    destination: Location,
    mode: TransportMode = TransportMode.DRIVING
  ): Promise<Route> {
    const profile = this.getProfile(mode)
    const coordinates = `${origin.lng},${origin.lat};${destination.lng},${destination.lat}`
    const url = `/${profile}/${coordinates}`

    try {
      const response = await this.client.get<OSRMResponse>(url, {
        params: {
          overview: 'full',
          geometries: 'geojson',
          steps: true,
        },
      })

      return this.parseRouteResponse(response.data, mode, origin, destination)
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(`OSRM API error: ${error.message}`)
      }
      throw error
    }
  }

  private parseRouteResponse(
    data: OSRMResponse,
    mode: TransportMode,
    origin: Location,
    destination: Location
  ): Route {
    if (data.code !== 'Ok' || !data.routes || data.routes.length === 0) {
      throw new Error('No route found')
    }

    const routeData = data.routes[0]
    const coordinates = routeData.geometry.coordinates.map(([lng, lat]) => ({
      lat,
      lng,
    }))

    const instructions: string[] = []
    routeData.legs.forEach((leg) => {
      leg.steps.forEach((step) => {
        if (step.maneuver?.instruction) {
          instructions.push(step.maneuver.instruction)
        }
      })
    })

    // Adjust duration if API returns unrealistic values
    const adjustedDuration = adjustDuration(routeData.duration, routeData.distance, mode)

    return {
      waypoints: [origin, ...coordinates.slice(1, -1), destination],
      totalDistance: routeData.distance,
      totalDuration: adjustedDuration,
      transportMode: mode,
      geometry: {
        type: 'LineString',
        coordinates: routeData.geometry.coordinates,
      },
      instructions,
    }
  }
}

export default new OSRMService()

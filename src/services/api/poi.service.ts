import axios, { AxiosInstance } from 'axios'
import { HTTP_TIMEOUT } from '../../constants/api.constants'
import { Location } from '../../types/route.types'

const OVERPASS_API_URL = 'https://overpass-api.de/api/interpreter'

export interface POI {
  id: string
  name: string
  type: string
  lat: number
  lng: number
  category?: string
}

interface OverpassResponse {
  elements: Array<{
    type: string
    id: number
    lat?: number
    lon?: number
    tags?: {
      name?: string
      amenity?: string
      shop?: string
      tourism?: string
      [key: string]: string | undefined
    }
  }>
}

class POIService {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: OVERPASS_API_URL,
      timeout: HTTP_TIMEOUT,
      // Add retry configuration
      validateStatus: (status) => status < 500, // Don't throw on 4xx errors
    })
  }

  /**
   * Get POIs along a route
   */
  async getPOIsAlongRoute(routeCoordinates: [number, number][]): Promise<POI[]> {
    if (routeCoordinates.length < 2) {
      return []
    }

    // Create a bounding box around the route
    const lats = routeCoordinates.map((coord) => coord[1]) // lat is second element
    const lngs = routeCoordinates.map((coord) => coord[0]) // lng is first element
    const minLat = Math.min(...lats) - 0.01
    const maxLat = Math.max(...lats) + 0.01
    const minLng = Math.min(...lngs) - 0.01
    const maxLng = Math.max(...lngs) + 0.01

    // Build Overpass QL query - fixed syntax
    // Format: (south,west,north,east) for bounding box
    // Use proper regex syntax with ~ operator
    const query = `[out:json][timeout:25];
(
  node["amenity"~"^(restaurant|cafe|fuel|parking|fast_food)$"](${minLat},${minLng},${maxLat},${maxLng});
  node["shop"~"^(supermarket|convenience|bakery)$"](${minLat},${minLng},${maxLat},${maxLng});
  node["tourism"~"^(attraction|museum|hotel)$"](${minLat},${minLng},${maxLat},${maxLng});
  way["amenity"~"^(restaurant|cafe|fuel|parking|fast_food)$"](${minLat},${minLng},${maxLat},${maxLng});
  way["shop"~"^(supermarket|convenience|bakery)$"](${minLat},${minLng},${maxLat},${maxLng});
  way["tourism"~"^(attraction|museum|hotel)$"](${minLat},${minLng},${maxLat},${maxLng});
);
out center;`

    try {
      const response = await this.client.post<OverpassResponse>('', query, {
        headers: {
          'Content-Type': 'text/plain',
        },
      })

      return this.parsePOIResponse(response.data)
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error('Overpass API error:', error.response?.data || error.message)
        return []
      }
      return []
    }
  }

  private parsePOIResponse(data: OverpassResponse): POI[] {
    const pois: POI[] = []

    data.elements.forEach((element) => {
      if (!element.tags || !element.tags.name) {
        return
      }

      let lat: number
      let lng: number

      if (element.type === 'node') {
        lat = element.lat!
        lng = element.lon!
      } else if (element.type === 'way' && element.lat && element.lon) {
        // For ways, use center coordinates
        lat = element.lat
        lng = element.lon
      } else {
        return
      }

      const category = element.tags.amenity || element.tags.shop || element.tags.tourism || 'other'

      pois.push({
        id: `poi-${element.id}`,
        name: element.tags.name,
        type: element.type,
        lat,
        lng,
        category,
      })
    })

    return pois
  }

  /**
   * Get POIs near a specific location
   */
  async getPOIsNearLocation(
    location: Location,
    radius: number = 500 // meters
  ): Promise<POI[]> {
    // Convert radius from meters to degrees (approximate)
    const radiusDegrees = radius / 111000 // ~111km per degree

    const minLat = location.lat - radiusDegrees
    const maxLat = location.lat + radiusDegrees
    const minLng = location.lng - radiusDegrees
    const maxLng = location.lng + radiusDegrees

    return this.getPOIsInBounds(minLat, minLng, maxLat, maxLng)
  }

  /**
   * Get POIs within a bounding box
   * Limits bounding box size to prevent timeout errors
   */
  async getPOIsInBounds(
    minLat: number,
    minLng: number,
    maxLat: number,
    maxLng: number
  ): Promise<POI[]> {
    // Limit bounding box size to prevent timeout (max ~0.1 degrees = ~11km)
    const MAX_BBOX_SIZE = 0.1
    const latDiff = maxLat - minLat
    const lngDiff = maxLng - minLng

    // If bounding box is too large, shrink it around the center
    if (latDiff > MAX_BBOX_SIZE || lngDiff > MAX_BBOX_SIZE) {
      const centerLat = (minLat + maxLat) / 2
      const centerLng = (minLng + maxLng) / 2
      const halfLat = Math.min(latDiff / 2, MAX_BBOX_SIZE / 2)
      const halfLng = Math.min(lngDiff / 2, MAX_BBOX_SIZE / 2)

      minLat = centerLat - halfLat
      maxLat = centerLat + halfLat
      minLng = centerLng - halfLng
      maxLng = centerLng + halfLng
    }

    // Use shorter timeout - limit results in JavaScript instead of query
    const query = `[out:json][timeout:10];
(
  node["amenity"~"^(restaurant|cafe|fuel|parking|fast_food)$"](${minLat},${minLng},${maxLat},${maxLng});
  node["shop"~"^(supermarket|convenience|bakery)$"](${minLat},${minLng},${maxLat},${maxLng});
  node["tourism"~"^(attraction|museum|hotel)$"](${minLat},${minLng},${maxLat},${maxLng});
  way["amenity"~"^(restaurant|cafe|fuel|parking|fast_food)$"](${minLat},${minLng},${maxLat},${maxLng});
  way["shop"~"^(supermarket|convenience|bakery)$"](${minLat},${minLng},${maxLat},${maxLng});
  way["tourism"~"^(attraction|museum|hotel)$"](${minLat},${minLng},${maxLat},${maxLng});
);
out center;`

    try {
      const response = await this.client.post<OverpassResponse>('', query, {
        headers: {
          'Content-Type': 'text/plain',
        },
        timeout: 15000, // 15 second HTTP timeout (shorter than query timeout)
      })

      const pois = this.parsePOIResponse(response.data)

      // Limit results to 100 to prevent overload (limit in JavaScript, not query)
      return pois.slice(0, 100)
    } catch (error) {
      // Only log if it's not a timeout or bad request (these are expected and handled gracefully)
      if (axios.isAxiosError(error)) {
        const isTimeout =
          error.code === 'ECONNABORTED' ||
          error.response?.status === 504 ||
          (typeof error.response?.data === 'string' && error.response.data.includes('timeout'))

        const isBadRequest = error.response?.status === 400

        if (!isTimeout && !isBadRequest) {
          console.error('Overpass API error:', error.response?.data || error.message)
        }
        // Silently return empty array for timeouts/bad requests - we'll use cached data
        return []
      }
      return []
    }
  }
}

export default new POIService()

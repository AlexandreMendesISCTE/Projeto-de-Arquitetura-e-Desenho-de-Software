import axios, { AxiosInstance } from 'axios'
import { NOMINATIM_BASE_URL, HTTP_TIMEOUT } from '../../constants/api.constants'
import { Location } from '../../types/route.types'

interface NominatimResult {
  lat: string
  lon: string
  display_name: string
  address?: {
    road?: string
    city?: string
    country?: string
  }
}

class NominatimService {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: NOMINATIM_BASE_URL,
      timeout: HTTP_TIMEOUT,
    })
  }

  async search(query: string, limit: number = 10): Promise<Location[]> {
    try {
      const response = await this.client.get<NominatimResult[]>('/search', {
        params: {
          q: query,
          format: 'json',
          limit,
          addressdetails: 1,
        },
      })

      return response.data.map(this.parseLocation)
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(`Nominatim API error: ${error.message}`)
      }
      throw error
    }
  }

  async reverseGeocode(lat: number, lng: number): Promise<Location> {
    try {
      const response = await this.client.get<NominatimResult>('/reverse', {
        params: {
          lat,
          lon: lng,
          format: 'json',
          addressdetails: 1,
        },
      })

      return this.parseLocation(response.data)
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(`Nominatim reverse geocoding error: ${error.message}`)
      }
      throw error
    }
  }

  private parseLocation(result: NominatimResult): Location {
    const address = result.address
    const addressParts: string[] = []

    if (address?.road) addressParts.push(address.road)
    if (address?.city) addressParts.push(address.city)
    if (address?.country) addressParts.push(address.country)

    return {
      lat: parseFloat(result.lat),
      lng: parseFloat(result.lon),
      name: result.display_name,
      address: addressParts.length > 0 ? addressParts.join(', ') : undefined,
    }
  }
}

export default new NominatimService()

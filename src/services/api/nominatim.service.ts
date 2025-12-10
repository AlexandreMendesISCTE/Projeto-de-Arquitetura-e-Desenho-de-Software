// Geocoding service - Primary: Google Places API, Fallback: Nominatim
// Uses Google Places for fast, reliable results, falls back to Nominatim if Google fails
import axios, { AxiosInstance } from 'axios'
import { NOMINATIM_BASE_URL, HTTP_TIMEOUT } from '../../constants/api.constants'
import { Location } from '../../types/route.types'
import googlePlacesService from './google-places.service'

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
    // Try Google Places API first (faster and more reliable)
    try {
      const googleResults = await googlePlacesService.search(query, limit)
      return googleResults
    } catch (error) {
      // If Google Places fails, fallback to Nominatim
      console.warn('Google Places API failed, falling back to Nominatim:', error)
      try {
        const response = await this.client.get<NominatimResult[]>('/search', {
          params: {
            q: query,
            format: 'json',
            limit,
            addressdetails: 1,
          },
          validateStatus: (status) => {
            // Don't throw on 418 (rate limit) or other client errors
            return status < 500
          },
        })

        // Check if we got a valid response
        if (response.status === 418 || response.status >= 400) {
          throw new Error(`Nominatim returned ${response.status}`)
        }

        if (!response.data || response.data.length === 0) {
          throw new Error('Nominatim returned empty results')
        }

        const nominatimResults = response.data.map(this.parseLocation)
        console.log(`✅ Nominatim fallback successful: ${nominatimResults.length} results`)
        return nominatimResults
      } catch (fallbackError) {
        // If both fail, throw error
        if (axios.isAxiosError(fallbackError)) {
          throw new Error(
            `Geocoding failed (Google Places: ${error}, Nominatim: ${fallbackError.message})`
          )
        }
        throw new Error(`Geocoding failed (Google Places: ${error}, Nominatim: ${fallbackError})`)
      }
    }
  }

  async reverseGeocode(lat: number, lng: number): Promise<Location> {
    // Try Google Places API first (faster and more reliable)
    try {
      return await googlePlacesService.reverseGeocode(lat, lng)
    } catch (error) {
      // If Google Places fails, fallback to Nominatim
      console.warn('Google Places reverse geocoding failed, falling back to Nominatim:', error)
      try {
        const response = await this.client.get<NominatimResult>('/reverse', {
          params: {
            lat,
            lon: lng,
            format: 'json',
            addressdetails: 1,
          },
          validateStatus: (status) => {
            // Don't throw on 418 or other client errors
            return status < 500
          },
        })

        // Check if we got a valid response
        if (response.status === 418 || response.status >= 400) {
          throw new Error(`Nominatim returned ${response.status}`)
        }

        if (!response.data) {
          throw new Error('Nominatim returned empty result')
        }

        const result = this.parseLocation(response.data)
        console.log(`✅ Nominatim reverse geocoding fallback successful`)
        return result
      } catch (fallbackError) {
        // If both fail, throw error
        if (axios.isAxiosError(fallbackError)) {
          throw new Error(
            `Reverse geocoding failed (Google Places: ${error}, Nominatim: ${fallbackError.message})`
          )
        }
        throw new Error(
          `Reverse geocoding failed (Google Places: ${error}, Nominatim: ${fallbackError})`
        )
      }
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

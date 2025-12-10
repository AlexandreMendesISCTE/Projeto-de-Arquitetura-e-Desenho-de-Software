// Google Places API geocoding service (fallback for Nominatim)
import { Location } from '../../types/route.types'
import { GOOGLE_MAPS_API_KEY } from '../../constants/api.constants'
import { loadGoogleMapsAPI } from './google-maps.service'

class GooglePlacesService {
  private placesService: google.maps.places.PlacesService | null = null
  private geocoder: google.maps.Geocoder | null = null
  private autocompleteService: google.maps.places.AutocompleteService | null = null

  private async ensureInitialized(): Promise<void> {
    await loadGoogleMapsAPI()

    if (!window.google?.maps) {
      throw new Error('Google Maps API not loaded')
    }

    if (!this.autocompleteService && window.google.maps.places) {
      this.autocompleteService = new google.maps.places.AutocompleteService()
    }

    if (!this.placesService && window.google.maps.places) {
      // Create a dummy map element for PlacesService (it requires a map instance)
      const mapDiv = document.createElement('div')
      const map = new google.maps.Map(mapDiv)
      this.placesService = new google.maps.places.PlacesService(map)
    }

    if (!this.geocoder) {
      this.geocoder = new google.maps.Geocoder()
    }
  }

  async search(query: string, limit: number = 10): Promise<Location[]> {
    if (!GOOGLE_MAPS_API_KEY) {
      throw new Error('Google Maps API key is not configured. Cannot use fallback geocoding.')
    }

    try {
      await this.ensureInitialized()
    } catch (error) {
      throw new Error(`Failed to initialize Google Places API: ${error}`)
    }

    if (!this.placesService) {
      throw new Error('PlacesService not initialized')
    }

    return new Promise((resolve, reject) => {
      if (!this.autocompleteService) {
        reject(new Error('AutocompleteService not initialized'))
        return
      }

      const request: google.maps.places.AutocompleteRequest = {
        input: query,
      }

      this.autocompleteService.getPlacePredictions(request, (predictions, status) => {
        if (status !== google.maps.places.PlacesServiceStatus.OK || !predictions) {
          reject(new Error(`Google Places API error: ${status}`))
          return
        }

        // Limit results
        const limitedPredictions = predictions.slice(0, limit)

        // Get place details for each prediction
        const placeDetailsPromises = limitedPredictions.map((prediction) => {
          return new Promise<Location>((resolveDetail, rejectDetail) => {
            if (!this.placesService) {
              rejectDetail(new Error('PlacesService not available'))
              return
            }

            const detailsRequest: google.maps.places.PlaceDetailsRequest = {
              placeId: prediction.place_id,
              fields: ['formatted_address', 'geometry', 'name', 'types'],
            }

            this.placesService.getDetails(detailsRequest, (place, placeStatus) => {
              if (
                placeStatus !== google.maps.places.PlacesServiceStatus.OK ||
                !place ||
                !place.geometry ||
                !place.geometry.location
              ) {
                rejectDetail(new Error(`Place details error: ${placeStatus}`))
                return
              }

              const location: Location = {
                lat: place.geometry.location.lat(),
                lng: place.geometry.location.lng(),
                name: place.formatted_address || place.name || prediction.description,
                address: place.formatted_address,
              }

              resolveDetail(location)
            })
          })
        })

        Promise.all(placeDetailsPromises)
          .then((locations) => resolve(locations))
          .catch((error) => reject(error))
      })
    })
  }

  async reverseGeocode(lat: number, lng: number): Promise<Location> {
    if (!GOOGLE_MAPS_API_KEY) {
      throw new Error('Google Maps API key is not configured. Cannot use fallback geocoding.')
    }

    try {
      await this.ensureInitialized()
    } catch (error) {
      throw new Error(`Failed to initialize Google Places API: ${error}`)
    }

    if (!this.geocoder) {
      throw new Error('Geocoder not initialized')
    }

    return new Promise((resolve, reject) => {
      const request: google.maps.GeocoderRequest = {
        location: { lat, lng },
      }

      if (!this.geocoder) {
        reject(new Error('Geocoder not initialized'))
        return
      }

      this.geocoder.geocode(request, (results, status) => {
        if (status !== google.maps.GeocoderStatus.OK || !results || results.length === 0) {
          reject(new Error(`Google Geocoder error: ${status}`))
          return
        }

        const result = results[0]
        if (!result.geometry || !result.geometry.location) {
          reject(new Error('Invalid geocoding result'))
          return
        }

        const location: Location = {
          lat: result.geometry.location.lat(),
          lng: result.geometry.location.lng(),
          name: result.formatted_address,
          address: result.formatted_address,
        }

        resolve(location)
      })
    })
  }
}

export default new GooglePlacesService()

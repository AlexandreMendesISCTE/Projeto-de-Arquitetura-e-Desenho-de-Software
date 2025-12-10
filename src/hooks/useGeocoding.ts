// React-query wrappers around Nominatim search/reverse endpoints
import { useQuery } from '@tanstack/react-query'
import nominatimService from '../services/api/nominatim.service'

export const useGeocoding = (query: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['geocoding', query],
    queryFn: () => nominatimService.search(query),
    enabled: enabled && query.length > 2,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

export const useReverseGeocoding = (lat: number | null, lng: number | null) => {
  return useQuery({
    queryKey: ['reverseGeocoding', lat, lng],
    queryFn: () => {
      if (lat === null || lng === null) {
        throw new Error('Latitude and longitude are required')
      }
      return nominatimService.reverseGeocode(lat, lng)
    },
    enabled: lat !== null && lng !== null,
    staleTime: 10 * 60 * 1000,
  })
}

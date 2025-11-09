import { useQuery } from '@tanstack/react-query'
import poiService from '../services/api/poi.service'
import { Route } from '../types/route.types'
import { usePOIStore } from '../store/poi.store'

export const usePOIs = (route: Route | null) => {
  const { enabled } = usePOIStore()
  
  return useQuery({
    queryKey: ['pois', route?.geometry, enabled],
    queryFn: () => {
      if (!enabled || !route || !route.geometry || route.geometry.coordinates.length === 0) {
        return []
      }
      // Convert GeoJSON coordinates to [lng, lat][] format
      const coordinates = route.geometry.coordinates as [number, number][]
      return poiService.getPOIsAlongRoute(coordinates)
    },
    enabled: enabled && !!route && !!route.geometry,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}


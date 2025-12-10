import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useMemo } from 'react'
import googleMapsService from '../services/api/google-maps.service'
import { Location, TransportMode } from '../types/route.types'

export const useRoute = (
  origin: Location | null,
  destination: Location | null,
  mode: TransportMode,
  waypoints: Location[] = []
) => {
  const queryClient = useQueryClient()
  // Filter out empty waypoints (0,0 coordinates)
  const validWaypoints = waypoints.filter((wp) => wp.lat !== 0 || wp.lng !== 0)
  const queryKey = useMemo(
    () => [
      'route',
      origin?.lat,
      origin?.lng,
      destination?.lat,
      destination?.lng,
      mode,
      validWaypoints.length,
    ],
    [destination?.lat, destination?.lng, mode, origin?.lat, origin?.lng, validWaypoints.length]
  )

  const query = useQuery({
    queryKey,
    queryFn: () => {
      if (!origin || !destination) {
        throw new Error('Origin and destination are required')
      }
      return googleMapsService.calculateRoute(origin, destination, mode, validWaypoints)
    },
    enabled: !!origin && !!destination,
    staleTime: 0,
    refetchOnWindowFocus: false,
  })

  // Force refetch when mode or waypoints change
  useEffect(() => {
    if (origin && destination) {
      queryClient.removeQueries({
        queryKey: ['route'],
        exact: false,
      })
      queryClient.refetchQueries({ queryKey })
    }
  }, [destination, mode, origin, queryClient, queryKey, validWaypoints.length]) // Depend on mode, waypoints, and route context

  return query
}

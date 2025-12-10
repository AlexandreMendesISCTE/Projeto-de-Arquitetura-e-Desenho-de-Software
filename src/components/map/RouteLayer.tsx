import { Polyline } from 'react-leaflet'
import { useRouteStore } from '../../store/route.store'
import { TransportMode } from '../../types/route.types'

const getRouteColor = (mode: TransportMode): string => {
  switch (mode) {
    case TransportMode.DRIVING:
      return '#3b82f6' // blue
    case TransportMode.BICYCLING:
      return '#10b981' // green
    case TransportMode.WALKING:
      return '#f59e0b' // orange
    default:
      return '#3b82f6'
  }
}

const RouteLayer = () => {
  const { route } = useRouteStore()

  if (!route || !route.geometry) {
    return null
  }

  const coordinates = route.geometry.coordinates.map(([lng, lat]) => [lat, lng] as [number, number])
  const color = getRouteColor(route.transportMode)

  return <Polyline positions={coordinates} color={color} weight={5} opacity={0.7} />
}

export default RouteLayer

import { Location, Route, TransportMode } from './route.types'

export interface MapState {
  center: [number, number]
  zoom: number
  selectedPoint: Location | null
  waitingForInput: 'origin' | 'destination' | 'waypoint' | null
  setCenter: (center: [number, number]) => void
  setZoom: (zoom: number) => void
  setSelectedPoint: (point: Location | null) => void
  setWaitingForInput: (type: 'origin' | 'destination' | 'waypoint' | null) => void
  clearWaitingForInput: () => void
}

export interface RouteState {
  origin: Location | null
  destination: Location | null
  waypoints: Location[]
  route: Route | null
  transportMode: TransportMode
  isLoading: boolean
  setOrigin: (location: Location | null) => void
  setDestination: (location: Location | null) => void
  setWaypoints: (waypoints: Location[]) => void
  addWaypoint: (waypoint: Location) => void
  removeWaypoint: (index: number) => void
  setRoute: (route: Route | null) => void
  setTransportMode: (mode: TransportMode) => void
  reset: () => void
  setIsLoading: (loading: boolean) => void
}

export interface SearchState {
  query: string
  results: Location[]
  isLoading: boolean
  setQuery: (query: string) => void
  setResults: (results: Location[]) => void
  setIsLoading: (loading: boolean) => void
  clear: () => void
}

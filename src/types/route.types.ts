export interface Location {
  lat: number
  lng: number
  name?: string
  address?: string
}

export interface Route {
  waypoints: Location[]
  totalDistance: number // meters
  totalDuration: number // seconds
  transportMode: TransportMode
  geometry: GeoJSON.LineString
  instructions?: string[]
}

export enum TransportMode {
  DRIVING = 'driving',
  BICYCLING = 'cycling',
  WALKING = 'walking',
}

export interface RouteInfo {
  distance: string
  duration: string
  mode: TransportMode
}

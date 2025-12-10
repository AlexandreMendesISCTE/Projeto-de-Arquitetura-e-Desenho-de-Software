export const OSRM_BASE_URL =
  import.meta.env.VITE_OSRM_BASE_URL || 'https://router.project-osrm.org/route/v1'
export const NOMINATIM_BASE_URL =
  import.meta.env.VITE_NOMINATIM_BASE_URL || '/nominatim'
export const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || ''

export const MAP_DEFAULT_CENTER: [number, number] = [
  parseFloat(import.meta.env.VITE_MAP_DEFAULT_CENTER_LAT || '38.7223'),
  parseFloat(import.meta.env.VITE_MAP_DEFAULT_CENTER_LNG || '-9.1393'),
]
export const MAP_DEFAULT_ZOOM = parseInt(import.meta.env.VITE_MAP_DEFAULT_ZOOM || '13')

export const HTTP_TIMEOUT = 30000 // 30 seconds

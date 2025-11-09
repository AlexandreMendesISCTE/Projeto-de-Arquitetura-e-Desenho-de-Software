/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_OSRM_BASE_URL: string
  readonly VITE_NOMINATIM_BASE_URL: string
  readonly VITE_MAP_DEFAULT_CENTER_LAT: string
  readonly VITE_MAP_DEFAULT_CENTER_LNG: string
  readonly VITE_MAP_DEFAULT_ZOOM: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}


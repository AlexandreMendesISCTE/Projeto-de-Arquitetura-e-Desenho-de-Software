import { create } from 'zustand'

interface POIState {
  enabled: boolean
  toggle: () => void
  setEnabled: (enabled: boolean) => void
}

export const usePOIStore = create<POIState>((set) => ({
  enabled: false,
  toggle: () => set((state) => ({ enabled: !state.enabled })),
  setEnabled: (enabled) => set({ enabled }),
}))


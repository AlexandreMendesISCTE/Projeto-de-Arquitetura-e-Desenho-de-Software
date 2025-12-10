// Shared state for search query/results/loading used by LocationSearch
import { create } from 'zustand'
import { SearchState } from '../types/store.types'

export const useSearchStore = create<SearchState>((set) => ({
  query: '',
  results: [],
  isLoading: false,
  setQuery: (query) => set({ query }),
  setResults: (results) => set({ results }),
  setIsLoading: (loading) => set({ isLoading: loading }),
  clear: () => set({ query: '', results: [], isLoading: false }),
}))

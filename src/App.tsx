import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import MapRouteExplorer from './components/MapRouteExplorer'
import ChatWidget from './components/ChatWidget'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      refetchOnWindowFocus: false,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <MapRouteExplorer />
      <ChatWidget />
    </QueryClientProvider>
  )
}

export default App


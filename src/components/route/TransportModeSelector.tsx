import { useState } from 'react'
import { Car, Bike, Footprints, Train, ChevronDown } from 'lucide-react'
import { useRouteStore } from '../../store/route.store'
import { TransportMode } from '../../types/route.types'

const TransportModeSelector = () => {
  const { transportMode, setTransportMode } = useRouteStore()
  const [isExpanded, setIsExpanded] = useState(false)

  const modes = [
    { mode: TransportMode.DRIVING, label: 'Carro', icon: Car, color: 'bg-blue-500' },
    { mode: TransportMode.BICYCLING, label: 'Bicicleta', icon: Bike, color: 'bg-green-500' },
    { mode: TransportMode.WALKING, label: 'A pé', icon: Footprints, color: 'bg-orange-500' },
    { mode: TransportMode.TRANSIT, label: 'Transporte', icon: Train, color: 'bg-purple-500' },
  ]

  const currentMode = modes.find((m) => m.mode === transportMode) || modes[0]

  const handleModeSelect = (mode: TransportMode) => {
    setTransportMode(mode)
    setIsExpanded(false)
  }

  return (
    <>
      {/* Desktop view - always visible */}
      <div className="hidden md:block absolute top-4 right-4 z-[1000] bg-white rounded-lg shadow-lg p-2 flex gap-2">
        {modes.map(({ mode, label, icon: Icon, color }) => (
          <button
            key={mode}
            onClick={() => setTransportMode(mode)}
            className={`px-3 py-2 rounded-md transition-all flex items-center justify-center gap-2 text-sm min-h-[44px] ${
              transportMode === mode
                ? `${color} text-white shadow-md`
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            <Icon className="w-4 h-4" />
            <span className="font-medium">{label}</span>
          </button>
        ))}
      </div>

      {/* Mobile view - collapsible */}
      <div className="md:hidden w-full">
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className={`w-full px-3 py-2.5 rounded-md transition-all flex items-center justify-between text-sm min-h-[44px] ${
            currentMode.color
          } text-white shadow-md`}
        >
          <div className="flex items-center gap-2">
            <currentMode.icon className="w-4 h-4" />
            <span className="font-medium">{currentMode.label}</span>
          </div>
          <ChevronDown
            className={`w-4 h-4 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`}
          />
        </button>

        <div
          className={`overflow-hidden transition-all duration-300 ease-in-out ${
            isExpanded ? 'max-h-48 opacity-100 mt-2' : 'max-h-0 opacity-0'
          }`}
        >
          <div className="bg-white rounded-lg shadow-lg p-2 flex flex-col gap-2">
            {modes
              .filter((m) => m.mode !== transportMode)
              .map(({ mode, label, icon: Icon, color }) => (
                <button
                  key={mode}
                  onClick={() => handleModeSelect(mode)}
                  className={`px-3 py-2.5 rounded-md transition-all flex items-center gap-2 text-sm min-h-[44px] ${
                    color
                  } text-white shadow-md hover:opacity-90`}
                >
                  <Icon className="w-4 h-4" />
                  <span className="font-medium">{label}</span>
                </button>
              ))}
          </div>
        </div>
      </div>
    </>
  )
}

export default TransportModeSelector

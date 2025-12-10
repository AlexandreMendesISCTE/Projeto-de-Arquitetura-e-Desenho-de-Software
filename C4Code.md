```mermaid
C4Code
    title Diagrama de Codigo - Stores Zustand

    Container_Boundary(route_store, "route.store.ts") {
        Class(route_state, "RouteState", "Interface TypeScript", "Estado da rota atual")
        Class(route_actions, "RouteActions", "Funcoes", "Acoes para manipular rotas")
        Class(route_store_instance, "RouteStore", "Zustand Store", "Instancia do store Zustand")
    }

    Container_Boundary(map_store, "map.store.ts") {
        Class(map_state, "MapState", "Interface TypeScript", "Estado do mapa")
        Class(map_actions, "MapActions", "Funcoes", "Acoes para mapa")
        Class(map_store_instance, "MapStore", "Zustand Store", "Instancia do store")
    }

    Rel(route_store_instance, route_state, "Contem")
    Rel(route_store_instance, route_actions, "Implementa")
    Rel(map_store_instance, map_state, "Contem")
    Rel(map_store_instance, map_actions, "Implementa")

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```
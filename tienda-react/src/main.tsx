// Punto de entrada: React en modo estricto + enrutador integrado vía RouterProvider.
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import './index.css'
import './styles/bootstrap.min.css'
import './styles/libreria.css'
import './styles/app.css'
import './styles/tienda.css'
import './styles/auth.css'
import './styles/perfil.css'
import { CarritoProvider } from './client/context/CarritoContext'
import { rootRouter } from './router/app.router'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {/* El carrito se comparte entre catálogo, carrito, perfil y panel admin */}
    <CarritoProvider>
      {/* RouterProvider entrega el enrutador centralizado a toda la SPA */}
      <RouterProvider router={rootRouter} />
    </CarritoProvider>
  </StrictMode>,
)

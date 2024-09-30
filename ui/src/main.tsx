import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./index.css";
import { ContextProvider } from "./context/GlobalContext.tsx";
import { routesConfig, futureFlags } from "./helpers/helpers";

const router = createBrowserRouter(routesConfig, futureFlags);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ContextProvider>
      <RouterProvider router={router} />
    </ContextProvider>
  </StrictMode>
);

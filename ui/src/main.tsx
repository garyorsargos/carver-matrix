import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import "./index.css";
import { ContextProvider } from "./context/GlobalContext.tsx";
import { UnsavedChangesProvider } from "./context/UnsavedChangesContext";
import { routesConfig, futureFlags } from "./helpers/helpers";

const router = createBrowserRouter(routesConfig, futureFlags);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ContextProvider>
      <UnsavedChangesProvider>
        <RouterProvider router={router} />
      </UnsavedChangesProvider>
    </ContextProvider>
  </StrictMode>
);

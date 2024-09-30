import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { ROUTES, routesConfig, USER_ROLES } from "../../../helpers/helpers";
import { GlobalContext } from "../../../context/GlobalContext";
import { Api } from "../../../api/api";

describe("Redirect", () => {
  test("automatically redirects to the /Home route when the requested route doesn't exist", () => {
    const router = createMemoryRouter(routesConfig, {
      initialEntries: ["/fake-route", ...Object.values(ROUTES)],
      initialIndex: 0,
    });

    render(
      <GlobalContext.Provider
        value={{ roles: [], makeRequest: new Api(() => {}) }}
      >
        <RouterProvider router={router} />
      </GlobalContext.Provider>
    );

    expect(screen.queryByTestId("app-page")).toBeInTheDocument();
  });

  test("allows the user access to a page with no admin restrictions", () => {
    const router = createMemoryRouter(routesConfig, {
      initialEntries: [...Object.values(ROUTES)],
      initialIndex: 1,
    });

    render(
      <GlobalContext.Provider
        value={{ roles: [], makeRequest: new Api(() => {}) }}
      >
        <RouterProvider router={router} />
      </GlobalContext.Provider>
    );

    expect(screen.getByTestId("chat-page")).toHaveTextContent("Chat Bot Page");
  });

  test("does not allow a user access to a page with admin restrictions if they don't have admin rights", () => {
    const router = createMemoryRouter(routesConfig, {
      initialEntries: [...Object.values(ROUTES)],
      initialIndex: 2,
    });

    render(
      <GlobalContext.Provider
        value={{ roles: [], makeRequest: new Api(() => {}) }}
      >
        <RouterProvider router={router} />
      </GlobalContext.Provider>
    );

    expect(screen.queryByTestId("app")).toBeInTheDocument();
  });

  test("allows a user access to a page with admin restrictions if they have admin rights", () => {
    const router = createMemoryRouter(routesConfig, {
      initialEntries: [...Object.values(ROUTES)],
      initialIndex: 2,
    });

    render(
      <GlobalContext.Provider
        value={{
          roles: [USER_ROLES.admin],
          makeRequest: new Api(() => [USER_ROLES.admin]),
        }}
      >
        <RouterProvider router={router} />
      </GlobalContext.Provider>
    );

    expect(screen.queryByTestId("admin-page")).toBeInTheDocument();
  });
});

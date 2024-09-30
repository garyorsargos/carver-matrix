import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { ROUTES, routesConfig } from "../../helpers/helpers";
import { GlobalContext } from "../../context/GlobalContext";
import { Api } from "../../api/api";

describe("App", () => {
  test("clicking the 'start chat' button should redirect to the Chat Page", async () => {
    const router = createMemoryRouter(routesConfig, {
      initialEntries: Object.values(ROUTES),
      initialIndex: 0,
    });

    render(
      <GlobalContext.Provider
        value={{ roles: [], makeRequest: new Api(() => {}) }}
      >
        <RouterProvider router={router} />
      </GlobalContext.Provider>
    );

    fireEvent.click(screen.getByTestId("start-chat-button"));

    await waitFor(() => {
      expect(screen.queryByTestId("app-page")).toBeNull();
      expect(screen.queryByTestId("chat-page")).toBeInTheDocument();
    });
  });
});

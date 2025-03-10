import { Outlet } from "react-router-dom";
import App from "../App";
import { Redirect } from "../components/navigation/Redirect";
import Admin from "../pages/Admin";
import CreateMatrix from "../pages/CreateMatrix";
import EditMatrix from "../pages/EditMatrix";
import ViewMatrix from "../pages/ViewMatrix";
import Landing from "../pages/Landing";

// User Roles
export const USER_ROLES = {
  admin: "STARTER_ADMIN",
  read: "STARTER_READ",
  write: "STARTER_WRITE",
};

// The Red Theme Object
export const redTheme: object = {
  palette: {
    action: {
      active: "#0000008F",
      hover: "#0000000A",
      selected: "#00000014",
      disabledBackground: "#0000001F",
      focus: "#0000001F",
      disabled: "#00000061",
    },
    text: {
      primary: "#000000DE",
      secondary: "#00000099",
      disabled: "#00000061",
    },
    primary: {
      main: "#221F20",
      dark: "#221F20",
      light: "#424242",
      contrastText: "#FFF",
    },
    secondary: {
      main: "#690005",
      dark: "#3C0406",
      light: "#A10D14",
      contrastText: "#FFF",
    },
    error: {
      main: "#D32F2F",
      dark: "#C62828",
      light: "#EF5350",
      contrastText: "#FFF",
    },
    warning: {
      main: "#EF6C00",
      dark: "#E65100",
      light: "#FF9800",
      contrastText: "#FFF",
    },
    info: {
      main: "#B5A583",
      dark: "#958054",
      light: "#ACA596",
      contrastText: "#FFF",
    },
    success: {
      main: "#3D583D",
      dark: "#2F472F",
      light: "#6A936A",
      contrastText: "#FFF",
    },
  },
  components: {
    MuiButton: {
      variants: [
        {
          props: { variant: "outlined" },
          style: {
            border: "1px solid #97875E50",
          },
        },
      ],
    },
  },
};

export const blueTheme: object = {
  palette: {
    action: {
      active: "#0000008F",
      hover: "#0000000A",
      selected: "#00000014",
      disabledBackground: "#0000001F",
      focus: "#0000001F",
      disabled: "#00000061",
    },
    text: {
      primary: "#FFF",
      secondary: "#00000099",
      disabled: "#00000061",
    },
    primary: {
      main: "#014093",
      dark: "#012B61",
      light: "#3875C5",
      contrastText: "#FFF",
    },
    secondary: {
      main: "#690005",
      dark: "#3C0406",
      light: "#A10D14",
      contrastText: "#FFF",
    },
    error: {
      main: "#D32F2F",
      dark: "#C62828",
      light: "#EF5350",
      contrastText: "#FFF",
    },
    warning: {
      main: "#EF6C00",
      dark: "#E65100",
      light: "#FF9800",
      contrastText: "#FFF",
    },
    info: {
      main: "#B5A583",
      dark: "#958054",
      light: "#ACA596",
      contrastText: "#FFF",
    },
    success: {
      main: "#3D583D",
      dark: "#2F472F",
      light: "#6A936A",
      contrastText: "#FFF",
    },
  },
  components: {
    MuiButton: {
      variants: [
        {
          props: { variant: "outlined" },
          style: {
            border: "1px solid #01409380",
          },
        },
      ],
    },
  },
};

export const ROUTES: { [key: string]: string } = {
  home: "/Home",
  admin: "/Admin",
  landing: "/Landing",
  createMatrix: "/ModifyMatrix",
  editMatrix: "/EditMatrix",
  viewMatrix: "/ViewMatrix",
};

export const ADMIN_ROUTES: string[] = [ROUTES.admin];

export const routesConfig = [
  {
    path: "*",
    element: (
      <Redirect>
        <></>
      </Redirect>
    ),
  },
  {
    path: "/",
    element: (
      <Redirect>
        <Outlet />
      </Redirect>
    ),
    children: [
      {
        path: ROUTES.home,
        element: <App />,
      },
      {
        path: ROUTES.landing,
        element: <Landing userName="User" />,
      },
      {
        path: ROUTES.admin,
        element: <Admin />,
      },
      {
        path: ROUTES.createMatrix,
        element: <CreateMatrix />,
      },
      {
        path: ROUTES.editMatrix,
        element: <EditMatrix />,
      },
      {
        path: ROUTES.viewMatrix,
        element: <ViewMatrix />,
      },
    ],
  },
];

// Begin incorporation of react router "Future Flags"
export const futureFlags = {
  future: {
    v7_normalizeFormMethod: true,
    v7_fetcherPersist: true,
    v7_partialHydration: true,
    v7_relativeSplatPath: true,
    v7_skipActionErrorRevalidation: true,
  },
};

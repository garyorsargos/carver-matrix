import { useContext, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { GlobalContext } from "../../context/GlobalContext";
import {
  ADMIN_ROUTES,
  blueTheme,
  ROUTES,
  USER_ROLES,
} from "../../helpers/helpers";
import { Theme, ThemeProvider } from "@emotion/react";
import { SpaceDashboardOutlined } from "@mui/icons-material";
import {
  createTheme,
  AppBar,
  Paper,
  Toolbar,
  Box,
  IconButton,
  Typography,
  Stack,
  Menu,
  MenuItem,
  Avatar,
  Divider,
} from "@mui/material";
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import LogoutIcon from '@mui/icons-material/Logout';

interface RedirectProps {
  children: React.ReactElement | React.ReactElement[];
}

/**
 * Redirect is the primary application component. Its holds
 * all other routes for the application, and is constantly
 * listening to the Global Context value "roles". If "roles"
 * doesn't contain the appropriate role, the user will be
 * instantly redirected away from the page back to the /Home
 * page route. This prevents URL fuzzing as well (when a user
 * attempts to manually type in a URL they know exists).
 *
 * @param children React Element or Elements
 * @returns The component(s) associated with the current URI
 */
export const Redirect: React.FC<RedirectProps> = ({ children }) => {
  const { roles } = useContext(GlobalContext);
  const navigate = useNavigate();
  const location = useLocation();
  // 64 pixels accounts for the application bar offset
  const [height, setHeight] = useState<number>(window.innerHeight);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleProfile = () => {
    handleProfileMenuClose();
    navigate(ROUTES.profile);
  };

  const handleLogout = () => {
    handleProfileMenuClose();
    // Clear all cookies
    document.cookie.split(";").forEach((cookie) => {
      const eqPos = cookie.indexOf("=");
      const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
    });
    // Redirect to Keycloak logout
    window.location.href = '/logout';
  };

  /**
   * Checks the current route for validity, and reroutes a user
   * if they are not allowed to be on the page.
   */
  useEffect(() => {
    if (!Object.values(ROUTES).includes(location.pathname)) {
      navigate(ROUTES.landing);
    }
    if (
      ADMIN_ROUTES.includes(location.pathname) &&
      !roles.includes(USER_ROLES.admin)
    ) {
      navigate(ROUTES.landing);
    }
  }, [roles, location.pathname]);

  /**
   * Used as the event listener handler for resizes of the browser
   */
  const handleResize = (): void => {
    setHeight(window.innerHeight);
  };

  /**
   * Sets the event listener on the window for resize, and removes it
   * upon unmount from the DOM.
   */
  useEffect(() => {
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  /**
   * Create the theme object which injects styles into all other
   * MUI components.
   */
  const defaultTheme = createTheme({
    ...blueTheme,
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    palette: { ...blueTheme.palette, mode: "dark" },
  } as Theme);

  // Height - 64 accounts for the height of the Appbar
  // Offset the height difference to fill the page top to bottom
  return (
    <div
      style={{
        height: height - 64,
        width: "100%",
        overflowY: "hidden",
        position: "absolute",
        top: "64px",
      }}
    >
      <ThemeProvider theme={defaultTheme}>
        <AppBar>
          <Paper sx={{ backgroundColor: "#000" }}>
            <Toolbar
              style={{ display: "flex", justifyContent: "space-between" }}
            >
              <Box
                style={{
                  display: "flex",
                  justifyContent: "space-around",
                  alignItems: "center",
                }}
              >
                <IconButton onClick={() => navigate(ROUTES.viewMatrix)}>
                  <img src="/AIDIV-logo.svg" />
                </IconButton>
                <Typography variant="h5">CARVER Matrix App</Typography>
              </Box>
              <Box
                style={{
                  display: "flex",
                  justifyContent: "space-around",
                  alignItems: "center",
                  gap: "16px",
                }}
              >
                {/** roles is a state-managed object, so this component will render in real time if roles is updated */}
                {roles.includes(USER_ROLES.admin) ? (
                  <>
                    <Typography variant="h6">Dashboard</Typography>
                    <Stack>
                      <IconButton onClick={() => navigate(ROUTES.admin)}>
                        <SpaceDashboardOutlined
                          sx={{ color: "#FFF" }}
                        ></SpaceDashboardOutlined>
                      </IconButton>
                    </Stack>
                  </>
                ) : (
                  <></>
                )}
                <IconButton
                  onClick={handleProfileMenuOpen}
                  sx={{
                    color: '#FFF',
                    '&:hover': {
                      backgroundColor: 'rgba(255, 255, 255, 0.1)',
                    },
                  }}
                >
                  <Avatar sx={{ bgcolor: '#014093', width: 32, height: 32 }}>
                    <AccountCircleIcon />
                  </Avatar>
                </IconButton>
                <Menu
                  anchorEl={anchorEl}
                  open={Boolean(anchorEl)}
                  onClose={handleProfileMenuClose}
                  PaperProps={{
                    sx: {
                      backgroundColor: 'rgba(0, 0, 0, 0.9)',
                      backdropFilter: 'blur(10px)',
                      border: '1px solid rgba(255, 255, 255, 0.1)',
                      mt: 1,
                    },
                  }}
                  transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                  anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                >
                  <MenuItem
                    onClick={handleProfile}
                    sx={{
                      color: '#FFF',
                      '&:hover': { backgroundColor: 'rgba(255, 255, 255, 0.1)' },
                    }}
                  >
                    <AccountCircleIcon sx={{ mr: 1 }} />
                    Profile
                  </MenuItem>
                  <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.1)' }} />
                  <MenuItem
                    onClick={handleLogout}
                    sx={{
                      color: '#FFF',
                      '&:hover': { backgroundColor: 'rgba(255, 255, 255, 0.1)' },
                    }}
                  >
                    <LogoutIcon sx={{ mr: 1 }} />
                    Logout
                  </MenuItem>
                </Menu>
              </Box>
            </Toolbar>
          </Paper>
        </AppBar>
        {children}
      </ThemeProvider>
    </div>
  );
};

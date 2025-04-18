import { useContext, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { GlobalContext } from "../../context/GlobalContext";
import { useUnsavedChanges } from "../../context/UnsavedChangesContext";
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
  Breadcrumbs,
  Link,
} from "@mui/material";
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import LogoutIcon from '@mui/icons-material/Logout';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';

interface RedirectProps {
  children: React.ReactElement | React.ReactElement[];
}

export const Redirect: React.FC<RedirectProps> = ({ children }) => {
  const { roles } = useContext(GlobalContext);
  const navigate = useNavigate();
  const location = useLocation();
  const [height, setHeight] = useState<number>(window.innerHeight);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { hasUnsavedChanges, showUnsavedChangesDialog } = useUnsavedChanges();

  // Map routes to display names for breadcrumbs
  const routeNames = {
    [ROUTES.landing]: "Home",
    [ROUTES.createMatrix]: "Create Matrix",
    [ROUTES.editMatrix]: "Edit Matrix",
    [ROUTES.viewMatrix]: "View Matrices",
    [ROUTES.profile]: "Profile",
    [ROUTES.admin]: "Admin Dashboard",
  };

  // Define parent-child relationships for routes
  const routeHierarchy = {
    [ROUTES.viewMatrix]: [ROUTES.createMatrix, ROUTES.editMatrix],
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  const handleProfile = () => {
    handleProfileMenuClose();
    if (hasUnsavedChanges) {
      showUnsavedChangesDialog(() => navigate(ROUTES.profile));
    } else {
      navigate(ROUTES.profile);
    }
  };

  const handleLogout = () => {
    handleProfileMenuClose();
    
    // Get the base URL of the application
    const baseUrl = window.location.origin;
    
    // First hit the OAuth2 proxy's sign_out endpoint to clear the Redis session
    fetch('/oauth2/sign_out', { method: 'GET', credentials: 'include' })
      .finally(() => {
        // Then redirect to Keycloak's logout endpoint with the proper parameters
        const logoutUrl = new URL('https://keycloak.zeus.socom.dev/realms/zeus-apps/protocol/openid-connect/logout');
        logoutUrl.searchParams.append('post_logout_redirect_uri', baseUrl);
        logoutUrl.searchParams.append('client_id', 'starter-app');
        
        window.location.href = logoutUrl.toString();
      });
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

  // Generate breadcrumbs based on current location
  const getBreadcrumbs = () => {
    const pathnames = location.pathname.split('/').filter((x) => x);
    let currentPath = '';
    const breadcrumbPaths: string[] = [];

    // Build the breadcrumb paths array
    pathnames.forEach((value) => {
      currentPath += `/${value}`;
      
      // Check if this path needs a parent path inserted before it
      Object.entries(routeHierarchy).forEach(([parent, children]) => {
        if (children.includes(currentPath) && !breadcrumbPaths.includes(parent)) {
          breadcrumbPaths.push(parent);
        }
      });
      
      breadcrumbPaths.push(currentPath);
    });

    const handleNavigation = (path: string) => {
      if (hasUnsavedChanges) {
        showUnsavedChangesDialog(() => navigate(path));
      } else {
        navigate(path);
      }
    };

    return (
      <Breadcrumbs 
        separator={<NavigateNextIcon sx={{ color: 'rgba(255, 255, 255, 0.7)', fontSize: '1.2rem' }} />}
        sx={{ ml: 2 }}
      >
        <Link
          component="button"
          onClick={() => handleNavigation(ROUTES.landing)}
          sx={{
            color: '#ffffff',
            textDecoration: 'none',
            '&:hover': {
              textDecoration: 'underline',
            },
            fontSize: '1rem',
            fontFamily: "'Roboto Condensed', sans-serif",
          }}
        >
          CARVER Dashboard
        </Link>
        {breadcrumbPaths.map((path, index) => {
          const isLast = index === breadcrumbPaths.length - 1;
          const routeName = routeNames[path] || path;

          return isLast ? (
            <Typography
              key={path}
              sx={{
                color: 'rgba(255, 255, 255, 0.7)',
                fontSize: '1rem',
                fontFamily: "'Roboto Condensed', sans-serif",
              }}
            >
              {routeName}
            </Typography>
          ) : (
            <Link
              key={path}
              component="button"
              onClick={() => handleNavigation(path)}
              sx={{
                color: '#ffffff',
                textDecoration: 'none',
                '&:hover': {
                  textDecoration: 'underline',
                },
                fontSize: '1rem',
                fontFamily: "'Roboto Condensed', sans-serif",
              }}
            >
              {routeName}
            </Link>
          );
        })}
      </Breadcrumbs>
    );
  };

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
                  justifyContent: "flex-start",
                  alignItems: "center",
                  flex: 1,
                }}
              >
                <IconButton onClick={() => {
                  if (hasUnsavedChanges) {
                    showUnsavedChangesDialog(() => navigate(ROUTES.landing));
                  } else {
                    navigate(ROUTES.landing);
                  }
                }}>
                  <img src="/AIDIV-logo.svg" />
                </IconButton>
                {getBreadcrumbs()}
              </Box>
              <Box
                style={{
                  display: "flex",
                  justifyContent: "flex-end",
                  alignItems: "center",
                  gap: "16px",
                }}
              >
                {/** roles is a state-managed object, so this component will render in real time if roles is updated */}
                {roles.includes(USER_ROLES.admin) ? (
                  <>
                    <Typography variant="h6">Dashboard</Typography>
                    <Stack>
                      <IconButton onClick={() => {
                        if (hasUnsavedChanges) {
                          showUnsavedChangesDialog(() => navigate(ROUTES.admin));
                        } else {
                          navigate(ROUTES.admin);
                        }
                      }}>
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

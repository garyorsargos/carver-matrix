import { useNavigate } from "react-router-dom";
import "./App.css";
import { ROUTES } from "./helpers/helpers";
import {
  Box,
  Button,
  Card,
  List,
  ListItem,
  ListItemText,
  Typography,
} from "@mui/material";
import PageContainer from "./components/containers/PageContainer";

export const App = () => {
  const navigate = useNavigate();

  return (
    <PageContainer testId="app-page">
      <Box
        style={{
          width: "100%",
          display: "flex",
          justifyContent: "center",
          position: "relative",
          top: "200px",
        }}
      >
        <Box
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
          }}
        >
          <img src="/caib-logo.png" />
          <Button
            data-testid="start-edit-matrix"
            variant="contained"
            sx={{ borderRadius: "18px" }}
            onClick={() => navigate(ROUTES.editMatrix)}
          >
            Edit
          </Button>
          <Button
            data-testid="start-create-matrix"
            variant="contained"
            sx={{ borderRadius: "18px" }}
            onClick={() => navigate(ROUTES.createMatrix)}
          >
            Create
          </Button>
          <Button
            data-testid="start-register"
            variant="contained"
            sx={{ borderRadius: "18px" }}
            onClick={() => navigate(ROUTES.register)}
          >
            Register
          </Button>
        </Box>
        <Card
          sx={{
            backgroundColor: "#000",
            paddingTop: 1,
            paddingRight: 3,
            paddingBottom: 1,
            paddingLeft: 3,
            margin: 5,
          }}
          style={{
            border: "1px solid #014093",
            borderRadius: "10px",
          }}
        >
          <Typography data-testid="app" variant="h4" sx={{ color: "#FFF" }}>
            Carver Matrix Digital Board
          </Typography>
          <Typography variant="h5" sx={{ color: "#FFF" }}>
            A new way to fill out and collaborate on CARVER Matrices
          </Typography>
          <List dense>
            <ListItem>
              <ListItemText primary="Ease of use: Our tools make it straightforward to fill out a matrix." />
            </ListItem>
            <ListItem>
              <ListItemText primary="EXAMPLE: description." />
            </ListItem>
            <ListItem>
              <ListItemText primary="EXAMPLE: description." />
            </ListItem>
            <ListItem>
              <ListItemText primary="EXAMPLE: description." />
            </ListItem>
          </List>
        </Card>
      </Box>
    </PageContainer>
  );
};

export default App;

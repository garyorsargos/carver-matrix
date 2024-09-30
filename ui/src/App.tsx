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
            data-testid="start-chat-button"
            variant="contained"
            sx={{ borderRadius: "18px" }}
            onClick={() => navigate(ROUTES.chat)}
          >
            Start Chat
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
            Meet CAIB
          </Typography>
          <Typography variant="h5" sx={{ color: "#FFF" }}>
            A powerful AI-driven chatbot
          </Typography>
          <List dense>
            <ListItem>
              <ListItemText
                primary="INSTANT RESPONSES: Get quick and accurate answers to your
                queries."
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="CREATIVE COLLABORATION: Generate ideas, draft content, and
                explore new perspectives."
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="NATURAL CONVERSATIONS: Engage in human-like interactions that
                are intuitive and relavent."
              />
            </ListItem>
            <ListItem>
              <ListItemText
                primary="CONTINUOUS LEARNING: Benefit from an AI that evolves and
                improves wwith each prompt."
              />
            </ListItem>
          </List>
        </Card>
      </Box>
    </PageContainer>
  );
};

export default App;

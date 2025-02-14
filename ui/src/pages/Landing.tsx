import React from "react";
import { Box, Button, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../helpers/helpers";

interface LandingProps {
  userName: string;
}

const Landing: React.FC<LandingProps> = ({ userName }) => {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        height: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 2,
        backgroundColor: "white",
        padding: 2,
      }}
    >
      <Typography variant="h3" sx={{ fontWeight: "bold", marginBottom: 3 }}>
        Hello, {userName}
      </Typography>

      <Box
        component="img"
        src="/caib-logo.png"
        alt="Landing Page Image"
        sx={{
          width: "300px",
          height: "200px",
          objectFit: "cover",
          borderRadius: 2,
        }}
      />

      <Box sx={{ display: "flex", gap: 2 }}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate(ROUTES.createMatrix)}
        >
          Create Matrix
        </Button>
        <Button
          variant="outlined"
          color="primary"
          onClick={() => navigate(ROUTES.home)}
        >
          Back to Home
        </Button>
      </Box>
    </Box>
  );
};

export default Landing;

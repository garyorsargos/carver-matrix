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
        backgroundColor: "#f5f5f5",
        padding: 2,
      }}
    >
      <Typography variant="h5" sx={{ fontWeight: "bold" }}>
        Hello, {userName}
      </Typography>

      <Box
        component="img"
        src="https://via.placeholder.com/300"
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
          Create
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

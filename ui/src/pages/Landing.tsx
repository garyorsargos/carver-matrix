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
        minHeight: "100vh",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 2,
        backgroundColor: "#D3D3D3",
        padding: 2,
        margin: 0,
        boxSizing: "border-box",
        overflowX: "hidden",
      }}
    >
      <Typography 
        variant="h3" 
        sx={{ 
          fontWeight: "bold", 
          marginBottom: 3,
          textAlign: "center"
        }}
      >
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
          display: "block",
          margin: "0 auto",
        }}
      />

      <Box 
        sx={{ 
          display: "flex", 
          gap: 2,
          justifyContent: "center",
          width: "100%",
        }}
      >
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate(ROUTES.createMatrix)}
        >
          Create Matrix
        </Button>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate(ROUTES.viewMatrix)}
        >
          View Matrices
        </Button>
      </Box>
    </Box>
  );
};

export default Landing;

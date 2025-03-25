import React from "react";
import { Box, Button, Typography, Container, Paper, Grid } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../helpers/helpers";
import MilitaryTechIcon from '@mui/icons-material/MilitaryTech';
import AssessmentIcon from '@mui/icons-material/Assessment';
import GroupIcon from '@mui/icons-material/Group';

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
        backgroundColor: "#1a1a1a",
        color: "#ffffff",
        position: "relative",
        overflow: "auto",
      }}
    >
      {/* Hero Section with Military Pattern Background */}
      <Box
        sx={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          height: "100%",
          backgroundImage: "linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7)), url('/military-pattern.svg')",
          backgroundSize: "100px 100px",
          backgroundPosition: "center",
          opacity: 0.1,
          zIndex: 0,
        }}
      />

      {/* Main Content */}
      <Container maxWidth="lg" sx={{ position: "relative", zIndex: 1, py: 4, minHeight: "100vh" }}>
        <Grid container spacing={3} alignItems="center">
          {/* Left Column - Welcome Section */}
          <Grid item xs={12} md={6}>
            <Box sx={{ mb: 2 }}>
              <Typography
                variant="h2"
                sx={{
                  fontWeight: 800,
                  color: "#ffffff",
                  textTransform: "uppercase",
                  letterSpacing: "2px",
                  mb: 1,
                  fontFamily: "'Roboto Condensed', sans-serif",
                  fontSize: { xs: '2.5rem', md: '3rem' }
                }}
              >
                Welcome, {userName}
              </Typography>
              <Typography
                variant="h5"
                sx={{
                  color: "#b5a583",
                  mb: 2,
                  fontFamily: "'Roboto Condensed', sans-serif",
                  fontSize: { xs: '1.2rem', md: '1.5rem' }
                }}
              >
                CARVER Matrix Digital Board
              </Typography>
              <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={() => navigate(ROUTES.createMatrix)}
                  sx={{
                    backgroundColor: "#014093",
                    "&:hover": {
                      backgroundColor: "#012B61",
                    },
                    borderRadius: "4px",
                    textTransform: "uppercase",
                    fontWeight: "bold",
                    letterSpacing: "1px",
                    padding: "8px 16px",
                  }}
                >
                  Create Matrix
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => navigate(ROUTES.viewMatrix)}
                  sx={{
                    borderColor: "#014093",
                    color: "#014093",
                    "&:hover": {
                      borderColor: "#012B61",
                      backgroundColor: "rgba(1, 64, 147, 0.1)",
                    },
                    borderRadius: "4px",
                    textTransform: "uppercase",
                    fontWeight: "bold",
                    letterSpacing: "1px",
                    padding: "8px 16px",
                  }}
                >
                  View Matrices
                </Button>
              </Box>
            </Box>
          </Grid>

          {/* Right Column - Logo */}
          <Grid item xs={12} md={6}>
            <Box
              component="img"
              src="/caib-logo.png"
              alt="CARVER Matrix Logo"
              sx={{
                width: "100%",
                maxWidth: "300px",
                height: "auto",
                display: "block",
                margin: "0 auto",
                filter: "drop-shadow(0 0 10px rgba(255, 255, 255, 0.1))",
              }}
            />
          </Grid>
        </Grid>

        {/* Features Section */}
        <Box sx={{ mt: 4, mb: 4 }}>
          <Typography
            variant="h4"
            sx={{
              textAlign: "center",
              color: "#ffffff",
              mb: 3,
              fontWeight: "bold",
              textTransform: "uppercase",
              letterSpacing: "1px",
              fontSize: { xs: '1.5rem', md: '2rem' }
            }}
          >
            Key Features
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Paper
                sx={{
                  p: 2,
                  height: "100%",
                  backgroundColor: "rgba(255, 255, 255, 0.05)",
                  backdropFilter: "blur(10px)",
                  border: "1px solid rgba(255, 255, 255, 0.1)",
                }}
              >
                <MilitaryTechIcon sx={{ fontSize: 32, color: "#014093", mb: 1 }} />
                <Typography variant="h6" sx={{ color: "#ffffff", mb: 1, fontSize: { xs: '1rem', md: '1.25rem' } }}>
                  Military-Grade Security
                </Typography>
                <Typography variant="body2" sx={{ color: "#b5a583", fontSize: { xs: '0.875rem', md: '1rem' } }}>
                  Enterprise-level security protocols ensuring your data remains protected and confidential.
                </Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} md={4}>
              <Paper
                sx={{
                  p: 2,
                  height: "100%",
                  backgroundColor: "rgba(255, 255, 255, 0.05)",
                  backdropFilter: "blur(10px)",
                  border: "1px solid rgba(255, 255, 255, 0.1)",
                }}
              >
                <AssessmentIcon sx={{ fontSize: 32, color: "#014093", mb: 1 }} />
                <Typography variant="h6" sx={{ color: "#ffffff", mb: 1, fontSize: { xs: '1rem', md: '1.25rem' } }}>
                  Advanced Matrix Analysis
                </Typography>
                <Typography variant="body2" sx={{ color: "#b5a583", fontSize: { xs: '0.875rem', md: '1rem' } }}>
                  Comprehensive CARVER matrix evaluation tools with detailed scoring and analysis capabilities.
                </Typography>
              </Paper>
            </Grid>
            <Grid item xs={12} md={4}>
              <Paper
                sx={{
                  p: 2,
                  height: "100%",
                  backgroundColor: "rgba(255, 255, 255, 0.05)",
                  backdropFilter: "blur(10px)",
                  border: "1px solid rgba(255, 255, 255, 0.1)",
                }}
              >
                <GroupIcon sx={{ fontSize: 32, color: "#014093", mb: 1 }} />
                <Typography variant="h6" sx={{ color: "#ffffff", mb: 1, fontSize: { xs: '1rem', md: '1.25rem' } }}>
                  Collaborative Environment
                </Typography>
                <Typography variant="body2" sx={{ color: "#b5a583", fontSize: { xs: '0.875rem', md: '1rem' } }}>
                  Real-time collaboration tools for seamless team coordination.
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        </Box>
      </Container>
    </Box>
  );
};

export default Landing;

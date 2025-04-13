import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Divider,
  Skeleton,
  Button,
} from '@mui/material';
import axios from 'axios';
import SettingsIcon from '@mui/icons-material/Settings';

interface UserData {
  userId: number;
  keycloakId: string;
  firstName: string | null;
  lastName: string | null;
  fullName: string | null;
  username: string;
  email: string;
  createdAt: string;
}

const Profile: React.FC = () => {
  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await axios.get('/api/user2/whoami-upsert', { responseType: 'text' });
        const dataString = response.data;
        let parsedData;
        
        // Handle the double JSON response format
        if (dataString.includes('}{')) {
          const parts = dataString.split('}{');
          parsedData = JSON.parse(parts[0] + '}');
        } else {
          parsedData = JSON.parse(dataString);
        }
        
        setUserData(parsedData);
      } catch (error) {
        console.error('Error fetching user data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  const InfoRow = ({ label, value }: { label: string; value: string | null }) => (
    <Grid container spacing={2} sx={{ mb: 2 }}>
      <Grid item xs={4} sm={3}>
        <Typography
          variant="subtitle2"
          sx={{
            color: 'rgba(255, 255, 255, 0.7)',
            fontWeight: 'bold',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
          }}
        >
          {label}
        </Typography>
      </Grid>
      <Grid item xs={8} sm={9}>
        <Typography
          variant="body1"
          sx={{
            color: '#ffffff',
            fontFamily: "'Roboto Condensed', sans-serif",
          }}
        >
          {value || 'Not provided'}
        </Typography>
      </Grid>
    </Grid>
  );

  const handleAdvancedSettings = () => {
    window.open('https://keycloak.zeus.socom.dev/realms/zeus-apps/account/', '_blank');
  };

  return (
    <Box
      sx={{
        padding: 3,
        minHeight: '100vh',
        backgroundColor: '#1a1a1a',
        position: 'relative',
      }}
    >
      {/* Background Pattern */}
      <Box
        sx={{
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundImage: "linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7)), url('/military-pattern.svg')",
          backgroundSize: "100px 100px",
          backgroundPosition: "center",
          opacity: 0.1,
          zIndex: 0,
        }}
      />
      <Paper
        elevation={3}
        sx={{
          position: 'relative',
          zIndex: 1,
          maxWidth: 800,
          margin: '0 auto',
          padding: 4,
          backgroundColor: 'rgba(255, 255, 255, 0.05)',
          backdropFilter: 'blur(10px)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          borderRadius: 2,
        }}
      >
        <Typography
          variant="h4"
          sx={{
            color: '#ffffff',
            fontWeight: 'bold',
            textTransform: 'uppercase',
            letterSpacing: '1px',
            fontFamily: "'Roboto Condensed', sans-serif",
            mb: 3,
          }}
        >
          Profile Information
        </Typography>

        <Divider sx={{ mb: 4, borderColor: 'rgba(255, 255, 255, 0.1)' }} />

        {loading ? (
          <Box sx={{ mt: 2 }}>
            {[...Array(6)].map((_, index) => (
              <Box key={index} sx={{ mb: 2 }}>
                <Skeleton
                  variant="text"
                  sx={{
                    bgcolor: 'rgba(255, 255, 255, 0.1)',
                    height: 40,
                  }}
                />
              </Box>
            ))}
          </Box>
        ) : (
          userData && (
            <Box>
              <InfoRow label="Username" value={userData.username} />
              <InfoRow label="Email" value={userData.email} />
              <InfoRow label="Full Name" value={userData.fullName} />
              <InfoRow label="First Name" value={userData.firstName} />
              <InfoRow label="Last Name" value={userData.lastName} />
              <InfoRow
                label="Member Since"
                value={new Date(userData.createdAt).toLocaleDateString()}
              />
              
              <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
                <Button
                  variant="outlined"
                  startIcon={<SettingsIcon />}
                  onClick={handleAdvancedSettings}
                  sx={{
                    color: '#ffffff',
                    borderColor: 'rgba(255, 255, 255, 0.3)',
                    '&:hover': {
                      borderColor: '#014093',
                      backgroundColor: 'rgba(1, 64, 147, 0.1)',
                    },
                    textTransform: 'uppercase',
                    fontWeight: 'bold',
                    letterSpacing: '0.5px',
                    padding: '8px 24px',
                  }}
                >
                  Advanced Settings
                </Button>
              </Box>
            </Box>
          )
        )}
      </Paper>
    </Box>
  );
};

export default Profile; 
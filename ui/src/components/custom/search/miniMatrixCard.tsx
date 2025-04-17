import React from "react";
import { Typography, Paper, Box } from "@mui/material";
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import PersonIcon from '@mui/icons-material/Person';

interface MiniMatrixCardProps {
  title: string;
  onSelectMatrix: () => void;
  isHost?: boolean;
  isParticipant?: boolean;
}

const MiniMatrixCard: React.FC<MiniMatrixCardProps> = ({
  title,
  onSelectMatrix,
  isHost = false,
  isParticipant = false,
}) => {
  return (
    <Paper
      sx={{
        padding: 1.5,
        width: '100%',
        borderRadius: 2,
        backgroundColor: "rgba(255, 255, 255, 0.05)",
        backdropFilter: "blur(10px)",
        border: "1px solid rgba(255, 255, 255, 0.1)",
        boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
        cursor: "pointer",
        transition: "all 0.2s ease-in-out",
        "&:hover": {
          backgroundColor: "rgba(255, 255, 255, 0.08)",
          transform: "translateY(-2px)",
          boxShadow: "0 4px 8px rgba(0, 0, 0, 0.2)",
        },
        minHeight: "60px",
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        boxSizing: 'border-box',
      }}
      onClick={onSelectMatrix}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        {isHost && (
          <AdminPanelSettingsIcon sx={{ color: '#4D9FFF', fontSize: 20 }} />
        )}
        {!isHost && isParticipant && (
          <PersonIcon sx={{ color: '#00E676', fontSize: 20 }} />
        )}
        <Typography 
          variant="subtitle2" 
          fontWeight="bold" 
          sx={{ 
            fontSize: "0.9rem",
            fontFamily: "'Roboto Condensed', sans-serif",
            textTransform: "uppercase",
            letterSpacing: "0.5px",
            overflow: "hidden",
            textOverflow: "ellipsis",
            display: "-webkit-box",
            WebkitLineClamp: 2,
            WebkitBoxOrient: "vertical",
            color: "#ffffff",
          }}
        >
          {title || ""}
        </Typography>
      </Box>
    </Paper>
  );
};

export default MiniMatrixCard; 
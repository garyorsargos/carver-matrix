import React from "react";
import { Typography, Paper } from "@mui/material";

interface MiniMatrixCardProps {
  title: string;
  description: string;
  onSelectMatrix: () => void;
  titleColor?: string;
}

const MiniMatrixCard: React.FC<MiniMatrixCardProps> = ({
  title,
  description,
  onSelectMatrix,
  titleColor = "#ffffff",
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
        minHeight: "80px",
        display: 'flex',
        flexDirection: 'column',
        gap: 1,
        boxSizing: 'border-box',
      }}
      onClick={onSelectMatrix}
    >
      <Typography 
        variant="subtitle2" 
        fontWeight="bold" 
        color={titleColor} 
        sx={{ 
          fontSize: "0.9rem",
          fontFamily: "'Roboto Condensed', sans-serif",
          textTransform: "uppercase",
          letterSpacing: "0.5px",
        }}
      >
        {title || ""}
      </Typography>
      <Typography 
        variant="caption" 
        sx={{ 
          display: "-webkit-box",
          WebkitLineClamp: 3,
          WebkitBoxOrient: "vertical",
          overflow: "hidden",
          fontSize: "0.8rem",
          lineHeight: 1.4,
          color: "rgba(255, 255, 255, 0.7)",
          flex: 1,
        }}
      >
        {description || ""}
      </Typography>
    </Paper>
  );
};

export default MiniMatrixCard; 
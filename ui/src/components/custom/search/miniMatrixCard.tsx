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
  titleColor = "gray",
}) => {
  return (
    <Paper
      sx={{
        padding: 2,
        marginBottom: 1,
        marginRight: 1,
        borderRadius: 1,
        backgroundColor: "#f5f5f5",
        boxShadow: 1,
        cursor: "pointer",
        "&:hover": {
          backgroundColor: "#eeeeee",
        },
        minHeight: "80px",
      }}
      onClick={onSelectMatrix}
    >
      <Typography variant="subtitle2" fontWeight="bold" color={titleColor} sx={{ fontSize: "1rem", mb: 1 }}>
        {title || ""}
      </Typography>
      <Typography 
        variant="caption" 
        color="text.secondary" 
        sx={{ 
          display: "-webkit-box",
          WebkitLineClamp: 3,
          WebkitBoxOrient: "vertical",
          overflow: "hidden",
          fontSize: "0.85rem",
          lineHeight: 1.4,
        }}
      >
        {description || ""}
      </Typography>
    </Paper>
  );
};

export default MiniMatrixCard; 
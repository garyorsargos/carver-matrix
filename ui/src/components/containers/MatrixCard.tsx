import React from "react";
import { Box, Typography, Button, Paper } from "@mui/material";

interface MatrixCardProps {
  title: string;
  description: string;
  onEditMatrix: () => void;
  onShare: () => void;
}

const MatrixCard: React.FC<MatrixCardProps> = ({
  title,
  description,
  onEditMatrix,
  onShare,
}) => {
  return (
    <Paper
      sx={{
        padding: 2,
        marginBottom: 2,
        borderRadius: 2,
        backgroundColor: "white",
        boxShadow: 2,
      }}
    >
      <Typography variant="h6" fontWeight="bold" color="gray">
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {description}
      </Typography>

      {/* Buttons */}
      <Box display="flex" justifyContent="space-between">
        <Button variant="outlined" color="primary" onClick={onEditMatrix}>
          Edit Matrix
        </Button>
        <Button variant="contained" color="secondary" onClick={onShare}>
          Export as PDF
        </Button>
      </Box>
    </Paper>
  );
};

export default MatrixCard;

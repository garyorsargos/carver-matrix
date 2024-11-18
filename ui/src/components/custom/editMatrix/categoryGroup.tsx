import React from "react";
import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Typography,
} from "@mui/material";
import { useMultiMatrix } from "../multiMatrixProvider";

interface CategoryDisplayProps {
  category: string;
  targetTitle: string;
}

const CategoryGroup: React.FC<CategoryDisplayProps> = ({ category, targetTitle }) => {
  const { multiMatrix, setMultiMatrix } = useMultiMatrix();
  const [score, setScore] = React.useState<number>(
    multiMatrix.get(targetTitle)?.get(category) || 1
  );

  const handleInputChange = (event: SelectChangeEvent<number>) => {
    const newScore = Number(event.target.value);
    setScore(newScore);

    setMultiMatrix((prevMatrix) => {
      const updatedMatrix = new Map(prevMatrix);

      // Get or create the sub-map for the current targetTitle
      const targetMap = updatedMatrix.get(targetTitle) || new Map<string, number>();
      targetMap.set(category, newScore);

      // Set the updated sub-map back to the outer map
      updatedMatrix.set(targetTitle, targetMap);

      return updatedMatrix;
    });

    console.log("Updated MultiMatrix:", multiMatrix);
  };

  return (
    <Box>
      <Typography>{category}</Typography>
      <Typography>{targetTitle}</Typography>
      <FormControl fullWidth sx={{ mt: 2 }}>
        <InputLabel id="score-select-label">Score</InputLabel>
        <Select
          id="score-select"
          value={score}
          label="Score"
          onChange={handleInputChange}
          sx={{
            ".MuiSelect-select": {
              color: "black",
            },
          }}
        >
          <MenuItem value={1}>1</MenuItem>
          <MenuItem value={2}>2</MenuItem>
          <MenuItem value={3}>3</MenuItem>
          <MenuItem value={4}>4</MenuItem>
          <MenuItem value={5}>5</MenuItem>
        </Select>
      </FormControl>
    </Box>
  );
};

export default CategoryGroup;


import React from "react";
import {
  Box,
  FormControl,
  Select,
  MenuItem,
  SelectChangeEvent,
} from "@mui/material";
import { useMultiMatrix } from "../multiMatrixProvider";

interface CategoryDisplayProps {
  category: string;
  targetTitle: string;
}

const CategoryGroup: React.FC<CategoryDisplayProps> = ({ category, targetTitle }) => {
  const { multiMatrix, setMultiMatrix } = useMultiMatrix();

  // Get the initial score from the multiMatrix, defaulting to 1 if undefined.
  const initialScore = multiMatrix.get(targetTitle)?.get(category);
  const [score, setScore] = React.useState<number>(initialScore !== undefined ? initialScore : 1);

  const handleInputChange = (event: SelectChangeEvent<string>) => {
    const newScore = Number(event.target.value);
    setScore(newScore);

    setMultiMatrix((prevMatrix) => {
      // Create a shallow copy of the outer map.
      const updatedMatrix = new Map(prevMatrix);
      // Get a copy of the inner map or create a new one if it doesn't exist.
      const targetMap = updatedMatrix.get(targetTitle)
        ? new Map(updatedMatrix.get(targetTitle))
        : new Map<string, number>();
      // Update the category score.
      targetMap.set(category, newScore);
      // Set the updated inner map back.
      updatedMatrix.set(targetTitle, targetMap);
      return updatedMatrix;
    });

    console.log("Updated MultiMatrix:", multiMatrix);
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="center" sx={{ width: "100%" }}>
      <FormControl fullWidth sx={{ mt: 1, minWidth: 50 }}>
        <Select
          id="score-select"
          value={score.toString()}
          label="Score"
          onChange={handleInputChange}
          sx={{
            ".MuiSelect-select": {
              color: "black",
              fontSize: { xs: "0.75rem", md: "1rem" },
              minWidth: 50,
            },
          }}
        >
          <MenuItem value="1">1</MenuItem>
          <MenuItem value="2">2</MenuItem>
          <MenuItem value="3">3</MenuItem>
          <MenuItem value="4">4</MenuItem>
          <MenuItem value="5">5</MenuItem>
        </Select>
      </FormControl>
    </Box>
  );
};

export default CategoryGroup;


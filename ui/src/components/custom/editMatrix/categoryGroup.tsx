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
    <Box display="flex" justifyContent="center" alignItems="center" sx={{ width: "100%" }}>
      <FormControl fullWidth sx={{ mt: 1, minWidth: 50 }}>
        <Select
          id="score-select"
          value={score}
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


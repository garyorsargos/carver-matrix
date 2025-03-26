import React from "react";
import { Box, FormControl, Select, MenuItem, SelectChangeEvent } from "@mui/material";
import { useMultiMatrix } from "../multiMatrixProvider";

interface CategoryDisplayProps {
  category: string;
  targetTitle: string;
}

const CategoryGroup: React.FC<CategoryDisplayProps> = ({ category, targetTitle }) => {
  const { multiMatrix, setMultiMatrix, itemIdMap, setUpdates } = useMultiMatrix();

  const initialScore = multiMatrix.get(targetTitle)?.get(category);
  const [score, setScore] = React.useState<number>(initialScore !== undefined ? initialScore : 1);

  // Map the UI category names to API field names.
  const categoryMap: { [key: string]: string } = {
    "Criticality": "criticality",
    "Accessibility": "accessibility",
    "Recuperability": "recoverability",
    "Vulnerability": "vulnerability",
    "Effect": "effect",
    "Recognizability": "recognizability",
  };
  const apiCategory = categoryMap[category] || category.toLowerCase();

  const handleInputChange = (event: SelectChangeEvent<string>) => {
    const newScore = Number(event.target.value);
    setScore(newScore);

    // Update the local matrix state.
    setMultiMatrix((prevMatrix) => {
      const updatedMatrix = new Map(prevMatrix);
      const targetMap = updatedMatrix.get(targetTitle)
        ? new Map(updatedMatrix.get(targetTitle))
        : new Map<string, number>();
      targetMap.set(category, newScore);
      updatedMatrix.set(targetTitle, targetMap);
      return updatedMatrix;
    });

    // Also update the "updates" state to record this change.
    const itemId = itemIdMap.get(targetTitle);
    if (!itemId) {
      console.warn(`No itemId found for ${targetTitle}`);
      return;
    }
    setUpdates((prevUpdates) => {
      const existingIndex = prevUpdates.findIndex((u: any) => u.itemId === itemId);
      if (existingIndex !== -1) {
        // Merge the new change with the existing update.
        const updatedItem = { ...prevUpdates[existingIndex], [apiCategory]: newScore };
        const newUpdates = [...prevUpdates];
        newUpdates[existingIndex] = updatedItem;
        return newUpdates;
      } else {
        // Create a new update entry.
        return [...prevUpdates, { itemId, [apiCategory]: newScore }];
      }
    });
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="center" sx={{ width: "100%" }}>
      <FormControl fullWidth sx={{ mt: 1, minWidth: 50 }}>
        <Select
          id="score-select"
          value={score.toString()}
          onChange={handleInputChange}
          sx={{
            ".MuiSelect-select": {
              color: "#ffffff",
              fontSize: { xs: "0.75rem", md: "1rem" },
              minWidth: 50,
            },
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: 'rgba(255, 255, 255, 0.23)',
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: '#014093',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: '#014093',
            },
            '& .MuiSelect-icon': {
              color: 'rgba(255, 255, 255, 0.7)',
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


import React from "react";
import { Box, FormControl, Select, MenuItem, SelectChangeEvent } from "@mui/material";
import { useMultiMatrix } from "../multiMatrixProvider";

interface CategoryDisplayProps {
  category: string;
  targetTitle: string;
}

const CategoryGroup: React.FC<CategoryDisplayProps> = ({ category, targetTitle }) => {
  const { setMultiMatrix, itemIdMap, setUpdates, rawItems, config } = useMultiMatrix();
  const currentEmail = config.currentUserEmail;

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

  // Find the raw item for this target
  const rawItem = rawItems.find(item => item.itemName === targetTitle);
  const scores = rawItem ? (rawItem[apiCategory] || {}) as { [email: string]: number } : {};
  const initialScore = currentEmail ? scores[currentEmail] || 0 : 0;
  const [score, setScore] = React.useState<number>(initialScore);

  const handleInputChange = (event: SelectChangeEvent<string>) => {
    if (!currentEmail) return;
    
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
        // Get existing scores or initialize new ones
        const existingScores = prevUpdates[existingIndex][apiCategory] || {};
        const updatedScores = {
          ...existingScores,
          [currentEmail]: newScore
        };

        // Merge the new change with the existing update
        const updatedItem = {
          ...prevUpdates[existingIndex],
          [apiCategory]: updatedScores
        };
        const newUpdates = [...prevUpdates];
        newUpdates[existingIndex] = updatedItem;
        return newUpdates;
      } else {
        // Create a new update entry with user-specific score
        return [...prevUpdates, {
          itemId,
          [apiCategory]: {
            [currentEmail]: newScore
          }
        }];
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
          <MenuItem value="0">-</MenuItem>
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

